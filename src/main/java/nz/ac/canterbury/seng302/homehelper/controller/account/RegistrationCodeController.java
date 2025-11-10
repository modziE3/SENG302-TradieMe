package nz.ac.canterbury.seng302.homehelper.controller.account;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

/**
 * Controller for the registration code form
 */
@Controller
public class RegistrationCodeController {

    private final Logger logger = LoggerFactory.getLogger(RegistrationCodeController.class);
    private final UserService userService;

    @Autowired
    public RegistrationCodeController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Displays the form for inputting the verification code sent by a registration email
     * If code is expired, expired code page is displayed instead
     * @param verificationCode verification code
     * @param errorMessage error message
     * @param principal the currently authenticated user
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @return thymeleaf registrationCode
     */
    @GetMapping("/registration-code")
    public String registrationCode(
            @RequestParam(name = "verificationCode", required = false, defaultValue = "") String verificationCode,
            @RequestParam(name = "errorMessage", required = false) String errorMessage,
            Principal principal,
            Model model) {
        if (principal != null && userService.getUser(principal.getName()) != null &&(userService.getUser(principal.getName())).getVerificationCode() == null) {
            return "redirect:/home";
        }
        logger.info("GET /registration-code");
        model.addAttribute("verificationCode", verificationCode.trim());
        model.addAttribute("errorMessage", errorMessage);
        return "registrationCodeTemplate";
    }

    /**
     * Posts the verification code to activate the user's account
     * If code is expired, expired code page is displayed instead
     * @param verificationCode verification code entered int the form
     * @param principal the currently authenticated user
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @return thymeleaf registrationCode
     */
    @PostMapping("/registration-code")
    public String registrationCodeSubmit(
            @RequestParam(name = "verificationCode", required = false, defaultValue = "") String verificationCode,
            Principal principal,
            Model model) {
        logger.info("POST /registration-code");

        try {
            User currentUser = userService.getUser(principal.getName());
            if (!verificationCode.equals(currentUser.getVerificationCode())) {
                throw new RuntimeException("Error when validating verification code");
            }
            userService.updateUserToBeVerified(currentUser);
            return "redirect:/logout?confirmation=Your account has been activated, please log in";
        } catch (Exception e) {
            String errorMessage = "Signup code invalid";
            if (userService.getUser(principal.getName()) == null) {
                errorMessage = "Signup code invalid. Your account has expired. Please logout";
            }
            model.addAttribute("verificationCode", verificationCode.trim());
            model.addAttribute("verificationCodeErrorMessage", errorMessage);
            return "registrationCodeTemplate";
        }
    }
}
