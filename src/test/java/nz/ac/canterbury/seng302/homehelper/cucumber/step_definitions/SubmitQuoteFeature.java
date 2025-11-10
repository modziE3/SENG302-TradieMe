package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.controller.job.JobDetailsController;
import nz.ac.canterbury.seng302.homehelper.controller.job.JobListingController;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class SubmitQuoteFeature {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JobService jobService;
    @Autowired
    private JobListingController jobListingController;
    @Autowired
    private JobDetailsController jobDetailsController;
    @Autowired
    private SubmitQuoteController submitQuoteController;
    @Autowired
    private QuoteRepository quoteRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private RenovationRecordRepository renovationRecordRepository;
    private Model model;
    @Autowired
    private RenovationRecordService renovationRecordService;
    @Autowired
    private UserService userService;
    @Autowired
    private Principal principal;
    @Autowired
    private EmailService emailService;
    @Autowired
    private QuoteService quoteService;

    private Job postedJob;
    private RenovationRecord record;
    private User user;
    private ArgumentCaptor<Quote> quoteCaptor;
    private ResultActions resultActions;

    @Before("@SubmitQuote")
    public void setup() {
        quoteRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();
        renovationRecordRepository.deleteAll();

        Mockito.reset(emailService);
        when(principal.getName()).thenReturn("jane@doe.nz");

        postedJob = new Job("Job", "Job", "02/02/2026", "01/01/2026");
        record = new RenovationRecord("Record", "Record", List.of(), "jane@doe.nz");
        record.setCity("Auckland");
        record.setSuburb("Auckland Central");
        user = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        userService.addUser(user);
        renovationRecordService.addRenovationRecord(record);
        postedJob.setId(1L);
        postedJob.setIsPosted(true);
        postedJob.setExpenses(null);
        postedJob.setRenovationRecord(record);
        jobService.addJob(postedJob);
    }

    @Given("I am on the Available Jobs page")
    public void i_am_on_the_available_jobs_page() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(jobListingController).build();

        mockMvc.perform(get("/job-listings")
                        .param("job-page", "1")
                        .principal(principal))
                .andExpect(view().name("jobListingTemplate"));
    }

    @Given("I have already submitted a quote")
    public void i_have_already_submitted_a_quote() throws Exception {
        User savedUser = userService.getUser("jane@doe.nz");
        Job jobFromDatabase = jobService.getJobById(postedJob.getId());
        Quote quote = new Quote("13", "3", "jane@doe.nz", "123456789", "Description");
        quote.setJob(jobFromDatabase);
        quote.setUser(savedUser);
        quoteService.addQuote(quote);
    }

    @When("I go to the details page of a posted job")
    public void i_go_to_the_details_page_of_a_posted_job() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(jobDetailsController).build();

        assertEquals(postedJob.getName(), jobService.getJobById(postedJob.getId()).getName());

        mockMvc.perform(get("/my-renovations/job-details")
                        .param("jobId",  postedJob.getId().toString())
                        .param("expensesPage", "1")
                        .param("fromSearch", "true")
                        .principal(principal))
                .andExpect(view().name("jobDetailsTemplate"));
    }

    @Then("I can see the Submit Quote button")
    public void i_can_see_the_submit_quote_button() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(submitQuoteController).build();

        mockMvc.perform(get("/submit-quote")
                        .param("jobId", postedJob.getId().toString()))
                .andExpect(view().name("submitQuoteTemplate"));
    }

    @Given("I am on the Submit Quote form")
    public void i_am_on_the_submit_quote_form() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(submitQuoteController).build();

        mockMvc.perform(get("/submit-quote")
                        .param("jobId", postedJob.getId().toString()))
                .andExpect(view().name("submitQuoteTemplate"));
    }

    @When("I input valid values and I press Submit")
    public void i_input_valid_values_and_i_press_submit() throws Exception {
        resultActions = mockMvc.perform(post("/submit-quote")
                        .param("jobId", postedJob.getId().toString())
                        .param("price", "2")
                        .param("workTime", "10")
                        .param("email", "jane@doe.nz")
                        .param("phoneNumber", "123456789")
                        .param("description", "Hi")
                        .principal(principal));
    }

    @Then("the system saves the quote")
    public void the_system_saves_the_quote() {
        List<Quote> capturedQuotes = quoteRepository.findAllByDescription("Hi");
        Quote capturedQuote = capturedQuotes.getLast();
        Assertions.assertEquals("2", capturedQuote.getPrice());
        Assertions.assertEquals("10", capturedQuote.getWorkTime());
        Assertions.assertEquals("jane@doe.nz", capturedQuote.getEmail());
        Assertions.assertEquals("123456789", capturedQuote.getPhoneNumber());
        Assertions.assertEquals("Hi", capturedQuote.getDescription());
    }

    @Then("I am returned to the posted job details page")
    public void i_am_returned_to_the_posted_job_details_page() throws Exception {
        resultActions.andExpect(view().name("redirect:/my-renovations/job-details?jobId="+postedJob.getId()+"&fromSearch=true"));
    }

    @When("I enter empty or invalid values for price and I press Submit")
    public void i_enter_empty_or_invalid_values_for_cost_and_press_Submit() throws Exception {
        mockMvc.perform(post("/submit-quote")
                .param("jobId", postedJob.getId().toString())
                .param("price", "")
                .param("workTime", "10")
                .param("email", "jane@doe.nz")
                .param("phoneNumber", "1234")
                .param("description", "Hi")
                .principal(principal));
    }

    @When("I enter empty or invalid values for estimated work time and I press Submit")
    public void i_enter_empty_or_invalid_values_for_estimated_work_time_and_press_Submit() throws Exception {
        mockMvc.perform(post("/submit-quote")
                .param("jobId", postedJob.getId().toString())
                .param("price", "2")
                .param("workTime", "")
                .param("email", "jane@doe.nz")
                .param("phoneNumber", "1234")
                .param("description", "Hi")
                .principal(principal));
    }

    @When("I enter empty or invalid values for email and I press Submit")
    public void i_enter_empty_or_invalid_values_for_email_and_press_Submit() throws Exception {
        mockMvc.perform(post("/submit-quote")
                .param("jobId", postedJob.getId().toString())
                .param("price", "2")
                .param("workTime", "10")
                .param("email", "")
                .param("phoneNumber", "1234")
                .param("description", "Hi")
                .principal(principal));
    }

    @When("I enter empty or invalid values for phone number and I press Submit")
    public void i_enter_empty_or_invalid_values_for_phone_number_and_press_Submit() throws Exception {
        mockMvc.perform(post("/submit-quote")
                .param("jobId", postedJob.getId().toString())
                .param("price", "2")
                .param("workTime", "10")
                .param("email", "jane@doe.nz")
                .param("phoneNumber", "hello")
                .param("description", "Hi")
                .principal(principal));
    }

    @Then("I am shown the price error message")
    public void i_am_shown_the_price_error_message() throws Exception {
        mockMvc.perform(post("/submit-quote")
                .param("jobId", postedJob.getId().toString())
                .param("price", "")
                .param("workTime", "10")
                .param("email", "jane@doe.nz")
                .param("phoneNumber", "1234")
                .param("description", "Hi")
                .principal(principal))
            .andExpect(model().attribute("priceErrorMessage", "Quote price cannot be empty"));

    }

    @Then("I am shown the estimated work time error message")
    public void i_am_shown_the_estimated_work_time_error_message() throws Exception {
        mockMvc.perform(post("/submit-quote")
                        .param("jobId", postedJob.getId().toString())
                        .param("price", "2")
                        .param("workTime", "")
                        .param("email", "jane@doe.nz")
                        .param("phoneNumber", "1234")
                        .param("description", "Hi")
                        .principal(principal))
                .andExpect(model().attribute("workTimeErrorMessage", "Estimated time must not be empty and must be numeric"));

    }

    @Then("I am shown the email error message")
    public void i_am_shown_the_email_error_message() throws Exception {
        mockMvc.perform(post("/submit-quote")
                        .param("jobId", postedJob.getId().toString())
                        .param("price", "2")
                        .param("workTime", "10")
                        .param("email", "")
                        .param("phoneNumber", "1234")
                        .param("description", "Hi")
                        .principal(principal))
                .andExpect(model().attribute("emailErrorMessage", "Email address must be in the form 'jane@doe.nz'"));

    }


    @Then("I am shown the phone number error message")
    public void i_am_shown_the_phone_number_error_message() throws Exception {
        mockMvc.perform(post("/submit-quote")
                        .param("jobId", postedJob.getId().toString())
                        .param("price", "2")
                        .param("workTime", "10")
                        .param("email", "jane@doe.nz")
                        .param("phoneNumber", "hello")
                        .param("description", "Hi")
                        .principal(principal))
                .andExpect(model().attribute("phoneNumberErrorMessage", "Phone number must be a valid phone number"));

    }

    @Then("The system sends an email to the job owner")
    public void the_system_sends_a_email_to_the_job_owner() throws Exception {
        await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        Mockito.verify(emailService, Mockito.times(1)).sendQuoteReceivedEmail(any())
                );
    }

    @Then("I am shown the already quoted error message")
    public void i_am_shown_the_already_quoted_error_message() throws Exception {
        mockMvc.perform(post("/submit-quote")
                        .param("jobId", postedJob.getId().toString())
                        .param("price", "2")
                        .param("workTime", "10")
                        .param("email", "jane@doe.nz")
                        .param("phoneNumber", "123456789")
                        .param("description", "Hi")
                        .principal(principal))
                .andExpect(model().attribute("quotedErrorMessage", "You have already submitted a quote for this job"));
    }
}
