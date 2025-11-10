package nz.ac.canterbury.seng302.homehelper.integration.controller;

import nz.ac.canterbury.seng302.homehelper.controller.HomePageController;
import nz.ac.canterbury.seng302.homehelper.entity.*;
import nz.ac.canterbury.seng302.homehelper.entity.dto.JobCardInfo;
import nz.ac.canterbury.seng302.homehelper.repository.JobRepository;
import nz.ac.canterbury.seng302.homehelper.repository.QuoteRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class HomePageControllerIntegrationTest {
    @Autowired
    private HomePageController homePageController;
    @Autowired
    private RenovationRecordService renovationRecordService;
    @Autowired
    private UserService userService;
    @Autowired
    private JobService jobService;
    @MockBean
    private JobRepository jobRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private RenovationRecordRepository renovationRecordRepository;
    @MockBean
    private Principal principal;
//    @Autowired
    private MockMvc mockMvc;

    private User user;
    private User anotherUser;
    private RenovationRecord userRecord;
    private RenovationRecord anotherUserRecord;
    private Job job1;
    private Job job2;
    private Job job3;
    private Job job4;
    private List<Job> postedJobs;
    @Autowired
    private QuoteRepository quoteRepository;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(homePageController).build();
        user = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        when(principal.getName()).thenReturn("jane@doe.nz");
        when(userRepository.findByEmailContainingIgnoreCase("jane@doe.nz")).thenReturn(user);

        userRecord = new RenovationRecord("Record", "Record", List.of(), "jane@doe.nz");
        userRecord.setUserEmail("jane@doe.nz");
        when(renovationRecordRepository.findRenovationRecordsByEmail(anyString())).thenReturn(List.of(userRecord));

        anotherUser = new User("John", "Doe", "john@doe.nz", "P4$$word", null, null);
        when(userRepository.findByEmailContainingIgnoreCase("john@doe.nz")).thenReturn(anotherUser);
        anotherUserRecord = new RenovationRecord("Record", "Record", List.of(), "john@doe.nz");
        anotherUserRecord.setUserEmail("john@doe.nz");

        job1 = new Job("job1", "job1", "28/03/2077", "28/02/2077");
        job1.setIsPosted(true);
        job1.setRenovationRecord(userRecord);
        job1.setStatus("Not Started");
        job1.setId(1L);
        job2 = new Job("job2", "job2", "27/02/2077", "27/02/2077");
        job2.setIsPosted(true);
        job2.setRenovationRecord(userRecord);
        job2.setStatus("Completed");
        job2.setId(2L);
        job3 = new Job("job3", "job3", "28/03/2077", null);
        job3.setIsPosted(true);
        job3.setRenovationRecord(anotherUserRecord);
        job3.setId(3L);
        job4 = new Job("job4", "job4", null, null);
        job4.setIsPosted(true);
        job4.setRenovationRecord(anotherUserRecord);
        job4.setId(4L);
        postedJobs = List.of(job1, job2, job3, job4);
        userRecord.setJobs(List.of(job1, job2));
        when(jobRepository.findPostedJobs()).thenReturn(postedJobs);
    }

    @Test
    public void getLoggedInHomePage_UserHasNoLocation_PostedJobsUserDoesNotOwnReturned() throws Exception {
        List<Job> expectedJobs = List.of(job4, job3);
        List<JobCardInfo> expectedJobCards = jobService.getJobCardsPosted(userService, expectedJobs);

        MvcResult mvcResult = mockMvc.perform(get("/home")
                    .param("name", "Jane")
                    .principal(principal))
                .andExpect(status().isOk())
                .andReturn();

        List<JobCardInfo> jobCards = (List<JobCardInfo>) mvcResult.getModelAndView().getModel().get("recommendedJobs");
        Assertions.assertNotNull(jobCards);
        assertTrue(jobCards.containsAll(expectedJobCards));
    }

    @Test
    public void getLoggedInHomePage_UserHasLocation_PostedJobsInSameLocationAppearFirst() throws Exception {
        user.setCity("Christchurch");
        RenovationRecord anotherUserRecord1 = new RenovationRecord("Record", "Record", List.of(), "john@doe.nz");
        anotherUserRecord1.setCity("Christchurch");
        RenovationRecord anotherUserRecord2 = new RenovationRecord("Record", "Record", List.of(), "john@doe.nz");
        anotherUserRecord2.setCity("Auckland");

        job1.setRenovationRecord(anotherUserRecord1);
        job2.setRenovationRecord(anotherUserRecord2);
        job3.setRenovationRecord(anotherUserRecord1);
        job4.setRenovationRecord(anotherUserRecord2);

        List<Job> expectedJobs = List.of(job3, job1, job2, job4);
        List<JobCardInfo> expectedJobCards = jobService.getJobCardsPosted(userService, expectedJobs);

        MvcResult mvcResult = mockMvc.perform(get("/home")
                        .param("name", "Jane")
                        .principal(principal))
                .andExpect(status().isOk())
                .andReturn();

        List<JobCardInfo> jobCards = (List<JobCardInfo>) mvcResult.getModelAndView().getModel().get("recommendedJobs");
        Assertions.assertNotNull(jobCards);
        assertTrue(jobCards.containsAll(expectedJobCards));
        JobCardInfo job1Card = jobService.getJobCardsPosted(userService, List.of(job1)).getFirst();
        JobCardInfo job3Card = jobService.getJobCardsPosted(userService, List.of(job3)).getFirst();
        assertTrue(jobCards.getFirst().equals(job3Card) || jobCards.getFirst().equals(job1Card));
        assertTrue(jobCards.get(1).equals(job1Card) || jobCards.get(1).equals(job3Card));
    }

    @Test
    public void getLoggedInHomePage_UserHasRenovationRecordsAndJobs_RecordJobNamesPassedToCalendarWidget() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/home")
                    .param("name", "Jane")
                    .principal(principal))
                .andExpect(status().isOk())
                .andReturn();

        List<String> jobNames = (List<String>) mvcResult.getModelAndView().getModel().get("jobNames");
        assertTrue(jobNames.contains("job1"));
        assertTrue(jobNames.contains("job2"));
    }

    @Test
    public void getLoggedInHomePage_UserHasRenovationRecordsAndJobs_RecordJobStartDatesPassedToCalendarWidget() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/home")
                        .param("name", "Jane")
                        .principal(principal))
                .andExpect(status().isOk())
                .andReturn();

        List<String> jobStartDates = (List<String>) mvcResult.getModelAndView().getModel().get("jobStartDates");
        assertTrue(jobStartDates.contains("2077-02-28"));
        assertTrue(jobStartDates.contains("2077-02-27"));
    }

    @Test
    public void getLoggedInHomePage_UserHasRenovationRecordsAndJobs_RecordJobDueDatesPassedToCalendarWidget() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/home")
                        .param("name", "Jane")
                        .principal(principal))
                .andExpect(status().isOk())
                .andReturn();

        List<String> jobDueDates = (List<String>) mvcResult.getModelAndView().getModel().get("jobDueDates");
        assertTrue(jobDueDates.contains("2077-03-29"));
        assertTrue(jobDueDates.contains("2077-02-28"));
    }

    @Test
    public void getLoggedInHomePage_UserHasRenovationRecordsAndJobs_RecordJobStatusesPassedToCalendarWidget() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/home")
                        .param("name", "Jane")
                        .principal(principal))
                .andExpect(status().isOk())
                .andReturn();

        List<String> jobStatuses = (List<String>) mvcResult.getModelAndView().getModel().get("jobStatuses");
        assertTrue(jobStatuses.contains("Not Started"));
        assertTrue(jobStatuses.contains("Completed"));
    }

    @Test
    public void getLoggedInHomePage_UserHasNoRecentJobs_RecentJobsIsEmpty() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/home")
                        .param("name", "Jane")
                        .principal(principal))
                .andExpect(status().isOk())
                .andReturn();

        List<JobCardInfo> jobCards = (List<JobCardInfo>) mvcResult.getModelAndView().getModel().get("recentJobs");
        Assertions.assertNotNull(jobCards);
        assertTrue(jobCards.isEmpty());
    }

    @Test
    public void getLoggedInHomePage_UserVisitsAJobPage_RecentJobsContainVisitedJob() throws Exception {
        user.setRecentJobs(List.of(job3.getId()));
        when(jobRepository.findById(job3.getId())).thenReturn(Optional.ofNullable(job3));

        MvcResult mvcResult = mockMvc.perform(get("/home")
                        .param("name", "Jane")
                        .principal(principal))
                .andExpect(status().isOk())
                .andReturn();

        List<JobCardInfo> jobCards = (List<JobCardInfo>) mvcResult.getModelAndView().getModel().get("recentJobs");
        Assertions.assertNotNull(jobCards);
        assertFalse(jobCards.isEmpty());
    }
}
