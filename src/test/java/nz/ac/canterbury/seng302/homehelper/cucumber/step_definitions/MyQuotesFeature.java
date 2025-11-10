package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.controller.quote.MyQuotesController;
import nz.ac.canterbury.seng302.homehelper.controller.job.JobListingController;
import nz.ac.canterbury.seng302.homehelper.controller.quote.MyQuotesController;
import nz.ac.canterbury.seng302.homehelper.controller.quote.SubmitQuoteController;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.Quote;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.JobRepository;
import nz.ac.canterbury.seng302.homehelper.repository.QuoteRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class MyQuotesFeature {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private Principal principal;
    @Autowired
    private UserService userService;
    @Autowired
    private RenovationRecordService renovationRecordService;
    @Autowired
    private JobService jobService;
    @Autowired
    private QuoteRepository quoteRepository;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RenovationRecordRepository renovationRecordRepository;
    @Autowired
    private SubmitQuoteController submitQuoteController;
    @Autowired
    private MyQuotesController myQuotesController;
    @Autowired
    private JobListingController jobListingController;
    @Autowired
    private QuoteService quoteService;
    @Autowired
    private EmailService emailService;


    private Job postedJob1;
    private Job postedJob2;
    private RenovationRecord record1;
    private RenovationRecord record2;
    private User user1;
    private User user2;
    private Quote capturedQuote;
    private Quote quote;
    private MvcResult mvcResult;
    private Quote retractableQuote;

    @Before("@MyQuotes")
    public void beforeAll() {
        quoteRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();
        Mockito.reset(emailService);

        when(principal.getName()).thenReturn("jane@doe.nz");

        postedJob1 = new Job("Job1", "Job1", "02/02/2026", "01/01/2026");
        postedJob2 = new Job("Job2", "Job2", "02/02/2026", "01/01/2026");
        record1 = new RenovationRecord("Record1", "Record1", List.of(), "jane@doe.nz");
        record2 = new RenovationRecord("Record1", "Record1", List.of(), "calan@meechang.nz");
        user1 = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        user2 = new User("Calan", "Meechang", "calan@meechang.nz", "P4$$word", null, null);
        quote = new Quote("2", "10", "calan@meechange.nz", "123456789", "Test 2");
        userService.addUser(user1);
        userService.addUser(user2);
        postedJob1.setIsPosted(true);
        postedJob1.setExpenses(null);
        postedJob1.setId(1L);
        postedJob1.setRenovationRecord(record1);
        postedJob2.setIsPosted(true);
        postedJob2.setExpenses(null);
        postedJob2.setRenovationRecord(record2);
        postedJob2.setId(2L);
        quote.setUser(user2);
        quote.setJob(postedJob1);
        renovationRecordService.addRenovationRecord(record1);
        renovationRecordService.addRenovationRecord(record2);
        jobService.addJob(postedJob1);
        jobService.addJob(postedJob2);

        Random rand = new Random();
        for (int i = 1; i <= 100; i++) {
            try {
                Quote q = new Quote("" + rand.nextInt(1, 100), "" + rand.nextInt(1, 24), user1.getEmail(), "123456", "Quote");
                RenovationRecord record = renovationRecordService.getRenovationRecordsByOwner("calan@meechang.nz").getFirst();
                Job job = record.getJobs().getFirst();
                q.setJob(job);
                q.setUser(user1);
                q.setStatus(List.of("Pending", "Accepted", "Rejected").get(rand.nextInt(0,3)));
                quoteService.addQuote(q);
            } catch (Exception e) {}
        }
    }


    @Given("I am on the My Quotes Page")
    public void i_am_on_the_my_quotes_page() throws Exception {
        when(principal.getName()).thenReturn("jane@doe.nz");

        mockMvc = MockMvcBuilders.standaloneSetup(myQuotesController).build();

        mockMvc.perform(get("/my-quotes")
                        .principal(principal))
                .andExpect(view().name("myQuotesTemplate"));
    }

    @When("I have created a quote for a job")
    public void i_have_created_a_quote_for_a_job() throws Exception {
        when(principal.getName()).thenReturn("jane@doe.nz");

        mockMvc = MockMvcBuilders.standaloneSetup(submitQuoteController).build();

        mockMvc.perform(post("/submit-quote")
                .param("jobId", postedJob2.getId().toString())
                .param("price", "2")
                .param("workTime", "10")
                .param("email", "jane@doe.nz")
                .param("phoneNumber", "123456789")
                .param("description", "Test 1")
                .principal(principal));
    }

    @Then("I can see a list that contains my sent quote")
    public void i_can_see_a_list_that_contains_my_sent_quote() throws Exception {
        when(principal.getName()).thenReturn("jane@doe.nz");

        mockMvc = MockMvcBuilders.standaloneSetup(myQuotesController).build();

        MvcResult mvcResult = mockMvc.perform(get("/my-quotes")
                        .principal(principal))
                .andExpect(view().name("myQuotesTemplate"))
                .andReturn();

        List<Quote> quotes = (List<Quote>) mvcResult.getModelAndView().getModel().get("receivedQuotes");
        Assertions.assertNotNull(quotes);
    }

    @When("Another user creates a quote for a job I own")
    public void Another_user_creates_a_quote_for_a_job_i_own() throws Exception {
        when(principal.getName()).thenReturn("calan@meechang.nz");
        mockMvc = MockMvcBuilders.standaloneSetup(jobListingController).build();

        renovationRecordService.addRenovationRecord(record1);
        jobService.addJob(postedJob1);

        mockMvc.perform(post("/submit-quote")
                .param("jobId", postedJob1.getId().toString())
                .param("price", "2")
                .param("workTime", "10")
                .param("email", "calan@meechang.nz")
                .param("phoneNumber", "123456789")
                .param("description", "Test 2")
                .principal(principal));
    }

    @Then("I can see a list that contains that received quote")
    public void i_can_see_a_list_that_contains_that_received_quote() throws Exception {
        when(principal.getName()).thenReturn("jane@doe.nz");

        mockMvc = MockMvcBuilders.standaloneSetup(myQuotesController).build();

        MvcResult mvcResult = mockMvc.perform(get("/my-quotes")
                        .principal(principal))
                .andExpect(view().name("myQuotesTemplate"))
                .andReturn();

        List<Quote> quotes = (List<Quote>) mvcResult.getModelAndView().getModel().get("receivedQuotes");
        Assertions.assertNotNull(quotes);
    }

    @Then("I can accept the quote and retract the job posting")
    public void i_can_accept_the_quote_and_retract_the_job_posting() throws Exception {
        when(principal.getName()).thenReturn("jane@doe.nz");

        renovationRecordService.addRenovationRecord(record1);
        jobService.addJob(postedJob1);
        userService.addUser(user2);
        quoteService.addQuote(quote);

        mockMvc = MockMvcBuilders.standaloneSetup(myQuotesController).build();


        mockMvc.perform(post("/accept-quote")
                        .param("quoteId", quote.getId().toString())
                        .param("retract", "true")
                        .principal(principal))
                .andExpect(status().is3xxRedirection());

        Quote updatedQuote = quoteService.findQuoteById(quote.getId());
        Assertions.assertEquals("Accepted", updatedQuote.getStatus());
        Assertions.assertFalse(updatedQuote.getJob().getIsPosted());
    }

    @When("I filter quotes by {string}")
    public void i_filter_quotes_by_pending(String string) throws Exception {
        when(principal.getName()).thenReturn("jane@doe.nz");

        mockMvc = MockMvcBuilders.standaloneSetup(myQuotesController).build();

        mvcResult = mockMvc.perform(get("/my-quotes")
                        .param("status", string)
                        .principal(principal))
                .andExpect(view().name("myQuotesTemplate"))
                .andReturn();
    }

    @Then("I can see only quotes with that {string}")
    public void i_can_see_only_quotes_with_that_pending(String string) throws Exception {
        List<Quote> quotes = (List<Quote>) mvcResult.getModelAndView().getModel().get("sentQuotes");
        Assertions.assertNotNull(quotes);
        for (Quote quote : quotes) {
            assertEquals(string.toLowerCase(), quote.getStatus().toLowerCase());
        }
    }

    @Given("I am a logged in user on the My Quotes page")
    public void I_am_a_logged_in_user_on_the_My_Quotes_page() throws Exception {
        when(principal.getName()).thenReturn("jane@doe.nz");
        mockMvc = MockMvcBuilders.standaloneSetup(myQuotesController).build();
        mockMvc.perform(get("/my-quotes")
                        .principal(principal))
                .andExpect(view().name("myQuotesTemplate"));
    }

    @And("I have sent multiple quotes")
    public void i_have_sent_multiple_quotes() {
        user1 = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        userRepository.save(user1);
        record1 = new RenovationRecord("Record1", "Record1", List.of(), "jane@doe.nz");
        renovationRecordService.addRenovationRecord(record1);
        postedJob1 = new Job("Job1", "Job1", "02/02/2026", "01/01/2026");
        postedJob1.setIsPosted(true);
        postedJob1.setExpenses(null);
        postedJob1.setRenovationRecord(record1);
        jobService.addJob(postedJob1);

        Random rand = new Random();
        for (int i = 1; i <= 10; i++) {
            try {
                Quote q = new Quote("" + rand.nextInt(1, 100), "" + rand.nextInt(1, 24), user1.getEmail(), "123456", "Quote" + i);
                q.setJob(postedJob1);
                q.setUser(user1);
                q.setStatus(List.of("Pending", "Accepted", "Rejected").get(rand.nextInt(0,3)));
                quoteRepository.save(q);
            } catch (Exception e) {}
        }
    }

    @When("I retract a quote")
    public void i_retract_a_quote() {
        mockMvc = MockMvcBuilders.standaloneSetup(myQuotesController).build();
        String status = "Accepted";
        List<Quote> quotes = quoteRepository.findSentQuotes(user1.getEmail(), status.toLowerCase());
        retractableQuote = quotes.getFirst();
        quoteService.retractQuote(retractableQuote);
    }

    @Then("It is removed from the system")
    public void it_is_removed_from_the_system() {
        boolean exists = quoteRepository.existsById(retractableQuote.getId());
        Assertions.assertFalse(exists, "Failed to remove quote.");
    }


    @Then("I can reject a quote")
    public void i_can_reject_a_quote() throws Exception {
        when(principal.getName()).thenReturn("jane@doe.nz");
        Quote quote = new Quote("10", "10", "calan@meechang.nz", "1234567890", "quote");
        quote.setUser(user2);
        quote.setJob(postedJob1);
        userRepository.save(user2);
        jobService.addJob(postedJob1);
        quote = quoteRepository.save(quote);


        mockMvc = MockMvcBuilders.standaloneSetup(myQuotesController).build();
        MvcResult mvcResult = mockMvc.perform(post("/reject-quote")
                        .param("quoteId", quote.getId().toString())
                        .principal(principal))
                .andExpect(view().name("redirect:/my-quotes"))
                .andReturn();

        Assertions.assertEquals("Rejected", quoteService.findQuoteById(quote.getId()).getStatus());

    }

    @When("I reject a quote")
    public void i_reject_a_quote() throws Exception {
        when(principal.getName()).thenReturn("jane@doe.nz");
        Quote quote = new Quote("10", "10", "calan@meechang.nz", "1234567890", "quote");
        quote.setUser(user2);
        quote.setJob(postedJob1);
        userRepository.save(user2);
        jobService.addJob(postedJob1);
        quote = quoteRepository.save(quote);


        mockMvc = MockMvcBuilders.standaloneSetup(myQuotesController).build();
        MvcResult mvcResult = mockMvc.perform(post("/reject-quote")
                        .param("quoteId", quote.getId().toString())
                        .principal(principal))
                .andExpect(view().name("redirect:/my-quotes"))
                .andReturn();

        Assertions.assertEquals("Rejected", quoteService.findQuoteById(quote.getId()).getStatus());
    }

    @Then("The quote sender is sent an email")
    public void the_quote_sender_is_sent_an_email() throws Exception {
        await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        Mockito.verify(emailService, Mockito.times(1)).sendQuoteRejectedEmail(any(), any())
                );
    }

}