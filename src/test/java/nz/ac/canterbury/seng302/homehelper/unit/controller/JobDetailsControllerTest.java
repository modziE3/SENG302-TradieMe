package nz.ac.canterbury.seng302.homehelper.unit.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.homehelper.controller.job.JobDetailsController;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JobDetailsControllerTest {
    @Mock
    private JobService jobService;
    @Mock
    private RenovationRecordService renovationRecordService;
    @Mock
    private UserService userService;
    @InjectMocks
    private JobDetailsController jobDetailsController;
    @Mock
    private ExpenseService expenseService;
    @Mock
    private QuoteService quoteService;

    private Model model;
    private Principal principal;
    private RenovationRecord testRecord;
    private Job testJob;
    private User testUser;

    @BeforeEach
    public void setup() {
        model = mock(Model.class);
        principal = mock(Principal.class);
        when(principal.getName()).thenReturn("jane@doe.nz");

        testRecord = new RenovationRecord("Record", "Record", List.of(), "jane@doe.nz");
        testRecord.setId(1L);
        testJob = new Job("Job", "Job", null, null);
        testJob.setId(1L);
        testJob.setRenovationRecord(testRecord);
        testRecord.setJobs(List.of(testJob));
        testUser = new User("J", "D", "jane@doe.nz", "P4$$word", null, null);
    }

    @Test
    public void getJobDetailsPage_JobExists_JobDetailsTemplateReturned() {
        testUser.setId(1L);
        when(userService.getUser(principal.getName())).thenReturn(testUser);
        when(userService.getUser("jane@doe.nz")).thenReturn(testUser);

        when(jobService.getJobById(1L)).thenReturn(testJob);

        String viewName = jobDetailsController.getJobDetailsPage(testJob.getId(), 1, 1, false, false, false, model, principal);

        assertEquals("jobDetailsTemplate", viewName);
    }

    @Test
    public void getJobDetailsPage_JobDoesNotExist_ErrorTemplateReturned() {
        String viewName = jobDetailsController.getJobDetailsPage(1000L, 1, 1, false, false, false, model, principal);
        assertEquals("error", viewName);
    }

    @Test
    public void updateJobIcon_RenovationRecordExistsAndJobExists_RedirectedToRenovationDetailsPage() {
        Mockito.when(renovationRecordService.getRecordById(testRecord.getId())).thenReturn(testRecord);
        Mockito.when(principal.getName()).thenReturn(testUser.getEmail());

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        String viewName = jobDetailsController.updateJobIcon(testRecord.getId(), 1L, "", principal, model, request);

        Assertions.assertEquals("redirect:null", viewName);
    }

    @Test
    public void updateJobIcon_RenovationRecordDoesNotExist_ErrorTemplateShown() {
        Mockito.when(renovationRecordService.getRecordById(2L)).thenReturn(null);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        String viewName = jobDetailsController.updateJobIcon(2L, 1L, "", principal, model, request);

        Assertions.assertEquals("error", viewName);
    }

    @Test
    public void updateJobIcon_RenovationRecordExistsAndJobDoesNotExist_ErrorTemplateShown() {
        Mockito.when(renovationRecordService.getRecordById(testRecord.getId())).thenReturn(testRecord);
        Mockito.when(principal.getName()).thenReturn(testUser.getEmail());

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        String viewName = jobDetailsController.updateJobIcon(testRecord.getId(), 231322L, "", principal, model, request);

        Assertions.assertEquals("error", viewName);
    }
}
