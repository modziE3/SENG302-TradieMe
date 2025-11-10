package nz.ac.canterbury.seng302.homehelper.unit.controller;

import nz.ac.canterbury.seng302.homehelper.controller.quote.MyQuotesController;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.Quote;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MyQuotesControllerTest {
    @InjectMocks
    private MyQuotesController myQuotesController;
    @Mock
    private QuoteService quoteService;
    @Mock
    private RenovationRecordService renovationRecordService;
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

    @BeforeEach
    public void setUp() {
        User user = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        user.setId(1L);
        lenient().when(userService.getUser(any())).thenReturn(user);
        jobService = Mockito.mock(JobService.class);
        userService = Mockito.mock(UserService.class);
    }

    @Test
    public void getMyQuotesPage_MyQuotesPageTemplateReturned() {
        when(principal.getName()).thenReturn("jane@doe.nz");
        String viewName = myQuotesController.getMyQuotesPage(null, null, null, model, principal);
        assertEquals("myQuotesTemplate", viewName);
    }

    @Test
    public void acceptQuote_RedirectedToMyQuotesPage() {
        Quote quote = new Quote("13", "3", "jane@doe.nz", "123456789", "Stuff");
        Job job = new Job("Job", "Job", null, null);
        RenovationRecord renovationRecord = new RenovationRecord("Record1", "Record1", List.of(), "jane@doe.nz");
        User user = new User("J", "D", "john@doe.nz", "P4$$word", null, null);
        job.setRenovationRecord(renovationRecord);
        quote.setJob(job);
        quote.setUser(user);

        when(quoteService.findQuoteById(1L)).thenReturn(quote);
        when(principal.getName()).thenReturn("jane@doe.nz");

        String viewName = myQuotesController.acceptQuote(1, true, false, model, principal);
        assertEquals("redirect:/my-quotes", viewName);
    }
}
