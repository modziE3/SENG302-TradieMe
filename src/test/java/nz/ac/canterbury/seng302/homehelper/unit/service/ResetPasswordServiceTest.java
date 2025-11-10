package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.repository.ResetPasswordTokenRepository;
import nz.ac.canterbury.seng302.homehelper.service.ResetPasswordService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import java.util.List;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
public class ResetPasswordServiceTest {
    @Mock
    public ResetPasswordTokenRepository resetPasswordTokenRepository;
    @Mock
    public TaskScheduler taskScheduler;
    @InjectMocks
    public ResetPasswordService resetPasswordService;

    static Stream<Arguments> expectedErrorMessages() {
        return Stream.of(
                Arguments.of(ResetPasswordService.NEW_PASSWORD_INVALID,
                        ResetPasswordService.NEW_PASSWORD_INVALID, ""),
                Arguments.of(ResetPasswordService.PASSWORDS_DONT_MATCH,
                        "", ResetPasswordService.PASSWORDS_DONT_MATCH),
                Arguments.of(ResetPasswordService.NEW_PASSWORD_INVALID +
                                ResetPasswordService.PASSWORDS_DONT_MATCH,
                        ResetPasswordService.NEW_PASSWORD_INVALID, ResetPasswordService.PASSWORDS_DONT_MATCH)
        );
    }
    @ParameterizedTest
    @MethodSource("expectedErrorMessages")
    public void getErrorMessages_AllErrorMessagesStringProvided_CorrectSeparateErrorMessagesReturned
            (String allErrorMessages, String newPasswordErrorMessage, String retypePasswordErrorMessage) {
        List<String> errorMessages = resetPasswordService.getErrorMessages(allErrorMessages);

        Assertions.assertEquals(newPasswordErrorMessage, errorMessages.get(0));
        Assertions.assertEquals(retypePasswordErrorMessage, errorMessages.get(1));
    }
}
