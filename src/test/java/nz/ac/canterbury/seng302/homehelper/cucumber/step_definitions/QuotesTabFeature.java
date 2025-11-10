package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.controller.job.JobDetailsController;
import nz.ac.canterbury.seng302.homehelper.entity.*;
import nz.ac.canterbury.seng302.homehelper.repository.JobRepository;
import nz.ac.canterbury.seng302.homehelper.repository.QuoteRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class QuotesTabFeature {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private Principal principal;
    @Autowired
    private JobDetailsController jobDetailsController;
    @Autowired
    private JobService jobService;
    @Autowired
    private RenovationRecordService renovationRecordService;
    @Autowired
    private UserService userService;
    @Autowired
    private QuoteService quoteService;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private QuoteRepository quoteRepository;
    @Autowired
    private RenovationRecordRepository renovationRecordRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ExpenseService expenseService;


    private Job postedJob1;
    private Job postedJob2;
    private RenovationRecord record1;
    private RenovationRecord record2;
    private User user1;
    private User user2;
    private MvcResult mvcResult;
    private Quote quote1;

    @Before("@JobDetailsTab")
    public void beforeEach() {
        Mockito.reset(emailService);
        quoteRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();
        renovationRecordRepository.deleteAll();

        when(principal.getName()).thenReturn("jane@doe.nz");

        postedJob1 = new Job("Job1", "Job1", "02/02/2026", "01/01/2026");
        postedJob2 = new Job("Job2", "Job2", "02/02/2026", "01/01/2026");
        record1 = new RenovationRecord("Record1", "Record1", List.of(), "jane@doe.nz");
        record2 = new RenovationRecord("Record1", "Record1", List.of(), "calan@meechang.nz");
        user1 = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        user2 = new User("Calan", "Meechang", "calan@meechang.nz", "P4$$word", null, null);
        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);
        postedJob1.setIsPosted(true);
        postedJob1.setExpenses(null);
        postedJob1.setRenovationRecord(record1);
        postedJob2.setIsPosted(true);
        postedJob2.setExpenses(null);
        postedJob2.setRenovationRecord(record2);
        record1 = renovationRecordRepository.save(record1);
        record2 = renovationRecordRepository.save(record2);
        postedJob1 = jobRepository.save(postedJob1);
        postedJob2 = jobRepository.save(postedJob2);
        quote1 = new Quote("50", "12", user2.getEmail(), "123456", "Quote");
        quote1.setJob(postedJob1);
        quote1.setUser(user2);
        quote1.setStatus("Pending");
        quote1 = quoteRepository.save(quote1);


        Random rand = new Random();
        for (int i = 1; i <= 100; i++) {
            try {
                Quote q = new Quote("" + rand.nextInt(1, 100), "" + rand.nextInt(1, 24), user1.getEmail(), "123456", "Quote");
                q.setJob(postedJob2);
                q.setUser(user1);
                q.setStatus(List.of("Pending", "Accepted", "Rejected").get(rand.nextInt(0,3)));
                quoteService.addQuote(q);
            } catch (Exception e) {}
        }
        jobRepository.flush();
        quoteRepository.flush();
    }

    @Given("I am on the details page of a job I've posted")
    public void i_am_on_the_details_page_of_a_job_i_ve_posted() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(jobDetailsController).build();
        mockMvc.perform((get("/my-renovations/job-details"))
                        .param("jobId", postedJob2.getId().toString())
                        .param("fromSearch", "false")
                        .param("fromCalendar", "false")
                        .param("expensesPage", "1")
                        .param("quotesPage", "1")
                    .principal(principal))
                .andExpect(view().name("jobDetailsTemplate"));
    }

    @When("I click on the quotes tab")
    public void i_click_on_the_quotes_tab() throws Exception {
        mvcResult = mockMvc.perform((get("/my-renovations/job-details#nav-calendar"))
                        .param("jobId", postedJob2.getId().toString())
                        .principal(principal))
                .andExpect(view().name("jobDetailsTemplate"))
                .andReturn();
    }

    @Then("I can see all the quotes which have been offered")
    public void i_can_see_all_the_quotes_which_have_been_offered() {
        List<Quote> quotes = (List<Quote>) mvcResult.getModelAndView().getModel().get("receivedQuotes");
        Assertions.assertNotNull(quotes);
    }

    @Then("I can accept a quote on the tab")
    public void i_can_accept_a_quote_on_the_tab() throws Exception {
        mockMvc.perform(post("/my-renovations/job-details/accept-quote")
                        .param("quoteId", quote1.getId().toString())
                        .param("retract", "true")
                        .param("jobId", postedJob1.getId().toString())
                        .param("fromSearch", "false")
                        .param("expensesPage", "1")
                        .param("quotesPage", "1")
                        .principal(principal))
                .andExpect(status().is3xxRedirection());

        Assertions.assertEquals("Accepted", quoteService.findQuoteById(quote1.getId()).getStatus());
        Assertions.assertFalse(jobService.getJobById(postedJob1.getId()).getIsPosted());
    }

    @When("I accept a quote on the tab")
    public void i_accept_a_quote_on_the_tab() throws Exception {
        mockMvc.perform(post("/my-renovations/job-details/accept-quote")
                        .param("quoteId", quote1.getId().toString())
                        .param("retract", "true")
                        .param("jobId", postedJob1.getId().toString())
                        .param("fromSearch", "false")
                        .param("expensesPage", "1")
                        .param("quotesPage", "1")
                        .principal(principal))
                .andExpect(status().is3xxRedirection());
    }

    @Then("The quote tab sender is sent an accepted email")
    public void the_quote_tab_sender_is_sent_an_accepted_email() throws Exception {
        await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        Mockito.verify(emailService, Mockito.times(1)).sendQuoteAcceptedEmail(any(), any())
                );
    }


    @Then("I can reject a quote on the tab")
    public void i_can_reject_a_quote_on_the_tab() throws Exception {
        mvcResult = mockMvc.perform((post("/my-renovations/job-details/reject-quote"))
                        .param("jobId", postedJob2.getId().toString())
                        .param("quoteId", quote1.getId().toString())
                        .param("fromSearch", "false")
                        .param("expensesPage", "1")
                        .param("quotesPage", "1")
                        .principal(principal))
                .andReturn();
        Assertions.assertEquals("Rejected", quoteService.findQuoteById(quote1.getId()).getStatus());
    }

    @When("I reject a quote on the tab")
    public void i_reject_a_quote_on_the_tab() throws Exception {
        mvcResult = mockMvc.perform((post("/my-renovations/job-details/reject-quote"))
                        .param("jobId", postedJob2.getId().toString())
                        .param("quoteId", quote1.getId().toString())
                        .param("fromSearch", "false")
                        .param("expensesPage", "1")
                        .param("quotesPage", "1")
                        .principal(principal))
                .andReturn();
        Assertions.assertEquals("Rejected", quoteService.findQuoteById(quote1.getId()).getStatus());
    }

    @Then("The quote tab sender is sent an email")
    public void the_quote_tab_sender_is_sent_an_email() throws Exception {
        await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        Mockito.verify(emailService, Mockito.times(1)).sendQuoteRejectedEmail(any(), any())
                );
    }

    @When("I accept a quote on the tab and transfer to expense")
    public void i_accept_a_quote_on_the_tab_and_transfer_to_expense() throws Exception {
        mockMvc.perform(post("/my-renovations/job-details/accept-quote")
                        .param("quoteId", quote1.getId().toString())
                        .param("retract", "true")
                        .param("transfer", "true")
                        .param("jobId", postedJob1.getId().toString())
                        .param("fromSearch", "false")
                        .param("expensesPage", "1")
                        .param("quotesPage", "1")
                        .principal(principal))
                .andExpect(status().is3xxRedirection());
    }

    @Then("A new expense is created for the quote tab")
    public void a_new_expense_is_created_for_the_quote_tab() throws Exception {
        Expense expense = expenseService.getExpensesByJobId(postedJob1.getId()).getFirst();
        Assertions.assertEquals(quote1.getPrice(), expense.getCost());
        Assertions.assertEquals("Quote", expense.getCategory());
        Assertions.assertEquals("Quote from Calan Meechang", expense.getDescription());
    }
}
