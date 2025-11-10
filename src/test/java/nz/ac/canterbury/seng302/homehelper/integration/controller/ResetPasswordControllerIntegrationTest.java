package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.controller.account.ResetPasswordFormController;
import nz.ac.canterbury.seng302.homehelper.entity.ResetPasswordToken;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.ResetPasswordTokenRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class ResetPasswordControllerIntegrationTest {

    @Autowired
    ResetPasswordFormController resetPasswordFormController;

    MockMvc mockMvc;

    @MockBean
    ResetPasswordTokenRepository resetPasswordTokenRepository;

    @MockBean
    UserRepository userRepository;

    @Autowired
    private UserService userService;
    private final User testUser = new User("Alex", "Cheals", "123@123.123", "password", null, null);

    @PostConstruct
    public void setup() {mockMvc = MockMvcBuilders.standaloneSetup(resetPasswordFormController).build(); }

    @Test
    void resetPassword_invalidPassword_ShowErrorMessage() throws Exception {
        Mockito.when(resetPasswordTokenRepository.findByToken(anyString())).thenReturn(Optional.of(new ResetPasswordToken("123@123.123", "token")));
        Mockito.when(userService.getUser("123@123.123")).thenReturn(testUser);

        mockMvc.perform(post("/reset-password")
                        .param("newPassword", "password")
                        .param("retypePassword", "password")
                        .param("token", "token"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("newPasswordErrorMessage", "Your password must be at least 8 characters " +
                        "long and include at least one uppercase letter, one lowercase letter, one number, and one " +
                        "special character"));
    }

    @Test
    void resetPassword_nonMatchingPasswords_ShowErrorMessage() throws Exception {
        Mockito.when(resetPasswordTokenRepository.findByToken(anyString())).thenReturn(Optional.of(new ResetPasswordToken("123@123.123", "token")));
        Mockito.when(userService.getUser("123@123.123")).thenReturn(testUser);

        mockMvc.perform(post("/reset-password")
                        .param("newPassword", "P4$$word")
                        .param("retypePassword", "P4$$word1")
                        .param("token", "token"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("retypePasswordErrorMessage", "The passwords do not match"));
    }

    @Test
    void resetPassword_validPassword_resetsPassword() throws Exception {
        Mockito.when(resetPasswordTokenRepository.findByToken(anyString())).thenReturn(Optional.of(new ResetPasswordToken("123@123.123", "token")));
        Mockito.when(userService.getUser("123@123.123")).thenReturn(new User("Alex", "Cheals", "123@123.123", "password", null, null));
        mockMvc.perform(post("/reset-password")
                        .param("newPassword", "P4$$word")
                        .param("retypePassword", "P4$$word")
                        .param("token", "token"));
        Mockito.verify(userRepository).updatePasswordWithUserEmail(eq("123@123.123"), anyString());
    }
}
