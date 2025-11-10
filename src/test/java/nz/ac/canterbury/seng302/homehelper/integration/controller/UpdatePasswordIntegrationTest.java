package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.controller.account.UpdatePasswordController;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class UpdatePasswordIntegrationTest {

    @Autowired
    private UpdatePasswordController updatePasswordController;

    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private User testUser;
    private Principal authentication;

    @PostConstruct
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(updatePasswordController).build();
        testUser = new User("Jane", "Doe", "jane@example.com", passwordEncoder.encode("P4$$word"),
                null, userService.generateValidationCode());
        testUser.setId(1L);
        testUser.grantAuthority("ROLE_USER");
        authentication = new UsernamePasswordAuthenticationToken(testUser.getEmail(), null, testUser.getAuthorities());
    }

    @Test
    void testGetUpdatePassword_DefaultValues() throws Exception {
        mockMvc.perform(get("/update-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("updatePasswordTemplate"));
    }

    @Test
    void testPostUpdatePassword_IncorrectPassword() throws Exception {
        when(userRepository.findByEmailContainingIgnoreCase(authentication.getName())).thenReturn(testUser);

        mockMvc.perform(post("/update-password")
                        .sessionAttr(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, authentication)
                        .param("currentPassword", "notP4$$word")
                        .param("newPassword", "newP4$$word" )
                        .param("confirmPassword", "newP4$$word")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("updatePasswordTemplate"))
                .andExpect(model().attribute("currentPasswordErrorMessage", "Your old password is incorrect"));

        verify(userRepository, never()).updatePasswordWithUserEmail(eq("jane@example.com"), anyString());
    }

    static Stream<Arguments> invalidPasswords() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of("newpassword"),
                Arguments.of("NEWPASSWORD"),
                Arguments.of("newP4ssword"),
                Arguments.of("newPa$$word"),
                Arguments.of("newP4$$")
        );
    }
    @ParameterizedTest
    @MethodSource("invalidPasswords")
    void testPostUpdatePassword_InvalidNewPassword(String invalidPassword) throws Exception {
        when(userRepository.findByEmailContainingIgnoreCase(authentication.getName())).thenReturn(testUser);

        mockMvc.perform(post("/update-password")
                        .sessionAttr(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, authentication)
                        .param("currentPassword", "P4$$word")
                        .param("newPassword", invalidPassword)
                        .param("confirmPassword", "newP4$$word")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("updatePasswordTemplate"))
                .andExpect(model().attribute("newPasswordErrorMessage",
                        "Your password must be at least 8 characters long and include at least one uppercase " +
                                "letter, one lowercase letter, one number, and one special character"));

        verify(userRepository, never()).updatePasswordWithUserEmail(eq("jane@example.com"), anyString());
    }

    @Test
    void testPostUpdatePassword_DifferentPasswords() throws Exception {
        when(userRepository.findByEmailContainingIgnoreCase(authentication.getName())).thenReturn(testUser);

        mockMvc.perform(post("/update-password")
                        .sessionAttr(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, authentication)
                        .principal(authentication)
                        .param("currentPassword", "P4$$word")
                        .param("newPassword", "newP4$$word" )
                        .param("confirmPassword", "newtP4$$word"))
                .andExpect(status().isOk())
                .andExpect(view().name("updatePasswordTemplate"))
                .andExpect(model().attribute("confirmPasswordErrorMessage", "The new passwords do not match"));

        verify(userRepository, never()).updatePasswordWithUserEmail(eq("jane@example.com"), anyString());
    }

    @Test
    void testPostUpdatePassword_Success() throws Exception {
        when(userService.getUser(authentication.getName())).thenReturn(testUser);

        mockMvc.perform(post("/update-password")
                        .sessionAttr(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, authentication)
                        .principal(authentication)
                        .param("currentPassword", "P4$$word")
                        .param("newPassword", "newP4$$word" )
                        .param("confirmPassword", "newP4$$word"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:./profile?userId="+testUser.getId()));

        verify(userRepository).updatePasswordWithUserEmail(eq("jane@example.com"), anyString());
    }
}
