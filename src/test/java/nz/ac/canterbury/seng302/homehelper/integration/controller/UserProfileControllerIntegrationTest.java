package nz.ac.canterbury.seng302.homehelper.integration.controller;

import nz.ac.canterbury.seng302.homehelper.controller.account.UserProfileController;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.Quote;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.QuoteRepository;
import nz.ac.canterbury.seng302.homehelper.repository.JobRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserProfileControllerIntegrationTest {
    @Autowired
    private UserProfileController userProfileController;
    @Autowired
    private UserService userService;
    @Autowired
    private RenovationRecordService renovationRecordService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private JobRepository jobRepository;
    @MockBean
    private Principal principal;
    @MockBean
    private QuoteRepository quoteRepository;

    private MockMvc mockMvc;
    private User user1;
    private User user2;
    private Job job;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(userProfileController).build();
        user1 = new User("John", "Doe", "john@doe.nz", "P4$$word", null, null);
        user1.setId(1L);
        user2 = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        user2.setId(2L);
        when(principal.getName()).thenReturn("john@doe.nz");
        when(userRepository.findByEmailContainingIgnoreCase("john@doe.nz")).thenReturn(user1);
        when(userRepository.findByEmailContainingIgnoreCase("jane@doe.nz")).thenReturn(user2);
        when(userRepository.findUserById(user1.getId())).thenReturn(user1);
        when(userRepository.findUserById(user2.getId())).thenReturn(user2);
        List<Job> jobs = new ArrayList<>();
        List<Quote> quotes = new ArrayList<>();
        for(int i = 0 ; i < 10 ; i++){
            Job job = new Job("job"+i, "job", "12/12/3030", "12/12/2030");
            Quote quote = new Quote("10", "12", "example@email.com", "1234567890", "desc");
            job.setStatus("Completed");
            quote.setJob(job);
            quote.setStatus("Accepted");
            quotes.add(quote);
        }
        when(quoteRepository.findAllByUserId(user1.getId())).thenReturn(quotes);

        job = new Job(null, null, null, null);
        job.setId(1L);
        job.setStatus("Completed");
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
    }

    @Test
    public void getProfile_OwnUserIdGiven_OwnProfileReturned() throws Exception {
        mockMvc.perform(get("/profile")
                    .param("userId", user1.getId().toString())
                    .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("userProfileTemplate"))
                .andExpect(model().attribute("profileUser", user1))
                .andExpect(model().attribute("ownProfile", true));
    }

    @Test
    public void getProfile_DifferentUserIdGiven_DifferentProfileReturned() throws Exception {
        mockMvc.perform(get("/profile")
                        .param("userId", user2.getId().toString())
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("userProfileTemplate"))
                .andExpect(model().attribute("profileUser", user2))
                .andExpect(model().attribute("ownProfile", false));
    }

    @Test
    public void addCompletedJobToProfile_JobNotInPortfolio_JobAddedToPortfolio() throws Exception {
        assertFalse(user1.getPortfolioJobs().contains(job));
        assertFalse(job.getPortfolioUsers().contains(user1));

        mockMvc.perform(post("/add-to-profile")
                    .param("userId", user1.getId().toString())
                    .param("jobId", job.getId().toString()))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(jobRepository).save(jobCaptor.capture());
        verify(userRepository).save(userCaptor.capture());
        Job capturedJob = jobCaptor.getValue();
        User capturedUser = userCaptor.getValue();
        assertTrue(capturedJob.getPortfolioUsers().contains(user1));
        assertTrue(capturedUser.getPortfolioJobs().contains(job));
    }

    @Test
    public void addCompletedJobToProfile_JobInPortfolio_JobRemovedFromPortfolio() throws Exception {
        user1.addPortfolioJob(job);
        job.addPortfolioUser(user1);
        assertTrue(user1.getPortfolioJobs().contains(job));
        assertTrue(job.getPortfolioUsers().contains(user1));

        mockMvc.perform(post("/add-to-profile")
                        .param("userId", user1.getId().toString())
                        .param("jobId", job.getId().toString()))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(jobRepository).save(jobCaptor.capture());
        verify(userRepository).save(userCaptor.capture());
        Job capturedJob = jobCaptor.getValue();
        User capturedUser = userCaptor.getValue();
        assertFalse(capturedJob.getPortfolioUsers().contains(user1));
        assertFalse(capturedUser.getPortfolioJobs().contains(job));
    }

    @Test
    public void getProfileCompletedJobs_RedirectToPaginatedPage() throws Exception {
        mockMvc.perform(post("/profile")
                .param("userId", user1.getId().toString())
                .param("completedPage", "1")
                .param("newCompletedPage", "2")
                .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/profile?userId=" + user1.getId().toString() + "&completedPage=2&portfolioPage=1"));
    }

    @Test
    public void addJobImage_ValidImageGiven_ImageAddedToJobImageFilenamesList() throws Exception {
        MockMultipartFile file = new MockMultipartFile("submittedFile", "test.png", "image/png", "test".getBytes());

        mockMvc.perform(multipart("/add-job-image")
                        .file(file)
                        .param("jobId", job.getId().toString()))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(jobCaptor.capture());
        Job capturedJob = jobCaptor.getValue();
        String uniqueFileName = "job" + job.getId().toString() + file.getOriginalFilename();
        assertTrue(capturedJob.getImageFilenames().contains(uniqueFileName));
    }

    @Test
    public void addJobImage_InvalidImageGiven_ErrorMessageShown() throws Exception {
        MockMultipartFile file = new MockMultipartFile("submittedFile", "test.pdf", "application/pdf", "test".getBytes());

        mockMvc.perform(multipart("/add-job-image")
                        .file(file)
                        .param("jobId", job.getId().toString()))
                .andExpect(flash().attributeExists("fileErrorMessage"))
                .andExpect(status().is3xxRedirection());


        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository, never()).save(jobCaptor.capture());
    }

    @Test
    public void removeJobImage_ValidImageGiven_ImageAddedToJobImageFilenamesList() throws Exception {
        MockMultipartFile file = new MockMultipartFile("submittedFile", "test.png", "image/png", "test".getBytes());

        mockMvc.perform(multipart("/add-job-image")
                        .file(file)
                        .param("jobId", job.getId().toString()))
                .andExpect(status().is3xxRedirection());

        String uniqueFileName = "job" + job.getId().toString() + file.getOriginalFilename();
        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(jobCaptor.capture());
        Job capturedJob = jobCaptor.getValue();
        assertTrue(capturedJob.getImageFilenames().contains(uniqueFileName));

        mockMvc.perform(post("/remove-job-image")
                .param("jobId", job.getId().toString())
                .param("filename", uniqueFileName))
                .andExpect(status().is3xxRedirection());

        jobCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository, times(2)).save(jobCaptor.capture());
        capturedJob = jobCaptor.getValue();
        assertFalse(capturedJob.getImageFilenames().contains(uniqueFileName));
    }
}
