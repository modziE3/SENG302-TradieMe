package nz.ac.canterbury.seng302.homehelper.controller.account;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
public class EditProfileController {
    private static final Logger logger = LoggerFactory.getLogger(EditProfileController.class);
    private final UserService userService;
    private final RenovationRecordService renovationRecordService;
    private final ImageService imageService;
    private final LocationService locationService;
    private final LocationQueryService locationQueryService;
    private final UserRepository userRepository;

    @Autowired
    public EditProfileController(UserService userService, RenovationRecordService renovationRecordService,
                                 ImageService imageService, LocationService locationService,
                                 LocationQueryService locationQueryService, UserRepository userRepository) {
        this.userService = userService;
        this.renovationRecordService = renovationRecordService;
        this.imageService = imageService;
        this.locationService = locationService;
        this.locationQueryService = locationQueryService;
        this.userRepository = userRepository;
    }

    /**
     * Checks if user exists/is logged in, if not redirects to login.
     * Fetches user data and model is used to display it on the edit profile page.
     * @param model shows previously existing details
     * @param principal is used to access details from currently authenticated user
     * @return the editProfileTemplate to display to the user.
     */
    @GetMapping("/edit-profile")
    public String editProfile(Model model, Principal principal, HttpServletRequest request, HttpSession session) {
        logger.info("GET /edit-profile");

        if (principal == null) {
            return "redirect:/login";
        }

        locationQueryService.setLocationQuerySessionAttribute(request, session);

        String email = principal.getName();
        User user = userService.getUser(email);
        model.addAttribute("userId", user.getId());
        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("streetAddress", user.getStreetAddress());
        model.addAttribute("suburb", user.getSuburb());
        model.addAttribute("city", user.getCity());
        model.addAttribute("postcode", user.getPostcode());
        model.addAttribute("country", user.getCountry());
        model.addAttribute("latitude", user.getLatitude() == 0.0f ? null : user.getLatitude());
        model.addAttribute("longitude", user.getLongitude() == 0.0f ? null : user.getLongitude());
        model.addAttribute("user", user);
        if (user.getProfilePicture() != null) {
            model.addAttribute("profileImage", "/profileImages/" + user.getProfilePicture());
        }
        return "editProfileTemplate";
    }

    /**
     * Updates user profile with new details
     * updates old principal with new one by re-authenticating
     * @param userId    user id (does not change)
     * @param firstName user first name
     * @param lastName  user lastname
     * @param email     user email
     */
    @PostMapping("/edit-profile")
    public String updateProfile(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "firstName") String firstName,
            @RequestParam(name = "lastName") String lastName,
            @RequestParam(name = "email") String email,
            @RequestParam(name = "streetAddress") String streetAddress,
            @RequestParam(name = "suburb") String suburb,
            @RequestParam(name = "city") String city,
            @RequestParam(name = "postcode") String postcode,
            @RequestParam(name = "country") String country,
            @RequestParam(name = "latitude", required = false) String latitude,
            @RequestParam(name = "longitude", required = false) String longitude,
            Model model) {
        logger.info("POST /edit-profile");

        try {
            List<String> locationInfo = userService.setEmptyLocationsToNull(List.of(streetAddress, suburb, city, postcode, country));
            userService.validateUserWithPasswordAndEmailUnique(firstName, lastName, email,
                    null, null, locationInfo, userId, false);

            User user = userService.getUserById(userId);
            List<RenovationRecord> records = renovationRecordService.getRenovationRecordsByOwner(user.getEmail());
            for (RenovationRecord record : records) {
                record.setUserEmail(email);
            }
            userService.updateDetails(userId, email, firstName, lastName, user.getProfilePicture());
            userService.updateLocation(userId, locationInfo);
            try {
                userRepository.updateCoordinates(userId, Float.parseFloat(latitude), Float.parseFloat(longitude));
            } catch (Exception ignored) {}

            User updatedUser = userService.getUserById(userId);

            try {
                Authentication authentication = new UsernamePasswordAuthenticationToken(email, updatedUser.getPassword(), updatedUser.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch(Exception e) {
                logger.info(e.getMessage());
            }

            logger.info("Updated Authentication: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            return "redirect:./profile?userId="+user.getId();
        } catch (IllegalArgumentException e) {
            logger.info("Exception caught");
            model.addAttribute("userId", userId);
            model.addAttribute("firstName", firstName.trim());
            model.addAttribute("lastName", lastName.trim());
            model.addAttribute("email", email.trim());
            model.addAttribute("streetAddress", streetAddress.trim());
            model.addAttribute("suburb", suburb.trim());
            model.addAttribute("city", city.trim());
            model.addAttribute("postcode", postcode.trim());
            model.addAttribute("country", country.trim());
            model.addAttribute("latitude", latitude.trim());
            model.addAttribute("longitude", longitude.trim());

            logger.error(e.getMessage());
            List<String> errorMessages = userService.getErrorMessages(e.getMessage());
            model.addAttribute("firstNameErrorMessage", errorMessages.get(0));
            model.addAttribute("lastNameErrorMessage", errorMessages.get(1));
            model.addAttribute("emailErrorMessage", errorMessages.get(2));

            locationService.setLocationErrorMessages(e.getMessage(), model);

            return "editProfileTemplate";
        }
    }

    /**
     * Updates the user profile picture
     * @param userId The users ID stored in a hidden input in the form
     * @param submittedFile The file which the user has submitted
     * @return The edit profile template
     */
    @PostMapping("/update-image")
    public String updateUserImage(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "submittedFile") MultipartFile submittedFile,
            RedirectAttributes redirectAttributes) {
        logger.info("POST /update-image");

        try {
            imageService.validateImage(submittedFile);
            User user = userService.getUserById(userId);
            imageService.saveImage(submittedFile, user);
        } catch (IOException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("fileErrorMessage", e.getMessage());
        }
        return "redirect:/edit-profile";
    }
}
