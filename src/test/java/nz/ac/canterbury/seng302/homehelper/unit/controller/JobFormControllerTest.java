package nz.ac.canterbury.seng302.homehelper.unit.controller;

import nz.ac.canterbury.seng302.homehelper.controller.job.JobFormController;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.security.Principal;
import java.text.ParseException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JobFormControllerTest {
    @Mock
    private JobService jobService;
    @Mock
    private RenovationRecordService renovationRecordService;
    @InjectMocks
    private JobFormController jobFormController;
    @Mock
    private Principal principal;
    @Mock
    private Model model;
    private Job job;

    @BeforeEach
    public void setUp() {
        RenovationRecord renovationRecord = new RenovationRecord("Record", "Record", List.of(), "jane@doe.nz");
        renovationRecord.setId(1L);
        job = new Job("Job", "Job", null, null);
        job.setId(1L);
        when(principal.getName()).thenReturn("jane@doe.nz");
        when(renovationRecordService.getAndAuthenticateRecord(anyLong(), anyString())).thenReturn(renovationRecord);
    }

    @Test
    public void getCreateJobForm_RecordExists_JobFormTemplateReturned() {
        String viewName = jobFormController.getCreateJobForm(1L, "", false, false, principal, model);
        assertEquals("jobFormTemplate", viewName);
    }

    @Test
    public void getCreateJobForm_RecordDoesNotExist_JobFormTemplateReturned() {
        when(renovationRecordService.getAndAuthenticateRecord(anyLong(), anyString())).thenReturn(null);
        String viewName = jobFormController.getCreateJobForm(1L, "", false, false, principal, model);
        assertEquals("error", viewName);
    }

    @Test
    public void postNewJob_RecordExists_CorrectRedirectReturned() {
        Job job = new Job("Job", "Job", null, null);
        job.setId(1L);
        when(jobService.addJob(any())).thenReturn(job);
        String viewName = jobFormController.postNewJob(1L, "Job", "Job", "Type",
                "Due date", "Start date", "", "", false, true, principal, model);
        assertEquals("redirect:/my-renovations/job-details?jobId=1&search=", viewName);
    }

    @Test
    public void postNewJob_RecordExistsAndFormAccessedFromCalendar_CorrectRedirectReturned() {
        Job job = new Job("Job", "Job", null, null);
        job.setId(1L);
        when(jobService.addJob(any())).thenReturn(job);
        String viewName = jobFormController.postNewJob(1L, "Job", "Job", "Type",
                "Due date", "Start date", "", "", true, false, principal, model);
        assertEquals("redirect:/my-renovations/details?recordId=1&search=", viewName);
    }

    @Test
    public void postNewJob_RecordDoesNotExist_ErrorPageReturned() {
        when(renovationRecordService.getAndAuthenticateRecord(anyLong(), anyString())).thenReturn(null);
        String viewName = jobFormController.postNewJob(1L, "Job", "Job", "Type",
                "Due date", "Start date", "", "", false, false, principal, model);
        assertEquals("error", viewName);
    }

    @Test
    public void postNewJob_InvalidJobDetails_JobFormTemplateReturned() {
        doThrow(IllegalArgumentException.class).when(jobService).validateJob(any());
        when(jobService.getErrorMessages(any())).thenReturn(List.of("", "", "", "", ""));
        String viewName = jobFormController.postNewJob(1L, "Job", "Job", "Type",
                "Due date", "Start date", "", "", false, false, principal, model);
        assertEquals("jobFormTemplate", viewName);
    }

    @Test
    public void getEditJobForm_RecordExists_JobFormTemplateReturned() {
        when(jobService.getAndAuthenticateJob(anyLong(), any())).thenReturn(job);
        String viewName = jobFormController.getEditJobForm(1L, 1L, "", false, false, false, model, principal);
        assertEquals("jobFormTemplate", viewName);
    }

    @Test
    public void getEditJobForm_RecordDoesNotExist_JobFormTemplateReturned() {
        when(renovationRecordService.getAndAuthenticateRecord(anyLong(), anyString())).thenReturn(null);
        String viewName = jobFormController.getEditJobForm(1L, 1L, "", false, false, false, model, principal);
        assertEquals("error", viewName);
    }

    @Test
    public void postEditJob_JobExists_CorrectRedirectReturned() {
        when(jobService.getAndAuthenticateJob(anyLong(), any())).thenReturn(job);
        String viewName = jobFormController.postEditJob(1L, 1L, "Job", "Job", "Type",
                "Due date", "Start date", "", "", false, false, false, model, principal);
        assertEquals("redirect:/my-renovations/details?recordId=1&search=", viewName);
    }

    @Test
    public void postEditJob_JobExistsAndFormAccessedFromCalendar_CorrectRedirectReturned() {
        when(jobService.getAndAuthenticateJob(anyLong(), any())).thenReturn(job);
        String viewName = jobFormController.postEditJob(1L, 1L, "Job", "Job", "Type",
                "Due date", "Start date", "", "", true, false, false, model, principal);
        assertEquals("redirect:/my-renovations/details?recordId=1&search=", viewName);
    }

    @Test
    public void postEditJob_JobExistsAndFormAccessedFromJobDetailsPage_CorrectRedirectReturned() {
        when(jobService.getAndAuthenticateJob(anyLong(), any())).thenReturn(job);
        String viewName = jobFormController.postEditJob(1L, 1L, "Job", "Job", "Type",
                "Due date", "Start date", "", "", false, true, false, model, principal);
        assertEquals("redirect:/my-renovations/job-details?jobId=1&search=", viewName);
    }

    @Test
    public void postEditJob_JobExistsAndFormAccessedFromHomePageWidget_CorrectRedirectReturned() {
        when(jobService.getAndAuthenticateJob(anyLong(), any())).thenReturn(job);
        String viewName = jobFormController.postEditJob(1L, 1L, "Job", "Job", "Type",
                "Due date", "Start date", "", "", false, false, true, model, principal);
        assertEquals("redirect:/home", viewName);
    }

    @Test
    public void postEditJob_JobDoesNotExist_CorrectRedirectReturned() {
        when(renovationRecordService.getAndAuthenticateRecord(anyLong(), anyString())).thenReturn(null);
        when(jobService.getAndAuthenticateJob(anyLong(), any())).thenReturn(null);
        String viewName = jobFormController.postEditJob(1L, 1L, "Job", "Job", "Type",
                "Due date", "Start date", "", "", false, false, false, model, principal);
        assertEquals("error", viewName);
    }

    @Test
    public void postEditJob_InvalidJobDetails_JobFormTemplateReturned() throws ParseException {
        when(jobService.getAndAuthenticateJob(anyLong(), any())).thenReturn(job);
        doThrow(IllegalArgumentException.class).when(jobService).editJob(any(), any(), any(), any(), any(), any());
        when(jobService.getErrorMessages(any())).thenReturn(List.of("", "", "", "", ""));
        String viewName = jobFormController.postEditJob(1L, 1L, "Job", "Job", "Type",
                "Due date", "Start date", "", "", false, false, false, model, principal);
        assertEquals("jobFormTemplate", viewName);
    }
}
