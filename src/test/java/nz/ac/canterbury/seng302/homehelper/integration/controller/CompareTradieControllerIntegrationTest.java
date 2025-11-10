package nz.ac.canterbury.seng302.homehelper.integration.controller;

import nz.ac.canterbury.seng302.homehelper.controller.quote.CompareTradieController;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.Quote;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.JobRepository;
import nz.ac.canterbury.seng302.homehelper.repository.QuoteRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class CompareTradieControllerIntegrationTest {
    @Autowired
    private CompareTradieController compareTradieController;
    @Autowired
    private JobService jobService;
    @Autowired
    private RenovationRecordService renovationRecordService;
    @Autowired
    private UserService userService;
    @MockBean
    private JobRepository jobRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private Principal principal;
    @MockBean
    private QuoteRepository quoteRepository;

    private MockMvc mockMvc;
    private RenovationRecord testRecord;
    private Job testJob;
    private User testUser;
    private User quote1Sender;
    private User quote2Sender;
    private User quote3Sender;
    private Quote quote1;
    private Quote quote2;
    private Quote quote3;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(compareTradieController).build();
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
        quote3Sender = new User("3", "last", "3@doe.nz", "P4$$word", null, null);
        quote1 = new Quote("10", "15", quote1Sender.getEmail(), "", "");
        quote1.setUser(quote1Sender);
        quote2 = new Quote("10", "15", quote2Sender.getEmail(), "", "");
        quote2.setUser(quote2Sender);
        quote3 = new Quote("10", "15", quote3Sender.getEmail(), "", "");
        quote3.setUser(quote3Sender);
        List<Quote> quotes = new ArrayList<>();
        quotes.add(quote1);
        quotes.add(quote2);
        quotes.add(quote3);
        testJob.setQuotes(quotes);

        when(jobRepository.findById(testJob.getId())).thenReturn(Optional.ofNullable(testJob));
        when(userRepository.findByEmailContainingIgnoreCase(testUser.getEmail())).thenReturn(testUser);
    }

    @Test
    public void getCompareTradiesPage_ValidJobId_PageReturned() throws Exception {
        mockMvc.perform(get("/my-renovations/job-details/compare-tradies")
                    .param("jobId", "1")
                    .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("compareTradiesTemplate"))
                .andExpect(model().attribute("user", testUser))
                .andExpect(model().attribute("job", testJob))
                .andExpect(model().attribute("tradie1", quote1Sender))
                .andExpect(model().attribute("quote1", quote1))
                .andExpect(model().attribute("tradie2", quote2Sender))
                .andExpect(model().attribute("quote2", quote2));
    }

    @Test
    public void getCompareTradiesPage_InvalidJobId_ErrorPageReturned() throws Exception {
        mockMvc.perform(get("/my-renovations/job-details/compare-tradies")
                        .param("jobId", "1111111111111")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    public void getCompareTradiesPage_JobIdOfDifferentUser_ErrorPageReturned() throws Exception {
        User newUser = new User("J", "D", "john@doe.nz", "P4$$word", null, null);
        when(principal.getName()).thenReturn("john@doe.nz");
        when(userRepository.findByEmailContainingIgnoreCase(newUser.getEmail())).thenReturn(newUser);

        mockMvc.perform(get("/my-renovations/job-details/compare-tradies")
                        .param("jobId", "1")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    public void getCompareTradiesPage_OneQuoteIsNotPending_OnlyTradiesWithPendingQuotesAppear() throws Exception {
        quote2.setStatus("Rejected");

        mockMvc.perform(get("/my-renovations/job-details/compare-tradies")
                        .param("jobId", "1")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("compareTradiesTemplate"))
                .andExpect(model().attribute("user", testUser))
                .andExpect(model().attribute("job", testJob))
                .andExpect(model().attribute("tradie1", quote1Sender))
                .andExpect(model().attribute("quote1", quote1))
                .andExpect(model().attribute("tradie2", quote3Sender))
                .andExpect(model().attribute("quote2", quote3));
    }

    @Test
    public void getNextTrade_ValidTradiesAndQuotesLeftSide_ReturnFragment() throws Exception {
        when(userRepository.findUserById(1L)).thenReturn(quote1Sender);
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote1));
        when(quoteRepository.findById(2L)).thenReturn(Optional.of(quote2));

        mockMvc.perform(get("/my-renovations/job-details/compare-tradies/get-next-tradie")
                .param("tradieIds", "1")
                .param("quoteIds", "1")
                .param("side", "left")
                .param("oldQuoteId", "2"))
                .andExpect(view().name("fragments/tradieCard :: tradieCard"))
                .andExpect(model().attribute("tradie", quote1Sender))
                .andExpect(model().attribute("quote", quote1))
                .andExpect(model().attribute("isLeft", true));
    }

    @Test
    public void getNextTrade_ValidTradiesAndQuotesRightSide_ReturnFragment() throws Exception {
        when(userRepository.findUserById(1L)).thenReturn(quote1Sender);
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote1));
        when(quoteRepository.findById(2L)).thenReturn(Optional.of(quote2));

        mockMvc.perform(get("/my-renovations/job-details/compare-tradies/get-next-tradie")
                        .param("tradieIds", "1")
                        .param("quoteIds", "1")
                        .param("side", "right")
                        .param("oldQuoteId", "2"))
                .andExpect(view().name("fragments/tradieCard :: tradieCard"))
                .andExpect(model().attribute("tradie", quote1Sender))
                .andExpect(model().attribute("quote", quote1))
                .andExpect(model().attribute("isLeft", false));
    }

    @Test
    public void getNextTrade_QuoteNotLinkedToTradieLeftSide_ReturnFragment() throws Exception {
        when(userRepository.findUserById(2L)).thenReturn(quote2Sender);
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote1));
        when(quoteRepository.findById(2L)).thenReturn(Optional.of(quote2));

        mockMvc.perform(get("/my-renovations/job-details/compare-tradies/get-next-tradie")
                        .param("tradieIds", "1")
                        .param("quoteIds", "1")
                        .param("side", "right")
                        .param("oldQuoteId", "2"))
                .andExpect(view().name("No More Tradies To Compare"));
    }
}
