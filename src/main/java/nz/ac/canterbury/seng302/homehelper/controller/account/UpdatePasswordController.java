package nz.ac.canterbury.seng302.homehelper.controller.account;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.service.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Controller
public class UpdatePasswordController {
    Logger logger = LoggerFactory.getLogger(UpdatePasswordController.class);

    private final ValidationService validationService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TaskScheduler taskScheduler;

    @Autowired
    public UpdatePasswordController(UserService userService,
                                    ValidationService validationService,
                                    PasswordEncoder passwordEncoder,
                                    EmailService emailService,
                                    TaskScheduler taskScheduler) {
        this.userService = userService;
        this.validationService = validationService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.taskScheduler = taskScheduler;
    }

    /**
     * Gets the update-password request and loads the updatePasswordTemplate form for the user.
     * Handles displaying the update password page for the user.
     * @return the thymeleaf updatePasswordTemplate page.
     */
    @GetMapping("/update-password")
    public String updatePassword() {
        logger.info("GET /update-password");
        return "updatePasswordTemplate";
    }

    /**
     * Submits the user's updated password details. Performs checks to check the current password of the user, the
     * strength of the new password, and the confirmation password matches. If the checks fail then the corresponding
     * error messages are displayed, and if the checks succeed then it updates the password.
     * @param currentPassword the user's current password.
     * @param newPassword the user's desired new password.
     * @param confirmPassword confirmation of the new password.
     * @param model the attributes to be displayed to the user, contains the error messages.
     * @param principal the current user.
     * @return the updatePasswordTemplate if checks fail or redirect to "/profile" if they pass.
     */
    @PostMapping("/update-password")
    public String submitUpdatePassword(@RequestParam(name="currentPassword") String currentPassword,
                                       @RequestParam(name="newPassword") String newPassword,
                                       @RequestParam(name="confirmPassword") String confirmPassword,
                                       Model model,
                                       Principal principal) {
        logger.info("POST /update-password");

        User currentUser = userService.getUser(principal.getName());

        String oldPasswordIncorrect = "Your old password is incorrect";
        String newPasswordInvalid = "Your password must be at least 8 characters long and include at least one uppercase " +
                "letter, one lowercase letter, one number, and one special character";
        String newPasswordsDontMatch = "The new passwords do not match";

        List<String> errors = new ArrayList<>();
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            errors.add(oldPasswordIncorrect);
        }
        if (!validationService.checkPassword(newPassword, currentUser)) {
            errors.add(newPasswordInvalid);
        }
        if (!newPassword.equals(confirmPassword)) {
            errors.add(newPasswordsDontMatch);
        }

        if (!errors.isEmpty()) {
            if (errors.contains(oldPasswordIncorrect)) {
                model.addAttribute("currentPasswordErrorMessage", oldPasswordIncorrect);
            }
            if (errors.contains(newPasswordInvalid)) {
                model.addAttribute("newPasswordErrorMessage", newPasswordInvalid);
            }
            if (errors.contains(newPasswordsDontMatch)) {
                model.addAttribute("confirmPasswordErrorMessage", newPasswordsDontMatch);
            }

            return "updatePasswordTemplate";
        } else {
            userService.updateUserPassword(principal.getName(), newPassword);
            taskScheduler.schedule(
                    () -> emailService.sendConfirmUpdatedPasswordEmail(principal.getName(), currentUser),
                    Instant.now()
            );

            return "redirect:./profile?userId="+currentUser.getId();
        }
    }
}
