package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.controller.job.JobFormController;
import nz.ac.canterbury.seng302.homehelper.controller.renovations.RenovationDetailsController;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.JobRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RecentRenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class JobCalendarFeature {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JobFormController jobFormController;
    @Autowired
    private RenovationDetailsController renovationDetailsController;
    @Autowired
    private JobService jobService;
    @Autowired
    private RenovationRecordService renovationRecordService;
    @Autowired
    private UserService userService;
    @Autowired
    private RenovationRecordRepository renovationRecordRepository;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RecentRenovationRepository recentRenovationRepository;

    @Autowired
    private Principal principal;
    private MvcResult mvcResult;

    private Job postedJob;
    private RenovationRecord record;
    private User user;

    @Before("@JobCalender")
    public void setup() {
        jobRepository.deleteAll();
        userRepository.deleteAll();
        renovationRecordRepository.deleteAll();
        recentRenovationRepository.deleteAll();

        when(principal.getName()).thenReturn("jane@doe.nz");

        postedJob = new Job("Job", "Job", "02/02/2026", "01/01/2026");
        record = new RenovationRecord("Record", "Record", List.of(), "jane@doe.nz");
        record.setId(1L);
        user = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        userService.addUser(user);
        postedJob.setId(1L);
        postedJob.setIsPosted(true);
        postedJob.setExpenses(null);
        postedJob.setRenovationRecord(record);
    }

    @Given("I am viewing a calendar of a record I own")
    public void i_am_viewing_a_calendar_of_a_record_i_own() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(renovationDetailsController).build();

        renovationRecordService.addRenovationRecord(record);
        jobService.addJob(postedJob);

        mvcResult = mockMvc.perform(get("/my-renovations/details#nav-calendar")
                        .param("recordId", record.getId().toString())
                        .principal(principal))
                .andExpect(view().name("renovationDetailsTemplate"))
                .andReturn();
    }

    @When("I double-click a job")
    public void i_double_click_a_job() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(jobFormController).build();

        mvcResult = mockMvc.perform(get("/my-renovations/edit-job")
                    .param("recordId", record.getId().toString())
                    .param("jobId", postedJob.getId().toString())
                    .param("fromCalendar", String.valueOf(true))
                    .principal(principal))
                .andExpect(view().name("jobFormTemplate"))
                .andReturn();
    }

    @Then("I am taken to the edit job page for that job")
    public void i_am_taken_to_the_edit_job_page_for_that_job() {
        ModelAndView mav = mvcResult.getModelAndView();
        assertEquals(postedJob.getName(), mav.getModel().get("newJobName"));
        assertEquals(postedJob.getDescription(), mav.getModel().get("jobDescription"));
        assertEquals(postedJob.getStartDate(), mav.getModel().get("jobStartDate"));
        assertEquals(postedJob.getDueDate(), mav.getModel().get("jobDueDate"));
    }
}
