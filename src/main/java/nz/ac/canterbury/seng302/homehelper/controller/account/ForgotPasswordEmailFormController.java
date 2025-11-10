package nz.ac.canterbury.seng302.homehelper.controller.account;

import nz.ac.canterbury.seng302.homehelper.entity.ResetPasswordToken;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.ResetPasswordService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.ui.Model;
import nz.ac.canterbury.seng302.homehelper.service.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Instant;

@Controller
public class ForgotPasswordEmailFormController {
    Logger logger = LoggerFactory.getLogger(ForgotPasswordEmailFormController.class);

    ValidationService validationService;
    UserService userService;
    ResetPasswordService resetPasswordService;
    EmailService emailService;
    TaskScheduler taskScheduler;

    @Autowired
    public ForgotPasswordEmailFormController(
            ValidationService validationService,
            UserService userService,
            ResetPasswordService resetPasswordService,
            EmailService emailService,
            TaskScheduler taskScheduler) {
        this.validationService = validationService;
        this.userService = userService;
        this.resetPasswordService = resetPasswordService;
        this.emailService = emailService;
        this.taskScheduler = taskScheduler;
    }

    /***
     * Sets up the page for entering email to reset password
     * @return thymeleaf forgot password form
     */
    @GetMapping("/forgot-password")
    public String getTemplate() {
        logger.info("GET /forgot-password");
        return "forgotPasswordEmailTemplate";
    }

    /***
     * Checks if the email entered is valid
     * @param email The email which is entered by the user into the form
     * @param model representation of data which will be displayed
     * @return thymeleaf forgot password form
     */
    @PostMapping("/forgot-password")
    public String createAndEmailToken(
            @RequestParam(name = "email") String email,
            Model model) {
        logger.info("POST /forgot-password");

        if (!validationService.checkEmailForm(email)) {
            model.addAttribute("email", email);
            model.addAttribute("emailErrorMessage",
                    "Email address must be in the form 'jane@doe.nz'");
            return "forgotPasswordEmailTemplate";
        } else {
            if (userService.getUser(email) != null) {
                ResetPasswordToken token = new ResetPasswordToken(email, resetPasswordService.generateRandomCode());
                resetPasswordService.addResetPasswordToken(token);

                String baseURL = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
                if (!baseURL.contains("localhost:8080")) {
                    String newBaseURL = "https://csse-seng302-team1000.canterbury.ac.nz";
                    if (baseURL.contains("/test")) {
                        newBaseURL = newBaseURL.concat("/test");
                    } else if (baseURL.contains("/prod")) {
                        newBaseURL = newBaseURL.concat("/prod");
                    }
                    baseURL = newBaseURL;
                }
                String url = baseURL + "/reset-password?token=" + token.getToken();

                taskScheduler.schedule(
                        () -> emailService.sendResetPasswordLinkEmail(email, url),
                        Instant.now()
                );
            }
        }
        model.addAttribute("confirmationMessage",
                "An email was sent to the address if it was recognised");
        return "forgotPasswordEmailTemplate";
    }

}

