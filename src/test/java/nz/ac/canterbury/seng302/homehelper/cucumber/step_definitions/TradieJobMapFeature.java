package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.cucumber.core.internal.com.fasterxml.jackson.core.type.TypeReference;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.controller.account.UserProfileController;
import nz.ac.canterbury.seng302.homehelper.controller.quote.CompareTradieController;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.Quote;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.entity.dto.MapMarker;
import nz.ac.canterbury.seng302.homehelper.repository.JobRepository;
import nz.ac.canterbury.seng302.homehelper.repository.QuoteRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.QuoteService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.unit.controller.UserProfileControllerTest;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Type;
import java.security.Principal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class TradieJobMapFeature {

    @Autowired private MockMvc mockMvc;

    @Autowired private UserProfileController userProfileController;
    @Autowired private Principal principal;

    @Autowired private JobService jobService;
    @Autowired private RenovationRecordService renovationRecordService;
    @Autowired private UserService userService;
    @Autowired private QuoteService quoteService;

    @Autowired private QuoteRepository quoteRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JobRepository jobRepository;
    @Autowired private RenovationRecordRepository renovationRecordRepository;

    private MvcResult mvcResult;

    private User owner;
    private User tradie;
    private RenovationRecord record;
    private Job job;
    private Quote quote;

    @Before("@TradieJobMap")
    public void setup() {
        //clear
        quoteRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();
        renovationRecordRepository.deleteAll();

        //data setup
        owner = new User("owner", "last", "o@o.o", "P4$$word", null, null);
        tradie = new User("tradie", "last", "t@t.t", "P4$$word", null, null);
        record = new RenovationRecord("r", "r", List.of(), owner.getEmail());
        job = new Job("j", "d", "31/12/2025", "30/12/2025");
        quote = new Quote("1", "1", tradie.getEmail(), null, "d");

        record.setJobs(List.of(job));
        job.setQuotes(List.of(quote));
        job.setRenovationRecord(record);
        quote.setJob(job);

        userRepository.save(owner);
        userRepository.save(tradie);
        renovationRecordRepository.save(record);
        jobRepository.save(job);
        quoteRepository.save(quote);
    }

    @Given("a tradie has completed jobs in their portfolio")
    public void a_tradie_has_completed_jobs_in_their_portfolio() {
        job.setStatus("completed");
        quote.setStatus("Accepted");
        jobRepository.save(job);
        quoteRepository.save(quote);
    }

    @When("I go to that tradie's map")
    public void i_go_to_that_tradie_s_map() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(userProfileController).build();
        when(principal.getName()).thenReturn(owner.getEmail());

        mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/map")
                .param("userId", tradie.getId().toString())
                .principal(principal)).andExpect(status().isOk()).andReturn();
    }

    @Then("I can see pins on the locations of the portfolio jobs")
    public void i_can_see_pins_on_the_locations_of_the_portfolio_jobs() throws Exception {
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/profile/map/jobs")
                .param("userId", tradie.getId().toString())
                .principal(principal)).andExpect(status().isOk()).andReturn();

        Gson gson = new Gson();
        Type listType = new TypeToken<List<MapMarker>>() {}.getType();
        List<MapMarker> markers = gson.fromJson(mvcResult.getResponse().getContentAsString(), listType);
        Assertions.assertEquals(markers, userService.getPortfolioMapMarkers(tradie.getId()));
    }
}
