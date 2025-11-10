package nz.ac.canterbury.seng302.homehelper.unit.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.controller.job.PostJobFormController;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PostJobFormControllerTest {
    @Mock
    private RenovationRecordService renovationRecordService;
    @Mock
    private JobService jobService;
    @Mock
    private UserService userService;
    @InjectMocks
    private PostJobFormController postJobFormController;

    @Mock
    private Model model;
    @Mock
    private Principal principal;
    @Mock
    private HttpServletRequest request;
    private Job testJob;

    @BeforeEach
    public void setUp() {
        testJob = new Job("Job", "Job", null, null);
        testJob.setId(1L);
    }

    @Test
    public void updateJobPostedStatus_JobDetailsValid_RedirectedToPreviousPage() {
        when(jobService.getJobById(Mockito.anyLong())).thenReturn(testJob);
        when(request.getHeader(Mockito.anyString())).thenReturn("test");

        when(userService.getUser(any())).thenReturn(Mockito.mock(User.class));


        String viewName = postJobFormController.updateJobPostedStatus(testJob.getId(), true, model, principal, request);
        assertEquals("redirect:test", viewName);
    }

    @Test
    public void updateJobPostedStatus_JobDetailsInvalid_RedirectedToPostJobFormPage() {
        when(jobService.getJobById(Mockito.anyLong())).thenReturn(testJob);
        doThrow(IllegalArgumentException.class).when(jobService).validateJobBeforePosting(testJob);

        String viewName = postJobFormController.updateJobPostedStatus(testJob.getId(), true, model, principal, request);
        assertEquals("redirect:/my-renovations/post-job?jobId=" + testJob.getId(), viewName);
    }
}
