package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.controller.account.ForgotPasswordEmailFormController;
import nz.ac.canterbury.seng302.homehelper.service.ValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class ForgotPassWordEmailFormTest {
    @Autowired
    private ForgotPasswordEmailFormController forgotPasswordEmailFormController;

    private MockMvc mockMvc;

    @Autowired
    private ValidationService validationService;

    @PostConstruct
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(forgotPasswordEmailFormController).build();
    }

    @Test
    void testPostWithInvalidEmail() throws Exception {
        mockMvc.perform(post("/forgot-password")
                        .param("email", "invalid"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("email", "invalid"))
                .andExpect(model().attribute("emailErrorMessage", "Email address must be in the form 'jane@doe.nz'"));
    }

    @Test
    void testPostWithValidEmail() throws Exception {
        mockMvc.perform(post("/forgot-password")
                        .param("email", "test@doe.nz"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("confirmationMessage", "An email was sent to the address if it was recognised"));
    }
}
