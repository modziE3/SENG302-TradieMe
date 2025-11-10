package nz.ac.canterbury.seng302.homehelper.controller.account;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.List;

/**
 * Controller for the registration form
 */
@Controller
public class RegistrationFormController {
    Logger logger = LoggerFactory.getLogger(RegistrationFormController.class);

    private final UserService userService;
    private final EmailService emailService;
    private final TaskScheduler taskScheduler;
    private final LocationService locationService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final LocationQueryService locationQueryService;

    @Autowired
    public RegistrationFormController(UserService userService, EmailService emailService, TaskScheduler taskScheduler,
                                      LocationService locationService, PasswordEncoder passwordEncoder,
                                      AuthenticationManager authenticationManager, LocationQueryService locationQueryService) {
        this.userService = userService;
        this.emailService = emailService;
        this.taskScheduler = taskScheduler;
        this.locationService = locationService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.locationQueryService = locationQueryService;
    }

    /**
     * displays the registration form it accepts and displays the results of the previous form if the user did not input
     * their details correctly
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @return thymeleaf registrationForm
     */
    @GetMapping("/registration-form")
    public String registration(Model model, HttpServletRequest request, HttpSession session) {
        logger.info("GET /registration-form");

        locationQueryService.setLocationQuerySessionAttribute(request, session);

        model.addAttribute("firstName", "");
        model.addAttribute("lastName", "");
        model.addAttribute("email", "");
        model.addAttribute("password", "");
        model.addAttribute("secondPassword", "");
        model.addAttribute("streetAddress", "");
        model.addAttribute("suburb", "");
        model.addAttribute("city", "");
        model.addAttribute("postcode", "");
        model.addAttribute("country", "");
        model.addAttribute("latitude", "");
        model.addAttribute("longitude", "");
        return "registrationFormTemplate";
    }

    /**
     * posts a new user with all of their information. If they have input the information in the right format then it
     * creates a new user and sends them to their profile page. If not then it remakes the form and displays the error
     * message.
     * @param firstName the first name of the user
     * @param lastName the last name of the user
     * @param email the users email
     * @param password the users password
     * @param secondPassword the users password typed in again to make sure that they got it write
     * @param model (map-like) representation of name, language and isJava boolean for use in thymeleaf,
     *              with values being set to relevant parameters provided
     * @return thymeleaf "registrationForm" if there was an error else take them to their new profile page
     */
    @PostMapping("/registration-form")
    public String submitRegistration(
            @RequestParam(name = "firstName") String firstName,
            @RequestParam(name = "lastName") String lastName,
            @RequestParam(name = "email") String email,
            @RequestParam(name = "password") String password,
            @RequestParam(name = "secondPassword") String secondPassword,
            @RequestParam(name = "streetAddress", required = false) String streetAddress,
            @RequestParam(name = "suburb", required = false) String suburb,
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "postcode", required = false) String postcode,
            @RequestParam(name = "country", required = false) String country,
            @RequestParam(name = "latitude", required = false) String latitude,
            @RequestParam(name = "longitude", required = false) String longitude,
            Model model,
            HttpServletRequest request) {
        logger.info("POST /registration-form");

        try {
            List<String> locationInfo = userService.setEmptyLocationsToNull(List.of(streetAddress, suburb, city, postcode, country));
            userService.validateUserWithPasswordAndEmailUnique(firstName, lastName, email, password, secondPassword,
                    locationInfo, null, true);

            String verificationCode = userService.generateValidationCode();
            String hashedPassword = passwordEncoder.encode(password);

            User newUser = new User(firstName, lastName, email, hashedPassword, null, verificationCode);
            try {
                newUser.setLatitude(Float.parseFloat(latitude));
                newUser.setLongitude(Float.parseFloat(longitude));
            } catch (Exception ignored) {}
            userService.setUserLocation(newUser, locationInfo);
            newUser.grantAuthority("ROLE_USER");
            userService.addUser(newUser);

            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(email, password, newUser.getAuthorities());
            Authentication auth = authenticationManager.authenticate(token);
            if (auth.isAuthenticated()) {
                SecurityContextHolder.getContext().setAuthentication(auth);
                request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
            }

            taskScheduler.schedule(
                    () -> emailService.sendConfirmRegistrationEmail(email, verificationCode),
                    Instant.now()
            );

            return "redirect:./registration-code";
        } catch(IllegalArgumentException e){
            logger.error("Form submission error: {}", e.getMessage());

            model.addAttribute("firstName", firstName.trim());
            model.addAttribute("lastName", lastName.trim());
            model.addAttribute("email", email.trim());
            model.addAttribute("password", password.trim());
            model.addAttribute("secondPassword", secondPassword.trim());
            model.addAttribute("streetAddress", streetAddress.trim());
            model.addAttribute("suburb", suburb.trim());
            model.addAttribute("city", city.trim());
            model.addAttribute("postcode", postcode.trim());
            model.addAttribute("country", country.trim());
            model.addAttribute("latitude", latitude.trim());
            model.addAttribute("longitude", longitude.trim());

            List<String> errorMessages = userService.getErrorMessages(e.getMessage());
            model.addAttribute("firstNameErrorMessage", errorMessages.get(0));
            model.addAttribute("lastNameErrorMessage", errorMessages.get(1));
            model.addAttribute("emailErrorMessage", errorMessages.get(2));
            model.addAttribute("passwordErrorMessage", errorMessages.get(3));
            model.addAttribute("secondPasswordErrorMessage", errorMessages.get(4));

            locationService.setLocationErrorMessages(e.getMessage(), model);

            return "registrationFormTemplate";
        }
    }
}
