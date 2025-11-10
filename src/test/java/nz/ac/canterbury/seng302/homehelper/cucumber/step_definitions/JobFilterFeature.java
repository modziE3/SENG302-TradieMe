package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.controller.job.JobListingController;
import nz.ac.canterbury.seng302.homehelper.entity.*;
import nz.ac.canterbury.seng302.homehelper.entity.dto.JobCardInfo;
import nz.ac.canterbury.seng302.homehelper.repository.JobRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class JobFilterFeature {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JobService jobService;
    @Autowired
    private JobListingController jobListingController;
    @Autowired
    private Principal principal;
    @Autowired
    private ValidationService validationService;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private RenovationRecordRepository renovationRecordRepository;
    @Autowired
    private RenovationRecordService renovationRecordService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    private MvcResult mvcResult;

    private Job postedJob;
    private RenovationRecord record;
    private User user;

    @Before("@FilterJobs")
    public void setup() {
        jobRepository.deleteAll();
        userRepository.deleteAll();
        renovationRecordRepository.deleteAll();

        when(principal.getName()).thenReturn("jane@doe.nz");

        postedJob = new Job("Job", "Job\uD83D\uDE00", "02/02/2026", "01/01/2026");
        record = new RenovationRecord("Record", "Record", List.of(), "jane@doe.nz");
        record.setCity("Auckland");
        record.setSuburb("Epsom");
        user = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        userService.addUser(user);
        postedJob.setId(1L);
        postedJob.setIsPosted(true);
        postedJob.setExpenses(null);
        postedJob.setRenovationRecord(record);
        renovationRecordService.addRenovationRecord(record);
        jobService.addJob(postedJob);
    }

    @Given("I am on the Available Jobs page.")
    public void i_am_on_the_available_jobs_page() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(jobListingController).build();

        mockMvc.perform(get("/job-listings")
                        .param("job-page", "1")
                        .principal(principal))
                .andExpect(view().name("jobListingTemplate"));
    }

    @When("I enter {string} in the search bar and click filter results")
    public void i_enter_a_in_the_search_bar(String string) throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(jobListingController).build();

        mvcResult = mockMvc.perform(get("/job-listings")
                        .param("job-page", "1")
                        .param("keywords-filter", string)
                        .principal(principal))
                .andExpect(view().name("jobListingTemplate"))
                .andReturn();
    }
    @Then("I am shown only posted jobs whose name or description include {string}")
    public void i_am_shown_only_posted_jobs_whose_name_or_description_include_my(String string) {
        ModelAndView mav = mvcResult.getModelAndView();
        List<Job> jobs = ((List<JobCardInfo>) mav.getModel().get("jobs")).stream()
                .map(j -> jobService.getJobById(j.getJobId()))
                .collect(Collectors.toList());

        assertNotNull(jobs);
        for (Job job : jobs) {
            String name = job.getName().toLowerCase();
            String description = job.getDescription().toLowerCase();
            assertTrue(name.contains(string.toLowerCase()) || description.contains(string.toLowerCase()));
        }
    }

    @When("I select the job type {string} and click filter results")
    public void i_select_the_job_type_and_click_filter_results(String string) throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(jobListingController).build();

        mvcResult = mockMvc.perform(get("/job-listings")
                        .param("job-page", "1")
                        .param("type-filter", string)
                        .principal(principal))
                .andExpect(view().name("jobListingTemplate"))
                .andReturn();
    }

    @Then("I am shown only posted jobs whose job type is {string}")
    public void i_am_shown_only_posted_jobs_whose_job_type_is(String string) {
        ModelAndView mav = mvcResult.getModelAndView();
        List<Job> jobs = ((List<JobCardInfo>) mav.getModel().get("jobs")).stream()
                .map(j -> jobService.getJobById(j.getJobId()))
                .collect(Collectors.toList());

        assertNotNull(jobs);
        for (Job job : jobs) {
            assertEquals(job.getType(), string);
        }
    }

    @When("I enter {string} in the due date field and click filter results")
    public void i_enter_in_the_due_date_field_and_click_filter_results(String string) throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(jobListingController).build();

        mvcResult = mockMvc.perform(get("/job-listings")
                        .param("job-page", "1")
                        .param("jobDueDate", string)
                        .principal(principal))
                .andExpect(view().name("jobListingTemplate"))
                .andReturn();
    }

    @Then("I am shown only posted jobs whose due date is before {string}")
    public void i_am_shown_only_posted_jobs_whose_due_date_is_before(String string) throws ParseException {
        ModelAndView mav = mvcResult.getModelAndView();
        List<Job> jobs = ((List<JobCardInfo>) mav.getModel().get("jobs")).stream()
                .map(j -> jobService.getJobById(j.getJobId()))
                .collect(Collectors.toList());

        assertNotNull(jobs);
        for (Job job : jobs) {
            assertTrue(validationService.dateAfterAnotherDate(string, job.getDueDate()));
        }
    }

    @Then("I am shown the due date error message {string}")
    public void i_am_shown_the_due_date_error_message(String string) {
        ModelAndView mav = mvcResult.getModelAndView();
        assertEquals(mav.getModel().get("jobDueDateErrorMessage"), string);
    }

    @When("I enter {string} in the start date field and click filter results")
    public void i_enter_in_the_start_date_field_and_click_filter_results(String string) throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(jobListingController).build();

        mvcResult = mockMvc.perform(get("/job-listings")
                        .param("job-page", "1")
                        .param("jobStartDate", string)
                        .principal(principal))
                .andExpect(view().name("jobListingTemplate"))
                .andReturn();
    }

    @Then("I am shown the start date error message {string}")
    public void i_am_shown_the_start_date_error_message(String string) {
        ModelAndView mav = mvcResult.getModelAndView();
        assertEquals(mav.getModel().get("jobStartDateErrorMessage"), string);
    }

    @Given("I have added some job filters")
    public void i_have_added_some_job_filters() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(jobListingController).build();

        mvcResult = mockMvc.perform(get("/job-listings")
                        .param("keywords-filter", "Test")
                        .param("type-filter", "Carpentry")
                        .param("jobStartDate", "12/10/2025")
                        .param("jobDueDate", "12/11/2025")
                        .principal(principal))
                .andExpect(view().name("jobListingTemplate"))
                .andReturn();

        ModelAndView mav = mvcResult.getModelAndView();
        List<JobCardInfo> jobCards = (List<JobCardInfo>) mav.getModel().get("jobs");
        assertEquals(0, jobCards.size());
    }

    @When("I click the Clear Filters button")
    public void i_click_the_clear_filters_button() throws Exception {
        mvcResult = mockMvc.perform(get("/job-listings")
                        .principal(principal))
                .andExpect(view().name("jobListingTemplate"))
                .andReturn();
    }

    @Then("All job filters are cleared")
    public void all_job_filters_are_cleared() {
        ModelAndView mav = mvcResult.getModelAndView();
        assertEquals("", mav.getModel().get("keywordsFilter"));
        assertEquals("", mav.getModel().get("typeFilter"));
        assertEquals("", mav.getModel().get("jobStartDate"));
        assertEquals("", mav.getModel().get("jobDueDate"));
        List<JobCardInfo> jobCards = (List<JobCardInfo>) mav.getModel().get("jobs");
        assertEquals(postedJob.getName(), jobCards.getFirst().getTitle());
    }

    @When("I select the city {string} and click filter results")
    public void i_select_the_city_and_click_filter_results(String string) throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(jobListingController).build();

        mvcResult = mockMvc.perform(get("/job-listings")
                        .param("job-page", "1")
                        .param("city-filter", string)
                        .principal(principal))
                .andExpect(view().name("jobListingTemplate"))
                .andReturn();
    }

    @Then("I am shown only posted jobs whose city is {string}")
    public void i_am_shown_only_posted_jobs_whose_city_is(String string) {
        ModelAndView mav = mvcResult.getModelAndView();
        List<Job> jobs = ((List<JobCardInfo>) mav.getModel().get("jobs")).stream()
                .map(j -> jobService.getJobById(j.getJobId()))
                .collect(Collectors.toList());

        assertNotNull(jobs);
        for (Job job : jobs) {
            assertEquals(job.getRenovationRecord().getCity(), string);
        }
    }

    @When("I select the city {string} and suburb {string} and click filter results")
    public void i_select_the_city_and_suburb_and_click_filter_results(String cityString, String suburbString) throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(jobListingController).build();

        mvcResult = mockMvc.perform(get("/job-listings")
                        .param("job-page", "1")
                        .param("city-filter", cityString)
                        .param("suburb-filter", suburbString)
                        .principal(principal))
                .andExpect(view().name("jobListingTemplate"))
                .andReturn();
    }

    @Then("I am shown only posted jobs whose city is {string} and suburb is {string}")
    public void i_am_shown_only_posted_jobs_whose_city_is_and_suburb_is(String cityString, String suburbString) {
        ModelAndView mav = mvcResult.getModelAndView();
        List<Job> jobs = ((List<JobCardInfo>) mav.getModel().get("jobs")).stream()
                .map(j -> jobService.getJobById(j.getJobId()))
                .collect(Collectors.toList());

        assertNotNull(jobs);
        for (Job job : jobs) {
            assertEquals(job.getRenovationRecord().getCity(), cityString);
            assertEquals(job.getRenovationRecord().getSuburb(), suburbString);
        }
    }

    @Then("I am shown the suburb error message {string}")
    public void i_am_shown_the_suburb_error_message(String string) {
        ModelAndView mav = mvcResult.getModelAndView();
        assertEquals(string, mav.getModel().get("suburbErrorMessage"));
    }

    @Then("I am shown the city error message {string}")
    public void i_am_shown_the_city_error_message(String string) {
        ModelAndView mav = mvcResult.getModelAndView();
        assertEquals(string, mav.getModel().get("cityErrorMessage"));
    }
}
