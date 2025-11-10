package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.controller.job.JobDetailsController;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.repository.*;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
public class UpdateJobIconControllerTest {
    private MockMvc mockMvc;
    @Autowired
    private JobDetailsController jobDetailsController;
    @Autowired
    private JobService jobService;
    @Autowired
    private RenovationRecordService renovationRecordService;
    @Autowired
    private ValidationService validationService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private ExpenseService expenseService;
    @Autowired
    private QuoteService quoteService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TaskScheduler taskScheduler;
    @Autowired
    private UserService userService;
    @MockBean
    private RenovationRecordRepository renovationRecordRepository;
    @MockBean
    private ExpenseRepository expenseRepository;
    @MockBean
    private JobRepository jobRepository;
    @MockBean
    private RoomRepository roomRepository;
    @MockBean
    private RecentRenovationRepository recentRenovationRepository;

    @PostConstruct
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(jobDetailsController).build();
    }


    @Test
    void updateJobIcon_IncorrectRenovationRecord_TakenToErrorPage() throws Exception {
        RenovationRecord renovationRecord1 = new RenovationRecord(
                "reno1","desc1", new ArrayList<>(), "jane@example.com" );
        renovationRecord1.setId(1L);
        renovationRecord1.setJobs(new ArrayList<>());

        RenovationRecord renovationRecord2 = new RenovationRecord(
                "reno2","desc2", new ArrayList<>(), "jane@example.com" );
        renovationRecord2.setId(2L);

        Job job = new Job();
        job.setId(1L);
        job.setRenovationRecord(renovationRecord2);

        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord1));
        when(renovationRecordRepository.findById(2L)).thenReturn(Optional.of(renovationRecord2));
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("jane@example.com");

        mockMvc.perform(post("/my-renovations/update-icon")
                        .param("recordId", renovationRecord1.getId().toString())
                        .param("jobId", job.getId().toString())
                        .param("selectedIcon", "")
                        .principal(principal))
                .andExpect(view().name("error"));
    }

    @Test
    void updateJobIcon_IsValid_Redirected() throws Exception {
        RenovationRecord renovationRecord1 = new RenovationRecord(
                "reno1","desc1", new ArrayList<>(), "jane@example.com" );
        renovationRecord1.setId(1L);

        Job job = new Job();
        job.setId(1L);
        job.setRenovationRecord(renovationRecord1);
        renovationRecord1.setJobs(List.of(job));

        jobService.addJob(job);

        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord1));
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("jane@example.com");

        mockMvc.perform(post("/my-renovations/update-icon")
                        .param("recordId", String.valueOf(renovationRecord1.getId().intValue()))
                        .param("jobId", String.valueOf(job.getId().intValue()))
                        .param("selectedIcon", "")
                        .principal(principal));
        verify(jobRepository, times(2)).save(any());
    }

    @Test
    void updateJobIcon_IsNotAValidIcon_IconNotUpdated() throws Exception {
        RenovationRecord renovationRecord1 = new RenovationRecord(
                "reno1","desc1", new ArrayList<>(), "jane@example.com" );
        renovationRecord1.setId(1L);

        Job job = new Job();
        job.setId(1L);
        job.setRenovationRecord(renovationRecord1);
        renovationRecord1.setJobs(List.of(job));

        jobService.addJob(job);

        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord1));
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("jane@example.com");

        mockMvc.perform(post("/my-renovations/update-icon")
                .param("recordId", String.valueOf(renovationRecord1.getId().intValue()))
                .param("jobId", String.valueOf(job.getId().intValue()))
                .param("selectedIcon", "not an icon") // input not an icon
                .principal(principal));
        verify(jobRepository, times(1)).save(any());
    }
}
