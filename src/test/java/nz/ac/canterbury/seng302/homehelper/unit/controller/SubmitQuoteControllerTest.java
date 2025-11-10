package nz.ac.canterbury.seng302.homehelper.unit.controller;

import nz.ac.canterbury.seng302.homehelper.controller.quote.SubmitQuoteController;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.QuoteService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.ui.Model;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class SubmitQuoteControllerTest {
    @InjectMocks
    private SubmitQuoteController submitQuoteController;
    @Mock
    private QuoteService quoteService;
    @Mock
    private JobService jobService;
    @Mock
    private UserService userService;
    @Mock
    private Model model;
    @Mock
    private Principal principal;
    @Mock
    private EmailService emailService;
    @Mock
    private TaskScheduler taskScheduler;

    @Test
    public void getSubmitQuoteForm_SubmitQuoteFormTemplateReturned() {
        String viewName = submitQuoteController.getSubmitQuoteForm(1L, model);
        assertEquals("submitQuoteTemplate", viewName);
    }

    @Test
    public void postSubmitQuoteForm_PageIsRedirected() {
        String viewName = submitQuoteController.postSubmitQuoteForm(1L, "2", "2h", "jane@doe.nz",
                "1234", "hi", model, principal);
        assertEquals("redirect:/my-renovations/job-details?jobId=1&fromSearch=true", viewName);
    }
}
