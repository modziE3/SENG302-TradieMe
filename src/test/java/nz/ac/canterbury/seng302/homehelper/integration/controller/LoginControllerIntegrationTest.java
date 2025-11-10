package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.Filter;
import nz.ac.canterbury.seng302.homehelper.controller.account.LoginController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@SpringBootTest
public class LoginControllerIntegrationTest {

    @Autowired
    private LoginController loginController;

    private MockMvc mockMvc;

    @Qualifier("springSecurityFilterChain")
    @Autowired
    private Filter springSecurityFilterChain;

    @PostConstruct
    public void setup() {

        mockMvc = MockMvcBuilders.standaloneSetup(loginController)
                .addFilter(springSecurityFilterChain)
                .build();
    }

    @Test
    void testGetLogin_EmptyValues() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("loginTemplate"))
                .andExpect(model().attributeDoesNotExist("errorMessageEmail"))
                .andExpect(model().attributeDoesNotExist("errorMessagePassword"));
    }

    @Test
    void testPostLogin_InvalidEmail() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "email")
                .param("password", "P4$$word"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login-error"));
    }

    @Test
    void testPostLogin_InvalidPassword() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "email@email.com")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login-error"));
    }

    @Test
    void testGetLogout_DefaultValues() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

}
