package nz.ac.canterbury.seng302.homehelper.integration.controller;

import nz.ac.canterbury.seng302.homehelper.controller.job.PostJobFormController;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.JobRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class PostFormControllerIntegrationTest {
    @Autowired
    private PostJobFormController postJobFormController;
    @Autowired
    private RenovationRecordService renovationRecordService;
    @Autowired
    private JobService jobService;
    @MockBean
    private RenovationRecordRepository renovationRecordRepository;
    @MockBean
    private JobRepository jobRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private UserService userService;
    @MockBean
    private Principal principal;

    private MockMvc mockMvc;
    private Job job;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(postJobFormController).build();

        when(userService.getUser(any())).thenReturn(Mockito.mock(User.class));
        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(Mockito.mock(User.class)));

        job = new Job("Job", "Job", null, null);
        job.setId(1L);
        job.setType("No Type");
        when(jobRepository.findById(Mockito.anyLong())).thenReturn(Optional.ofNullable(job));
    }

    @Test
    public void updateJobPostedStatus_JobDetailsValidAndPostTrue_JobPostedStatusUpdated() throws Exception {
        job.setType("Carpentry");
        job.setStartDate("01/12/3025");
        job.setDueDate("12/12/3025");

        mockMvc.perform(post("/my-renovations/update-posted-status")
                    .param("jobId", job.getId().toString())
                    .param("posted", "true")
                    .principal(principal))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<Job> jobArgumentCaptor = ArgumentCaptor.forClass(Job.class);
        Mockito.verify(jobRepository).save(jobArgumentCaptor.capture());
        job = jobArgumentCaptor.getValue();
        assertTrue(job.getIsPosted());
    }

    @Test
    public void updateJobPostedStatus_JobDetailsValidAndPostFalse_JobPostedStatusUpdated() throws Exception {
        job.setType("Carpentry");
        job.setStartDate("01/12/3025");
        job.setDueDate("12/12/3025");
        job.setIsPosted(true);



        mockMvc.perform(post("/my-renovations/update-posted-status")
                    .param("jobId", job.getId().toString())
                    .param("posted", "false")
                    .principal(principal))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<Job> jobArgumentCaptor = ArgumentCaptor.forClass(Job.class);
        Mockito.verify(jobRepository).save(jobArgumentCaptor.capture());
        job = jobArgumentCaptor.getValue();
        assertFalse(job.getIsPosted());
    }

    @Test
    public void updateJobPostedStatus_JobDetailsInvalidAndPostTrue_RedirectedToPostJobForm() throws Exception {
        mockMvc.perform(post("/my-renovations/update-posted-status")
                    .param("jobId", job.getId().toString())
                    .param("posted", "true")
                    .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/my-renovations/post-job?jobId=" + job.getId()));

        ArgumentCaptor<Job> jobArgumentCaptor = ArgumentCaptor.forClass(Job.class);
        Mockito.verify(jobRepository, never()).save(jobArgumentCaptor.capture());
        assertFalse(job.getIsPosted());
    }

    @Test
    public void getPostJobForm_JobDetailsInvalid_PostJobFormReturned() throws Exception {
        mockMvc.perform(get("/my-renovations/post-job")
                    .param("jobId", job.getId().toString())
                    .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("postJobTemplate"))
                .andExpect(model().attribute("jobTypeErrorMessage", JobService.JOB_TYPE_INVALID))
                .andExpect(model().attribute("jobStartDateErrorMessage", JobService.JOB_START_DATE_EMPTY))
                .andExpect(model().attribute("jobDueDateErrorMessage", JobService.JOB_DUE_DATE_EMPTY));
    }

    @Test
    public void editAndPostJob_JobDetailsValid_JobPostedStatusUpdated() throws Exception {
        mockMvc.perform(post("/my-renovations/post-job")
                    .param("jobId", job.getId().toString())
                    .param("jobName", job.getName())
                    .param("jobDescription", job.getDescription())
                    .param("jobType", "Electrical")
                    .param("jobStartDate", "01/12/3025")
                    .param("jobDueDate", "12/12/3025")
                    .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/my-renovations/job-details?jobId=" + job.getId()));

        ArgumentCaptor<Job> jobArgumentCaptor = ArgumentCaptor.forClass(Job.class);
        Mockito.verify(jobRepository, times(2)).save(jobArgumentCaptor.capture());
        job = jobArgumentCaptor.getValue();
        assertTrue(job.getIsPosted());
    }

    @Test
    public void editAndPostJob_JobDetailsInvalid_JobPostedStatusNotUpdated() throws Exception {
        mockMvc.perform(post("/my-renovations/post-job")
                        .param("jobId", job.getId().toString())
                        .param("jobName", job.getName())
                        .param("jobDescription", job.getDescription())
                        .param("jobType", "No Type")
                        .param("jobStartDate", "01/12/3025")
                        .param("jobDueDate", "12/12/3025")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("postJobTemplate"))
                .andExpect(model().attribute("jobId", job.getId()))
                .andExpect(model().attribute("jobName", job.getName()))
                .andExpect(model().attribute("jobDescription", job.getDescription()))
                .andExpect(model().attribute("jobType", "No Type"))
                .andExpect(model().attribute("jobStartDate", "01/12/3025"))
                .andExpect(model().attribute("jobDueDate", "12/12/3025"));

        ArgumentCaptor<Job> jobArgumentCaptor = ArgumentCaptor.forClass(Job.class);
        Mockito.verify(jobRepository, never()).save(jobArgumentCaptor.capture());
        assertFalse(job.getIsPosted());
    }
}
