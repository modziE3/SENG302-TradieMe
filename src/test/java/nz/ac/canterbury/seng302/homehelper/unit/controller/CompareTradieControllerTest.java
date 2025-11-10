package nz.ac.canterbury.seng302.homehelper.unit.controller;

import nz.ac.canterbury.seng302.homehelper.controller.quote.CompareTradieController;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.Quote;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompareTradieControllerTest {

    private CompareTradieController controller;


    @Mock private JobService jobService;
    @Mock private RenovationRecordService renovationRecordService;
    @Mock private UserService userService;
    @Mock private QuoteService quoteService;

    private Model model;
    private Principal principal;
    private RenovationRecord testRecord;
    private Job testJob;
    private User testUser;
    private User quote1Sender;
    private User quote2Sender;
    private Quote quote1;
    private Quote quote2;

    @BeforeEach
    public void setup() {
        model = mock(Model.class);
        principal = mock(Principal.class);
        jobService = mock(JobService.class);
        renovationRecordService = mock(RenovationRecordService.class);
        userService = mock(UserService.class);
        quoteService = mock(QuoteService.class);

        when(principal.getName()).thenReturn("jane@doe.nz");

        testRecord = new RenovationRecord("Record", "Record", List.of(), "jane@doe.nz");
        testRecord.setId(1L);
        testJob = new Job("Job", "Job", null, null);
        testJob.setId(1L);
        testJob.setRenovationRecord(testRecord);
        testJob.setIsPosted(true);
        testRecord.setJobs(List.of(testJob));
        testUser = new User("J", "D", "jane@doe.nz", "P4$$word", null, null);

        quote1Sender = new User("1", "last", "1@doe.nz", "P4$$word", null, null);
        quote2Sender = new User("2", "last", "2@doe.nz", "P4$$word", null, null);
        quote1 = new Quote("", "", quote1Sender.getEmail(), "", "");
        quote1.setId(1L);
        quote1.setUser(quote1Sender);
        quote1.setJob(testJob);
        quote2 = new Quote("", "", quote2Sender.getEmail(), "", "");
        quote2.setId(2L);
        quote2.setUser(quote2Sender);
        quote2.setJob(testJob);
        List<Quote> quotes = new ArrayList<>();
        quotes.add(quote1);
        quotes.add(quote2);
        testJob.setQuotes(quotes);

        controller = new CompareTradieController(jobService, renovationRecordService, userService, quoteService);
    }

    @Test
    public void getCompareTradiesPage_UserIsOwner_IAmShownTheComparePage() {
        when(principal.getName()).thenReturn("jane@doe.nz");
        when(userService.getUser(testUser.getEmail())).thenReturn(testUser);
        when(jobService.getJobById(testJob.getId())).thenReturn(testJob);

        String result = controller.getCompareTradiesPage(testJob.getId(), principal, model);

        Assertions.assertEquals("compareTradiesTemplate", result);
    }

    @Test
    public void getCompareTradiesPage_UserIsNotOwner_IAmShownTheNotFoundPage() {
        when(principal.getName()).thenReturn("1@doe.nz");
        when(userService.getUser(quote1Sender.getEmail())).thenReturn(quote1Sender);
        when(jobService.getJobById(testJob.getId())).thenReturn(testJob);

        String result = controller.getCompareTradiesPage(testJob.getId(), principal, model);

        Assertions.assertEquals("error", result);
    }

    @Test
    public void rejectTradie_QuoteIsForJobOwnedByUser_QuoteIsRejectedAndResponseIsOk() {
        when(principal.getName()).thenReturn("jane@doe.nz");
        when(userService.getUser(testUser.getEmail())).thenReturn(testUser);
        when(quoteService.findQuoteById(quote1.getId())).thenReturn(quote1);

        ResponseEntity<?> response = controller.rejectTradie(model, principal, "left", quote1.getId());

        verify(quoteService, times(1)).rejectQuote(quote1);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        Assertions.assertEquals(body.get("quoteId"), quote1.getId());
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    public void rejectTradie_QuoteIsForJobNotOwnedByUser_QuoteIsNotRejectedAndResponseIsBad() {
        when(principal.getName()).thenReturn("2@doe.nz");
        when(userService.getUser(quote2Sender.getEmail())).thenReturn(quote2Sender);
        when(quoteService.findQuoteById(quote1.getId())).thenReturn(quote1);

        ResponseEntity<?> response = controller.rejectTradie(model, principal, "left", quote1.getId());

        verify(quoteService, times(0)).rejectQuote(quote1);
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("does not belong to the user"));
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    public void rejectTradie_InvalidSide_QuoteIsNotRejectedAndResponseIsBad() {
        when(principal.getName()).thenReturn("jane@doe.nz");
        when(userService.getUser(testUser.getEmail())).thenReturn(testUser);
        when(quoteService.findQuoteById(quote1.getId())).thenReturn(quote1);

        ResponseEntity<?> response = controller.rejectTradie(model, principal, "not left or right", quote1.getId());

        verify(quoteService, times(0)).rejectQuote(quote1);
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Side is not valid (not 'left', 'right', 'top' or 'bottom')"));
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    public void rejectTradie_QuoteStatusSetToRejected() {
        when(principal.getName()).thenReturn("jane@doe.nz");
        when(userService.getUser(testUser.getEmail())).thenReturn(testUser);
        when(quoteService.findQuoteById(quote1.getId())).thenReturn(quote1);

        doAnswer(invocation -> {
            Quote q = invocation.getArgument(0);
            q.setStatus("Rejected");
            return null;
        }).when(quoteService).rejectQuote(quote1);

        ResponseEntity<?> response = controller.rejectTradie(model, principal, "left", quote1.getId());
        verify(quoteService, times(1)).rejectQuote(quote1);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        Assertions.assertEquals(body.get("remainingQuotes"), 0);
        Assertions.assertEquals(body.get("quoteId"), quote1.getId());
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals("Rejected", quote1.getStatus());
    }

    @Test
    public void lastQuote_StatusSetToAccepted() {
        when(principal.getName()).thenReturn("jane@doe.nz");
        when(userService.getUser(testUser.getEmail())).thenReturn(testUser);
        when(jobService.getJobById(testJob.getId())).thenReturn(testJob);
        List<RenovationRecord> recordsList = new ArrayList<>();
        recordsList.add(testRecord);
        when(renovationRecordService.getRenovationRecordsByOwner(testUser.getEmail())).thenReturn(recordsList);
        when(quoteService.getQuotesByJobIdFilteredStatus(testJob.getId(), "Pending")).thenReturn(List.of(quote1));

        doAnswer(invocation -> {
            Quote q = invocation.getArgument(0);
            q.setStatus("Accepted");
            return null;
        }).when(quoteService).acceptQuote(quote1);

        String view = controller.acceptLastTradie(testJob.getId(), model, principal);
        Assertions.assertEquals("redirect:/my-renovations/job-details?jobId=" + testJob.getId(), view);
        verify(quoteService, times(1)).acceptQuote(quote1);
        Assertions.assertEquals("Accepted", quote1.getStatus());

    }
}
