package nz.ac.canterbury.seng302.homehelper.controller.account;

import nz.ac.canterbury.seng302.homehelper.entity.ResetPasswordToken;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.ResetPasswordService;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.ui.Model;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.List;

/**
 * Controller for the resetting password form
 */
@Controller
public class ResetPasswordFormController {

    Logger logger = LoggerFactory.getLogger(ResetPasswordFormController.class);

    private final UserService userService;
    private final ResetPasswordService resetPasswordService;
    private final EmailService emailService;
    private final TaskScheduler taskScheduler;

    @Autowired
    public ResetPasswordFormController(
            UserService userService,
            ResetPasswordService resetPasswordService,
            EmailService emailService,
            TaskScheduler taskScheduler) {
        this.userService = userService;
        this.resetPasswordService = resetPasswordService;
        this.emailService = emailService;
        this.taskScheduler = taskScheduler;
    }

    /**
     * Sets up the form page with empty inputs and no error message
     * @param model representation of data to be used in thymeleaf display
     * @return thymeleaf reset password form template.
     */
    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam(name = "token") String token,
                                Model model) {
        logger.info("GET /reset-password?token={}", token);

        if (resetPasswordService.getByToken(token) == null) {
            return "redirect:/logout?error=Reset password link has expired";
        }

        model.addAttribute("token", token);
        return "resetPasswordFormTemplate";
    }

    /**
     * Posts the new password and retyped password entered into the form,
     * validates the passwords, updates the user's password and sends
     * an email to the user.
     * If reset password token is expired, then user is redirected to the login page
     * @param newPassword The new password entered into the reset password form
     * @param retypePassword The retyped password entered into the reset password form
     * @param token The unique reset password token emailed to the user after forgetting their password
     * @param model representation of data to be used in thymeleaf display
     * @return thymeleaf reset password form template
     */
    @PostMapping("/reset-password")
    public String submitReset(@RequestParam(name = "newPassword") String newPassword,
                              @RequestParam(name = "retypePassword") String retypePassword,
                              @RequestParam(name = "token") String token,
                              Model model) {
        logger.info("POST /reset-password?token={}", token);

        try {
            if (resetPasswordService.getByToken(token) == null) {
                return "redirect:/logout?error=Reset password link has expired";
            }

            model.addAttribute("token", token);
            ResetPasswordToken resetPasswordToken = resetPasswordService.getByToken(token);
            String userEmail = resetPasswordToken.getUserEmail();
            User user = userService.getUser(userEmail);
            resetPasswordService.validatePasswords(user, newPassword, retypePassword);

            taskScheduler.schedule(
                    () -> emailService.sendResetPasswordSuccessEmail(userEmail),
                    Instant.now()
            );
            userService.updateUserPassword(userEmail, newPassword);
            resetPasswordService.removeResetPasswordToken(resetPasswordToken);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            List<String> errorMessages = resetPasswordService.getErrorMessages(e.getMessage());
            model.addAttribute("newPasswordErrorMessage", errorMessages.get(0));
            model.addAttribute("retypePasswordErrorMessage", errorMessages.get(1));
            return "resetPasswordFormTemplate";
        }
    }
}
