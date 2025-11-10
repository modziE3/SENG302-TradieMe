package nz.ac.canterbury.seng302.homehelper.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nz.ac.canterbury.seng302.homehelper.controller.job.JobDetailsController;
import nz.ac.canterbury.seng302.homehelper.entity.*;
import nz.ac.canterbury.seng302.homehelper.repository.*;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class JobDetailsControllerIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(JobDetailsControllerIntegrationTest.class);
    @Autowired
    private JobDetailsController jobDetailsController;
    @Autowired
    private JobService jobService;
    @Autowired
    private RenovationRecordService renovationRecordService;
    @Autowired
    private UserService userService;
    @Autowired
    private ExpenseService expenseService;
    @Autowired
    private RatingService ratingService;
    @MockBean
    private QuoteService quoteService;
    @MockBean
    private JobRepository jobRepository;
    @MockBean
    private RenovationRecordRepository renovationRecordRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private QuoteRepository quoteRepository;
    @MockBean
    private ExpenseRepository expenseRepository;
    @MockBean
    private RatingRepository ratingRepository;
    @MockBean
    private Principal principal;


    private MockMvc mockMvc;
    private RenovationRecord renovationRecord;
    private User user1;
    private Job job;
    private User user2;
    private Quote johnQuote;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(jobDetailsController).build();
        user1 = new User("John", "Doe", "john@doe.nz", "P4$$word", null, null);
        user1.setId(1L);
        when(principal.getName()).thenReturn("john@doe.nz");

        when(userRepository.findByEmailContainingIgnoreCase(user1.getEmail())).thenReturn(user1);
        when(userRepository.findUserById(user1.getId())).thenReturn(user1);
        when(userRepository.findById(user1.getId())).thenReturn(Optional.ofNullable(user1));

        renovationRecord = new RenovationRecord("Record", "Record", new ArrayList<>(), user1.getEmail());
        job = new Job("Job", "Job", null, null);
        job.setId(1L);
        job.setRenovationRecord(renovationRecord);
        job.setIsPosted(true);
        renovationRecord.setJobs(List.of(job));

        for (int i = 1; i <= 150; i++) {
            Expense expense = new Expense("25", "Example expense description " + (i), "Material", "19/07/2025");
            job.addExpense(expense);
        }

        user2 = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        user2.setId(2L);
        johnQuote = new Quote("50", "12" , user1.getEmail(), "123456", "Quote");
        johnQuote.setJob(job);
        johnQuote.setUser(user2);
        johnQuote.setStatus("Pending");

        when(userRepository.findByEmailContainingIgnoreCase(user2.getEmail())).thenReturn(user2);
        when(userRepository.findUserById(user2.getId())).thenReturn(user2);

        when(renovationRecordRepository.findRenovationRecordsByEmail(Mockito.anyString())).thenReturn(List.of(renovationRecord));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
        when(quoteService.checkIfAlreadyQuoted(any(), any())).thenReturn(false);
        Mockito.doNothing().when(quoteService).setQuoteAsRated(any(), any());
    }

    @Test
    public void getJobDetailsPage_JobExists_JobDetailsPageReturned() throws Exception {
        user1.setId(123L);

        mockMvc.perform(get("/my-renovations/job-details")
                        .param("jobId", job.getId().toString())
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("jobDetailsTemplate"))
                .andExpect(model().attribute("job", job))
                .andExpect(model().attribute("renovationRecord", renovationRecord))
                .andExpect(model().attribute("icons", JobService.ICON_LIST))
                .andExpect(model().attribute("renovationRecords", List.of(renovationRecord)))
                .andExpect(model().attribute("loggedIn", true));
    }

    @Test
    public void getJobDetailsPage_JobDoesNotExist_ErrorPageReturned() throws Exception {
        when(jobRepository.findById(Mockito.anyLong())).thenReturn(null);

        mockMvc.perform(get("/my-renovations/job-details")
                        .param("jobId", "1")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("renovationRecords", List.of(renovationRecord)))
                .andExpect(model().attribute("loggedIn", true));
    }

    @Test
    public void getJobDetailsPage_PageDoesNotExist_ErrorPageReturned() throws Exception {
        when(jobRepository.findById(Mockito.anyLong())).thenReturn(null);

        mockMvc.perform(get("/my-renovations/job-details")
                        .param("jobId", "1")
                        .param("expensesPage", "25")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("renovationRecords", List.of(renovationRecord)))
                .andExpect(model().attribute("loggedIn", true));
    }

    @Test
    public void userInputtedPageForExpensesTab_ValidInput_RedirectsToPaginatedExpensesPage() throws Exception {
        mockMvc.perform(post("/my-renovations/job-details")
                        .param("jobId", "1")
                        .param("expensesPage", "1")
                        .param("quotesPage", "1")
                        .param("expensesPageNumber", "3")
                        .param("fromSearch", String.valueOf(false))
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/my-renovations/job-details?jobId=1&fromSearch=false&expensesPage=3&quotesPage=1"));
    }

    @Test
    public void userInputtedPageForQuotesTab_ValidInput_RedirectsToPaginatedQuotesPage() throws Exception {
        mockMvc.perform(post("/my-renovations/job-details")
                        .param("jobId", "1")
                        .param("expensesPage", "1")
                        .param("quotesPage", "1")
                        .param("quotesPageNumber", "3")
                        .param("fromSearch", String.valueOf(false))
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/my-renovations/job-details?jobId=1&fromSearch=false&expensesPage=1&quotesPage=3"));
    }

    static Stream<Arguments> jobStatuses() {
        return Stream.of(
                Arguments.of("Not Started"),
                Arguments.of("In Progress"),
                Arguments.of("Completed"),
                Arguments.of("Cancelled"),
                Arguments.of("Blocked")
        );
    }
    @ParameterizedTest
    @MethodSource("jobStatuses")
    public void updateJobStatus_NewStatusSet_JobStatusChangedToNewStatus(String jobStatus) throws Exception {
        Mockito.when(jobRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(job));

        mockMvc.perform(post("/my-renovations/update-job-status")
                        .param("jobId", "1")
                        .param("newJobStatus", jobStatus).principal(principal))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        Mockito.verify(jobRepository).save(jobCaptor.capture());
        job = jobCaptor.getValue();
        Assertions.assertEquals(jobStatus, job.getStatus());
    }

    @Test
    public void acceptQuote_WithRetract_RedirectedToMyQuotesPage() throws Exception {
        when(quoteService.findQuoteById(1L)).thenReturn((johnQuote));

        mockMvc.perform(post("/my-renovations/job-details/accept-quote")
                        .param("quoteId", "1")
                        .param("retract", "true")
                        .param("jobId", "1")
                        .param("quotesPage", "1")
                        .param("expensesPage", "1")
                        .param("fromSearch", "false")
                        .principal(principal))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<Quote> quoteCaptor = ArgumentCaptor.forClass(Quote.class);
        Mockito.verify(quoteService).addQuote(quoteCaptor.capture());
        Quote capturedQuote = quoteCaptor.getValue();
        assertEquals("Accepted", capturedQuote.getStatus());
        Assertions.assertFalse(capturedQuote.getJob().getIsPosted());
    }

    @Test
    public void acceptQuote_WithoutRetract_RedirectedToMyQuotesPage() throws Exception {
        when(quoteService.findQuoteById(1L)).thenReturn((johnQuote));

        mockMvc.perform(post("/my-renovations/job-details/accept-quote")
                        .param("quoteId", "1")
                        .param("retract", "false")
                        .param("jobId", "1")
                        .param("quotesPage", "1")
                        .param("expensesPage", "1")
                        .param("fromSearch", "false")
                        .principal(principal))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<Quote> quoteCaptor = ArgumentCaptor.forClass(Quote.class);
        Mockito.verify(quoteService).addQuote(quoteCaptor.capture());
        Quote capturedQuote = quoteCaptor.getValue();
        assertEquals("Accepted", capturedQuote.getStatus());
        Assertions.assertTrue(capturedQuote.getJob().getIsPosted());
    }

    @Test
    public void rejectQuotesTest() throws Exception {
        User johnUser = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        Quote johnQuote = new Quote("50", "12" , user1.getEmail(), "123456", "Quote");
        johnQuote.setJob(job);
        johnQuote.setUser(johnUser);
        johnQuote.setStatus("Pending");


        when(quoteService.findQuoteById(1L)).thenReturn((johnQuote));

        MvcResult mvcResult = mockMvc.perform(post("/my-renovations/job-details/reject-quote")
                        .param("quoteId", "1")
                        .param("jobId", "1")
                        .param("quotesPage", "1")
                        .param("expensesPage", "1")
                        .param("fromSearch", "false")
                        .principal(principal))
                .andExpect(view().name("redirect:/my-renovations/job-details?jobId=1&fromSearch=false&expensesPage=1&quotesPage=1#nav-quotes"))
                .andReturn();

        ArgumentCaptor<Quote> quoteCaptor = ArgumentCaptor.forClass(Quote.class);
        Mockito.verify(quoteService).addQuote(quoteCaptor.capture());

        Quote quote = quoteCaptor.getValue();
        assertEquals("rejected", quote.getStatus().toLowerCase());
    }

    @Test
    public void acceptQuote_WithoutRetractWithExpenseTransfer_RedirectedToMyQuotesPageExpenseCreated() throws Exception {
        when(quoteService.findQuoteById(1L)).thenReturn((johnQuote));

        mockMvc.perform(post("/my-renovations/job-details/accept-quote")
                        .param("quoteId", "1")
                        .param("retract", "false")
                        .param("transfer", "true")
                        .param("jobId", "1")
                        .param("quotesPage", "1")
                        .param("expensesPage", "1")
                        .param("fromSearch", "false")
                        .principal(principal))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<Expense> expenseArgumentCaptor = ArgumentCaptor.forClass(Expense.class);
        Mockito.verify(expenseRepository, Mockito.times(1)).save(expenseArgumentCaptor.capture());
        Expense expense = expenseArgumentCaptor.getValue();
        assertEquals(johnQuote.getPrice(), expense.getCost());
        assertEquals("Quote", expense.getCategory());
        assertEquals("Quote from Jane Doe", expense.getDescription());
    }


    static Stream<Integer> validRatingValues() {
        return Stream.of(
                1,2,3,4,5
        );
    }
    @ParameterizedTest
    @MethodSource("validRatingValues")
    public void rateTradie_OneValidRatingJsonGiven_RatingCreated(Integer ratingValue) throws Exception {
        String ratingJson = "[{\"tradieId\": \""+user2.getId()+"\", \"rating\": \""+ratingValue+"\"}]";
        mockMvc.perform(post("/rate-tradie")
                    .param("ratings", ratingJson)
                    .param("jobId", "1")
                    .principal(principal))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<Rating> ratingArgumentCaptor = ArgumentCaptor.forClass(Rating.class);
        Mockito.verify(ratingRepository, Mockito.times(1)).save(ratingArgumentCaptor.capture());
        Rating rating = ratingArgumentCaptor.getValue();
        assertEquals(ratingValue, rating.getRating());
        assertEquals(user1, rating.getSendingUser());
        assertEquals(user2, rating.getReceivingUser());
    }

    @Test
    public void rateTradie_MultipleValidRatingsJsonGiven_RatingsCreated() throws Exception {
        Integer ratingValue = 1;
        User user3 = new User("Calan", "Doe", "calan@doe.nz", "P4$$word", null, null);
        user3.setId(3L);
        User user4 = new User("Luke", "Doe", "luke@doe.nz", "P4$$word", null, null);
        user4.setId(4L);
        User user5 = new User("Fergus", "Doe", "fergus@doe.nz", "P4$$word", null, null);
        user5.setId(5L);
        User user6 = new User("Dury", "Doe", "dury@doe.nz", "P4$$word", null, null);
        user6.setId(6L);
        User user7 = new User("Alex", "Doe", "alex@doe.nz", "P4$$word", null, null);
        user7.setId(7L);
        User user8 = new User("Morgan", "Doe", "morgan@doe.nz", "P4$$word", null, null);
        user8.setId(8L);
        List<User> users = Arrays.asList(user3, user4, user5, user6, user7, user8);
        for (User user : users) {
            when(userRepository.findUserById(user.getId())).thenReturn(user);
        }
        String ratingJson = "[{\"tradieId\": \""+user3.getId()+"\", \"rating\": \""+ratingValue+"\"}";
        for (User user : users.subList(1, users.size())) {
            ratingJson += ",{\"tradieId\": \""+user.getId()+"\", \"rating\": \""+ratingValue+"\"}";
        }
        ratingJson += "]";

        mockMvc.perform(post("/rate-tradie")
                        .param("ratings", ratingJson)
                        .param("jobId", "1")
                        .principal(principal))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<Rating> ratingArgumentCaptor = ArgumentCaptor.forClass(Rating.class);
        Mockito.verify(ratingRepository, Mockito.times(users.size())).save(ratingArgumentCaptor.capture());
        List<Rating> ratings = ratingArgumentCaptor.getAllValues();
        for (int i = 0; i < ratings.size(); i++) {
            assertEquals(ratingValue, ratings.get(i).getRating());
            assertEquals(user1, ratings.get(i).getSendingUser());
            assertEquals(users.get(i), ratings.get(i).getReceivingUser());
        }
    }
}
