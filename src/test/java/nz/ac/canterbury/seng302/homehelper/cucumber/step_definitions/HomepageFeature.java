package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.controller.CustomiseWidgetsController;
import nz.ac.canterbury.seng302.homehelper.controller.HomePageController;
import nz.ac.canterbury.seng302.homehelper.controller.job.JobDetailsController;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.dto.JobCardInfo;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.JobRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class HomepageFeature {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private HomePageController homePageController;
    @Autowired
    private JobDetailsController jobDetailsController;
    @Autowired
    private CustomiseWidgetsController customiseWidgetsController;
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
    private Principal principal;

    private MvcResult mvcResult;

    private Job job;
    private RenovationRecord record;
    private User user;
    private String widgetOrder;

    @Before("@Homepage")
    public void setup() {
        jobRepository.deleteAll();
        userRepository.deleteAll();
        renovationRecordRepository.deleteAll();

        when(principal.getName()).thenReturn("jane@doe.nz");

        job = new Job("Job", "Job", "02/02/2026", "01/01/2026");
        record = new RenovationRecord("Record", "Record", List.of(), "jane@doe.nz");
        user = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        user.setId(221390L);
        userService.addUser(user);
        renovationRecordRepository.save(record);
        widgetOrder = String.join(",", user.getHomePageWidgetOrder());

        job.setId(1L);
        job.setIsPosted(true);
        job.setExpenses(null);
        job.setRenovationRecord(record);
        job = jobRepository.save(job);
    }

    @Given("I am viewing a jobs details")
    public void i_am_viewing_a_jobs_details() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(jobDetailsController).build();

        mockMvc.perform(get("/my-renovations/job-details")
                        .param("jobId", job.getId().toString())
                        .principal(principal))
                .andExpect(view().name("jobDetailsTemplate"));

        user = userService.getUser(principal.getName());
        Assertions.assertEquals(job.getId(), user.getRecentJobs().getFirst());
    }

    @When("I go to the homepage")
    public void i_go_to_the_homepage() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(homePageController).build();

        mvcResult = mockMvc.perform(get("/home")
                        .param("name", "jane")
                        .principal(principal))
                .andExpect(view().name("homePageTemplate")).andReturn();
    }
    
    @Then("I can see a list of recently viewed jobs containing the job I viewed last")
    public void i_can_see_a_list_of_recently_viewed_jobs_containing_the_job_i_viewed_last() {
        List<JobCardInfo> jobCardInfos = (List<JobCardInfo>) mvcResult.getModelAndView().getModel().get("recentJobs");
        Assertions.assertNotNull(jobCardInfos);
        Assertions.assertEquals(1, jobCardInfos.size());
        Assertions.assertEquals(job.getId(), jobCardInfos.getFirst().getJobId());
    }

    @Given("I am on the Customise Widgets page")
    public void i_am_on_the_customise_widgets_page() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(customiseWidgetsController).build();
        mockMvc.perform(get("/customise-widgets")
                    .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("customiseWidgetsTemplate"));
    }

    @When("I submit a new home page widget order")
    public void i_submit_a_new_home_page_widget_order() throws Exception {
        widgetOrder = "Recently Viewed Renovations,Job Calendar,Recent Jobs,Job Recommendations";
        mockMvc.perform(post("/customise-widgets")
                .param("widgetOrder", widgetOrder)
                .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/home"));
    }

    @Then("Widget order is displayed on the home page")
    public void widget_order_is_displayed_on_the_home_page() {
        List<String> widgetOrderList = List.of(widgetOrder.split(","));
        user = userService.getUser(principal.getName());
        assertEquals(widgetOrderList, user.getHomePageWidgetOrder());
    }

    @When("I disable the home page widget {string}")
    public void i_disable_the_home_page_widget(String widget) throws Exception {
        List<String> widgetOrderList = new ArrayList<>(List.of(widgetOrder.split(",")));
        widgetOrderList.remove(widget);
        widgetOrder = String.join(",", widgetOrderList);
        mockMvc.perform(post("/customise-widgets")
                        .param("widgetOrder", widgetOrder)
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/home"));
    }

    @Then("{string} is not displayed on the home page")
    public void is_not_displayed_on_the_home_page(String widget) {
        user = userService.getUser(principal.getName());
        assertFalse(user.getHomePageWidgetOrder().contains(widget));
    }

    @Given("Home page widget {string} is disabled")
    public void home_page_widget_is_disabled(String widget) {
        List<String> widgetOrderList = user.getHomePageWidgetOrder();
        widgetOrderList.remove(widget);
        widgetOrder = String.join(",", widgetOrderList);
        user.setHomePageWidgetOrder(widgetOrderList);
        userService.addUser(user);
    }

    @When("I enable the home page widget {string}")
    public void i_enable_the_home_page_widget(String widget) throws Exception {
        List<String> widgetOrderList = new ArrayList<>(List.of(widgetOrder.split(",")));
        widgetOrderList.add(widget);
        widgetOrder = String.join(",", widgetOrderList);
        mockMvc.perform(post("/customise-widgets")
                        .param("widgetOrder", widgetOrder)
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/home"));
    }

    @Then("{string} is displayed on the home page")
    public void is_displayed_on_the_home_page(String widget) {
        user = userService.getUser(principal.getName());
        assertTrue(user.getHomePageWidgetOrder().contains(widget));
    }
}
