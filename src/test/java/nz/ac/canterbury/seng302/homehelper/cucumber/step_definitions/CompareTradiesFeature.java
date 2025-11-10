package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.controller.job.JobDetailsController;
import nz.ac.canterbury.seng302.homehelper.controller.job.JobListingController;
import nz.ac.canterbury.seng302.homehelper.controller.quote.CompareTradieController;
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
import org.assertj.core.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class CompareTradiesFeature {

    @Autowired private MockMvc mockMvc;

    @Autowired private CompareTradieController compareTradieController;
    @Autowired private Principal principal;

    @Autowired private JobService jobService;
    @Autowired private RenovationRecordService renovationRecordService;
    @Autowired private UserService userService;
    @Autowired private QuoteService quoteService;

    @Autowired private QuoteRepository quoteRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JobRepository jobRepository;
    @Autowired private RenovationRecordRepository renovationRecordRepository;

    private RenovationRecord testRecord;
    private Job testJob;
    private User testUser;
    private User quote1Sender;
    private User quote2Sender;
    private Quote quote1;
    private Quote quote2;

    private String responseString;


    @Before("@CompareTradies")
    public void setup() {
        quoteRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();
        renovationRecordRepository.deleteAll();

        when(principal.getName()).thenReturn("jane@doe.nz");

        testRecord = new RenovationRecord("Record", "Record", List.of(), "jane@doe.nz");
        testJob = new Job("Job", "Job", null, null);
        testJob.setRenovationRecord(testRecord);
        testJob.setIsPosted(true);
        testRecord.setJobs(List.of(testJob));
        testUser = new User("J", "D", "jane@doe.nz", "P4$$word", null, null);

        quote1Sender = new User("1", "last", "1@doe.nz", "P4$$word", null, null);
        quote2Sender = new User("2", "last", "2@doe.nz", "P4$$word", null, null);
        quote1 = new Quote("10", "10", quote1Sender.getEmail(), "", "");
        quote1.setUser(quote1Sender);
        quote1.setJob(testJob);
        quote2 = new Quote("15", "15", quote2Sender.getEmail(), "", "");
        quote2.setUser(quote2Sender);
        quote2.setJob(testJob);
        testJob.setQuotes(List.of(quote1, quote2));


        userRepository.save(testUser);
        userRepository.save(quote1Sender);
        userRepository.save(quote2Sender);

        renovationRecordService.addRenovationRecord(testRecord);
        jobService.addJob(testJob);
        quoteService.addQuote(quote1);
        quoteService.addQuote(quote2);
    }

    @Given("I am on the compare tradies page")
    public void i_am_on_the_compare_tradies_page() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(compareTradieController).build();

        mockMvc.perform(get("/my-renovations/job-details/compare-tradies")
                        .param("jobId", testJob.getId().toString())
                        .principal(principal))
                .andExpect(view().name("compareTradiesTemplate"));
    }
    @When("I reject a tradie")
    public void i_reject_a_tradie() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(compareTradieController).build();

        responseString = mockMvc.perform(patch("/my-renovations/job-details/compare-tradies/reject")
                        .param("side", "left")
                        .param("rejectedQuoteId", quote1.getId().toString())
                        .principal(principal))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
    @Then("their quote is also rejected")
    public void their_quote_is_also_rejected() {
        String quoteId = null;
        try {
            quoteId = new ObjectMapper().readTree(responseString).get("quoteId").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertThat(quoteId).isEqualTo(quote1.getId().toString());
        Assertions.assertThat(quoteService.findQuoteById(quote1.getId()).getStatus()).isEqualTo("Rejected");
    }
}
