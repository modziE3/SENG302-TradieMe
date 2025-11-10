package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.controller.account.UserProfileController;
import nz.ac.canterbury.seng302.homehelper.controller.job.JobDetailsController;
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
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.QuoteService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class TradiePortfolioFeature {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserProfileController userProfileController;
    @Autowired
    private SubmitQuoteController submitQuoteController;
    @Autowired
    private MyQuotesController myQuotesController;
    @Autowired
    private JobDetailsController jobDetailsController;
    @Autowired
    private UserService userService;
    @Autowired
    private RenovationRecordService renovationRecordService;
    @Autowired
    private QuoteService quoteService;
    @Autowired
    private JobService jobService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RenovationRecordRepository renovationRecordRepository;
    @Autowired
    private QuoteRepository quoteRepository;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private Principal principal1;
    @Autowired
    private Principal principal2;

    private User userWithJob;
    private User tradieUser;
    private Job postedJob;
    private RenovationRecord record;
    private Quote quote;
    private MvcResult mvcResult;
    private Job updatedJob;

    @Transactional
    @Before("@TradiePortfolio")
    public void setup() {
        userRepository.deleteAll();
        renovationRecordRepository.deleteAll();
        quoteRepository.deleteAll();
        jobRepository.deleteAll();

        userWithJob = new User("John", "Doe", "john@doe.nz", "P4$$word", null, null);
        userWithJob.setId(1L);
        tradieUser = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        tradieUser.setId(2L);
        userWithJob = userRepository.save(userWithJob);
        tradieUser = userRepository.save(tradieUser);
        when(principal1.getName()).thenReturn("john@doe.nz");
        when(principal2.getName()).thenReturn("jane@doe.nz");

        record = new RenovationRecord("Record", "Record", List.of(), "john@doe.nz");
        record.setCity("Auckland");
        record.setSuburb("Auckland Central");
        renovationRecordService.addRenovationRecord(record);

        postedJob = new Job("Job", "Job", "02/02/2026", "01/01/2026");
        postedJob.setId(1L);
        postedJob.setIsPosted(true);
        postedJob.setExpenses(null);
        postedJob.setRenovationRecord(record);
        jobService.addJob(postedJob);

        for (int i = 0 ; i < 11 ; i++) {
            Job job = new Job("Job" + i,"Job", "02/02/2026", "01/01/2026" );
            job.setIsPosted(true);
            job.setExpenses(null);
            job.setRenovationRecord(record);
            job.setStatus("Completed");
            job.addPortfolioUser(tradieUser);
            job = jobRepository.save(job);
            Quote quote = new Quote("10", "12", tradieUser.getEmail(), "1234567890", "description");
            quote.setJob(job);
            quote.setUser(tradieUser);
            quote.setStatus("Accepted");
            quoteService.addQuote(quote);
            tradieUser.addPortfolioJob(job);
        }
        for (Job job : userService.getCompletedJobsUserHasWorkedOn(tradieUser.getId())) {
            job.addPortfolioUser(tradieUser);
            tradieUser.addPortfolioJob(job);
            job = jobRepository.save(job);
            tradieUser = userRepository.save(tradieUser);
        }
    }

    @Given("I have sent a quote to a renovators posted job")
    public void i_have_sent_a_quote_to_a_renovators_posted_job() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(submitQuoteController).build();
        mockMvc.perform(post("/submit-quote")
                    .param("jobId", postedJob.getId().toString())
                    .param("price", "10")
                    .param("workTime", "10")
                    .param("email", "jane@doe.nz")
                    .param("phoneNumber", "123456")
                    .param("description", "Hi")
                    .principal(principal2))
                .andExpect(status().is3xxRedirection());
    }

    @When("the renovator clicks on the link to my profile")
    public void the_renovator_clicks_on_the_link_to_my_profile() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(userProfileController).build();
        mvcResult = mockMvc.perform(get("/profile")
                    .param("userId", tradieUser.getId().toString())
                    .principal(principal1))
                .andExpect(view().name("userProfileTemplate"))
                .andReturn();
    }

    @Then("the renovator can see my profile")
    public void the_renovator_can_see_my_profile() {
        boolean ownProfile = (boolean) mvcResult.getModelAndView().getModel().get("ownProfile");
        assertFalse(ownProfile);
        User user = (User) mvcResult.getModelAndView().getModel().get("profileUser");
        assertEquals(tradieUser.getId(), user.getId());
    }

    @Given("I have a quote posted to a job")
    public void i_have_a_quote_posted_to_a_job() {
        quote = new Quote("12", "10", "jane@doe.nz", "1234", "Hi");
        quote.setUser(tradieUser);
        quote.setJob(postedJob);
        quoteService.addQuote(quote);

    }

    @When("the renovator accepts my quote")
    public void the_renovator_accepts_my_job() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(myQuotesController).build();
        Quote quote = quoteService.getAllQuotes().getLast();
        when(principal1.getName()).thenReturn("john@doe.nz");
        mockMvc.perform(post("/accept-quote")
                    .param("quoteId", quote.getId().toString())
                    .principal(principal1))
                .andExpect(status().is3xxRedirection());
    }

    @When("the renovator sets the job to completed")
    public void the_renovator_sets_the_job_to_completed() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(jobDetailsController).build();
        mockMvc.perform(post("/my-renovations/update-job-status")
                    .param("jobId", postedJob.getId().toString())
                    .param("newJobStatus", "Completed")
                    .principal(principal1))
                .andExpect(status().is3xxRedirection());
    }

    @Then("the completed job is added to my completed jobs tab")
    public void the_completed_job_is_added_to_my_completed_jobs_tab() {
        postedJob = jobService.getJobById(postedJob.getId());
        List<Job> completedJobs = userService.getCompletedJobsUserHasWorkedOn(tradieUser.getId());
        boolean found = completedJobs.stream().anyMatch(j -> j.getId().equals(postedJob.getId()));
        assertTrue(found);
    }

    @Given("I have completed jobs")
    public void i_have_completed_jobs() {
        List<Job> completedJobs = userService.getCompletedJobsUserHasWorkedOn(tradieUser.getId());
        assertTrue(completedJobs.size() > 9);
    }

    @Given("I have portfolio jobs")
    public void i_have_portfolio_jobs() throws Exception {
        List<Job> portfolioJobs = userService.getPortfolioJobs(tradieUser);
        assertTrue(portfolioJobs.size() > 9);
    }


    @When("I go on my profile page")
    public void i_go_on_my_profile_page() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(userProfileController).build();
        mvcResult = mockMvc.perform(get("/profile")
                        .param("userId", tradieUser.getId().toString())
                        .principal(principal2))
                .andExpect(view().name("userProfileTemplate"))
                .andReturn();
    }

    @Then("The completed jobs are paginated")
    public void the_completed_jobs_are_paginated() {
        List<Integer> pages = (List<Integer>) mvcResult.getModelAndView().getModel().get("completedJobsPageNums");
        assertTrue(pages.size() > 1);
    }

    @Then("I am shown upto 9 completed jobs")
    public void i_am_shown_upto_9_completed_jobs() {
        List<Job> completedJobs = (List<Job>) mvcResult.getModelAndView().getModel().get("completedJobs");
        assertTrue(completedJobs.size() <= 9);
    }

    @Then("The portfolio jobs are paginated")
    public void the_portfolio_jobs_are_paginated() {
        List<Integer> pages = (List<Integer>) mvcResult.getModelAndView().getModel().get("portfolioJobsPageNums");
        assertTrue(pages.size() > 1);
    }

    @Then("I am shown upto 9 portfolio jobs")
    public void i_am_shown_upto_9_portfolio_jobs() {
        List<Job> completedJobs = (List<Job>) mvcResult.getModelAndView().getModel().get("portfolioJobsPage");
        assertTrue(completedJobs.size() <= 9);
    }
}
