package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.controller.account.UserProfileController;
import nz.ac.canterbury.seng302.homehelper.controller.job.JobDetailsController;
import nz.ac.canterbury.seng302.homehelper.entity.*;
import nz.ac.canterbury.seng302.homehelper.repository.*;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class TradieRatingFeature {
    @Autowired
    private JobDetailsController jobDetailsController;
    @Autowired
    private UserProfileController userProfileController;
    @Autowired
    private UserService userService;
    @Autowired
    private RatingService ratingService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RatingRepository ratingRepository;
    @Autowired
    private Principal principal;
    @Autowired
    private ValidationService validationService;
    @Autowired
    private QuoteService quoteService;
    @Autowired
    private QuoteRepository quoteRepository;
    @Autowired
    private JobService jobService;
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private RenovationRecordService renovationRecordService;
    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    private MockMvc mockMvc;
    private MvcResult mvcResult;
    private User sendingUser;
    private User receivingUser;
    private Quote quote;
    private RenovationRecord record;
    private Job job;

    @Before("@TradieRatings")
    public void setup() {
        userRepository.deleteAll();
        ratingRepository.deleteAll();
        quoteRepository.deleteAll();
        jobRepository.deleteAll();
        renovationRecordRepository.deleteAll();

        when(principal.getName()).thenReturn("jane@doe.nz");
        sendingUser = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        sendingUser.setId(1L);
        receivingUser = new User("John", "Doe", "john@doe.nz", "P4$$word", null, null);
        receivingUser.setId(2L);
        userService.addUser(sendingUser);
        userService.addUser(receivingUser);
        job = new Job("TestJob", "JobDescription", "12/12/3030", "12/12/2030");
        record = new RenovationRecord("Reno", "reno", Collections.emptyList(), sendingUser.getEmail());
        job.setRenovationRecord(record);
        renovationRecordService.addRenovationRecord(record);
        job.setId(1L);
        jobService.addJob(job);
        quote = new Quote("10", "10", receivingUser.getEmail(), "1234567890", "description");
        quote.setJob(job);
        quote.setStatus("Accepted");
        quoteService.addQuote(quote);
    }

    @Given("A tradie has no ratings")
    public void a_tradie_has_no_ratings() {
        assertEquals(0, receivingUser.getReceivedRatings().size());
    }

    @Given("A tradie has a rating of {int}")
    public void a_tradie_has_a_rating_of(Integer ratingValue) {
        Rating rating = new Rating(ratingValue, receivingUser, null);
        ratingService.addRating(rating);
        receivingUser.setReceivedRatings(List.of(rating));
        assertEquals(1, receivingUser.getReceivedRatings().size());
    }

    @Given("I submit a rating of {int} to a tradie")
    public void i_submit_a_rating_of_to_a_tradie(Integer rating) throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(jobDetailsController).build();
        String ratingJson = "[{\"tradieId\": \""+receivingUser.getId()+"\", \"rating\": \""+rating+"\"}]";
        mockMvc.perform(post("/rate-tradie")
                        .param("ratings", ratingJson)
                        .param("jobId", "1")
                        .principal(principal))
                .andExpect(status().is3xxRedirection());
    }

    @When("I go to the tradie's profile page")
    public void i_go_to_the_tradie_s_profile_page() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(userProfileController).build();
        mvcResult = mockMvc.perform(get("/profile")
                    .param("userId", receivingUser.getId().toString())
                    .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("userProfileTemplate"))
                .andReturn();
    }

    @Then("The average rating on their profile page is {double}")
    public void the_average_rating_on_their_profile_page_is(double rating) {
        User tradie = (User) mvcResult.getModelAndView().getModel().get("profileUser");
        assertEquals(rating, tradie.getAverageRating());
    }

    @Then("Tradie has {int} ratings")
    public void tradie_still_has_no_ratings(Integer numberOfRatings) {
        assertEquals(numberOfRatings, receivingUser.getReceivedRatings().size());
    }

    @When("I set the job status to complete")
    public void i_set_the_job_status_to_complete() throws Exception {
        mvcResult = mockMvc.perform(post("/my-renovations/update-job-status")
                .param("jobId", job.getId().toString())
                .param("newJobStatus", "Completed")
                .principal(principal))
            .andExpect(status().is3xxRedirection())
            .andReturn();
    }

    @Then("The tradie is not shown")
    public void the_tradie_is_not_shown() {
        assertNull(mvcResult.getModelAndView().getModel().get("tradies"));
    }
}
