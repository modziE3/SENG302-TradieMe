package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.ResetPasswordToken;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.ResetPasswordTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


/**
 * Service class for ResetPasswordTokens.
 * This class links automatically with @link{ResetPasswordTokenRepository}
 */
@Service
public class ResetPasswordService {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final long TEN_MINUTES = 600000; // in milliseconds

    // Error messages for invalid passwords
    public static final String NEW_PASSWORD_INVALID = "Your password must be at least 8 characters long and include " +
            "at least one uppercase letter, one lowercase letter, one number, and one special character";
    public static final String PASSWORDS_DONT_MATCH = "The passwords do not match";

    private final ResetPasswordTokenRepository resetPasswordTokenRepository;
    private final TaskScheduler taskScheduler;
    private final ValidationService validationService;
    private final SecureRandom secureRandom;

    /**
     * Constructor for the resetPasswordService.
     * @param resetPasswordTokenRepository repo for the {@link ResetPasswordToken} entity.
     * @param taskScheduler spring boot job scheduler.
     */
    @Autowired
    public ResetPasswordService(ResetPasswordTokenRepository resetPasswordTokenRepository,
                                TaskScheduler taskScheduler) {
        this.resetPasswordTokenRepository = resetPasswordTokenRepository;
        this.taskScheduler = taskScheduler;
        this.validationService = new ValidationService();
        this.secureRandom = new SecureRandom();
    }

    /**
     * MADE BY CHATGPT. Takes the randomSecure and generates a random unique secure token
     * @return a 10 character long secure random unique code.
     */
    public String generateRandomCode() {
        int length = 10; // 10 random characters for the code
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        return code.toString();
    }

    /**
     * Adds a resetPasswordToken to the repo and schedules a job to delete the token
     * from the repo ten minutes later.
     * @param resetPasswordToken the token being added to the repo
     */
    public void addResetPasswordToken(ResetPasswordToken resetPasswordToken) {
        resetPasswordTokenRepository.save(resetPasswordToken);
        taskScheduler.schedule(()->removeResetPasswordToken(resetPasswordToken), Instant.now().plusMillis(TEN_MINUTES));
    }

    /**
     * Simply deletes a resetPasswordToken from the repo.
     * @param resetPasswordToken the token being deleted.
     */
    public void removeResetPasswordToken(ResetPasswordToken resetPasswordToken) {
        resetPasswordTokenRepository.delete(resetPasswordToken);
    }

    /**
     * Searches the repo for a resetPasswordToken with the given token.
     * @param token a 10 character string which is the token.
     * @return The resetPasswordToken if found
     */
    public ResetPasswordToken getByToken(String token) {
        return resetPasswordTokenRepository.findByToken(token).orElse(null);
    }

    /**
     * Validates the passwords entered into the reset password form
     * @param user The user wanting to reset their password
     * @param newPassword The new password entered into the form
     * @param retypePassword The retyped new password entered into the form
     */
    public void validatePasswords(User user, String newPassword, String retypePassword) throws IllegalArgumentException {
        List<String> errors = new ArrayList<>();

        if (!validationService.checkPassword(newPassword, user)) {
            errors.add(NEW_PASSWORD_INVALID);
        }
        if (!validationService.passwordMatch(newPassword, retypePassword)) {
            errors.add(PASSWORDS_DONT_MATCH);
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }
    }

    /**
     * Goes through a string of all detected errors from the reset password form
     * and assigns an error message from the string to be displayed for each
     * field of the form
     * @param allErrorMessages String of all errors detected from the reset password form
     * @return List of error messages (one for each input field of the form) that will
     * be displayed on the form
     */
    public List<String> getErrorMessages(String allErrorMessages) {
        String newPasswordErrorMessage = "";
        if (allErrorMessages.contains(NEW_PASSWORD_INVALID)) {
            newPasswordErrorMessage = NEW_PASSWORD_INVALID;
        }

        String retypePasswordErrorMessage = "";
        if (allErrorMessages.contains(PASSWORDS_DONT_MATCH)) {
            retypePasswordErrorMessage = PASSWORDS_DONT_MATCH;
        }

        return List.of(newPasswordErrorMessage, retypePasswordErrorMessage);
    }
}
