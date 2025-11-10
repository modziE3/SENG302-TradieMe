package nz.ac.canterbury.seng302.homehelper.integration.service;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.entity.ResetPasswordToken;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.ResetPasswordTokenRepository;
import nz.ac.canterbury.seng302.homehelper.service.ResetPasswordService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class ResetPasswordServiceIntegrationTest {

    private ResetPasswordService resetPasswordService;
    private TaskScheduler taskScheduler;
    private ResetPasswordTokenRepository resetPasswordTokenRepository;

    @PostConstruct
    public void setup() {
        resetPasswordTokenRepository = Mockito.mock(ResetPasswordTokenRepository.class);
        taskScheduler = Mockito.mock(TaskScheduler.class);
        resetPasswordService = new ResetPasswordService(resetPasswordTokenRepository, taskScheduler);
    }

    @Test
    public void addResetPasswordToken_ValidResetPasswordTokenIsAdded_TokenIsSaved() {
        ResetPasswordToken resetPasswordToken = new ResetPasswordToken(
                "john@example.com",
                resetPasswordService.generateRandomCode());
        resetPasswordService.addResetPasswordToken(resetPasswordToken);
        verify(resetPasswordTokenRepository, times(1)).save(resetPasswordToken);
    }

    @Test
    public void addResetPasswordToken_ValidResetPasswordTokenIsAdded_JobIsScheduled() {
        ResetPasswordToken resetPasswordToken = new ResetPasswordToken(
                "john@example.com",
                resetPasswordService.generateRandomCode());
        resetPasswordService.addResetPasswordToken(resetPasswordToken);
        verify(taskScheduler, times(1)).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    public void removeResetPasswordToken_TokenIsAddedAndRemoved_TokenIsDeletedFromRepo() {
        ResetPasswordToken resetPasswordToken = new ResetPasswordToken(
                "john@example.com",
                resetPasswordService.generateRandomCode());
        resetPasswordService.addResetPasswordToken(resetPasswordToken);
        verify(resetPasswordTokenRepository, times(0)).delete(resetPasswordToken);
        resetPasswordService.removeResetPasswordToken(resetPasswordToken);
        verify(resetPasswordTokenRepository, times(1)).delete(resetPasswordToken);
    }

    @Test
    public void validatePasswords_validPasswords_ExceptionNotThrown() {
        User testUser = new User("John", "Doe", "john@example.com", "Qwertyuiop1!",
                null, null);

        Assertions.assertDoesNotThrow(() ->
                resetPasswordService.validatePasswords(testUser, "P4$$word", "P4$$word"));
    }

    static Stream<Arguments> invalidPasswords() {
        return Stream.of(
                Arguments.of("", ""),
                Arguments.of("password", ""),
                Arguments.of("Password", ""),
                Arguments.of("P4ssword", ""),
                Arguments.of("Pa$$word", ""),
                Arguments.of("P4$$WORD", ""),
                Arguments.of("P4$$word", "password")
        );
    }
    @ParameterizedTest
    @MethodSource("invalidPasswords")
    public void validatePasswords_invalidPasswords_ExceptionThrown(String newPassword, String retypePassword) {
        User testUser = new User("John", "Doe", "john@example.com", "Qwertyuiop1!",
                null, null);

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                resetPasswordService.validatePasswords(testUser, newPassword, retypePassword));
    }
}
