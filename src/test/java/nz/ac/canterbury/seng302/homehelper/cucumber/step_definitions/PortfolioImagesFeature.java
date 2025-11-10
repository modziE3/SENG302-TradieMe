package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.controller.account.UserProfileController;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class PortfolioImagesFeature {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserProfileController userProfileController;
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
    private MockMultipartFile file;

    @Transactional
    @Before("@PortfolioImages")
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

    @Given("I am on the profile page")
    public void i_am_on_the_profile_page() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(userProfileController).build();
        mockMvc.perform(get("/profile")
                .param("userId", tradieUser.getId().toString())
                        .principal(principal2))
                .andExpect(status().isOk());
    }

    @When("I submit an image")
    public void i_submit_an_image() throws Exception {
        file = new MockMultipartFile("submittedFile", "test.png", "image/png", "test".getBytes());
        postedJob = tradieUser.getPortfolioJobs().get(0);
        mockMvc.perform(multipart("/add-job-image")
                        .file(file)
                        .param("jobId", postedJob.getId().toString()))
                .andExpect(status().is3xxRedirection());
    }

    @Then("the image is added to the portfolio job")
    public void the_image_is_added_to_the_portfolio_job() {
        String uniqueFileName = "job" + postedJob.getId().toString() + file.getOriginalFilename();
        Path uploadPath = Paths.get("uploads/images/");
        Path filePath = uploadPath.resolve(uniqueFileName);
        assertTrue(Files.exists(filePath));
        postedJob = jobService.getJobById(postedJob.getId());
        assertTrue(postedJob.getImageFilenames().contains(uniqueFileName));
    }
}
