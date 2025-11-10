package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;

import nz.ac.canterbury.seng302.homehelper.controller.job.JobFormController;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RoomRepository;
import nz.ac.canterbury.seng302.homehelper.repository.JobRepository;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
public class JobFormControllerIntegrationTest {
    @Autowired
    private JobFormController jobFormController;
    private MockMvc mockMvc;
    @MockBean
    private JobRepository jobRepository;
    @MockBean
    private RenovationRecordRepository renovationRecordRepository;
    @MockBean
    private RoomRepository roomRepository;
    @MockBean
    private Principal mockPrincipal;

    private RenovationRecord record;
    private Job job;

    @PostConstruct
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(jobFormController).build();
        when(mockPrincipal.getName()).thenReturn("jane@doe.nz");

        record = new RenovationRecord("Renovation", "Renovation", new ArrayList<>(), String.valueOf(mockPrincipal.getName()));
        record.setId(1L);
        job = new Job("Job", "Job", null, null);
        job.setId(1L);
        job.setRenovationRecord(record);
        record.setJobs(List.of(job));
        when(renovationRecordRepository.findById(record.getId())).thenReturn(Optional.of(record));
        when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
    }

    @Test
    void testGetJobForm() throws Exception {
        mockMvc.perform(get("/my-renovations/create-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("search", "false")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(view().name("jobFormTemplate"))
                .andExpect(model().attribute("record", record))
                .andExpect(model().attribute("renoRooms", "[]"))
                .andExpect(model().attribute("descriptionLength", "0/512"));
    }

    @Test
    void testGetJobForm_RecordNotFound() throws Exception {
        mockMvc.perform(get("/my-renovations/create-job")
                        .param("recordId", Long.toString(2L))
                        .param("search", "false")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    void testPostJobForm_RecordNotFound() throws Exception {
        mockMvc.perform(post("/my-renovations/create-job")
                        .param("recordId", Long.toString(2L))
                        .param("newJobName", "Job 1")
                        .param("jobDescription", "Job 1 Description")
                        .param("jobType", "No Type")
                        .param("jobDueDate", "01/01/3025")
                        .param("jobStartDate", "01/01/3024")
                        .param("selectedRooms", "")
                        .param("search", "false")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    void testPostJobForm_validInputWithoutRooms_callsJobRepository() throws Exception {
        when(jobRepository.save(any())).thenReturn(job);

        mockMvc.perform(post("/my-renovations/create-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("newJobName", "Job 1")
                        .param("jobDescription", "Job 1 Description")
                        .param("jobType", "No Type")
                        .param("jobDueDate", "01/01/3025")
                        .param("jobStartDate", "01/01/3024")
                        .param("selectedRooms", "")
                        .param("search", "false")
                        .principal(mockPrincipal))
                .andExpect(view().name("redirect:/my-renovations/job-details?jobId=" + job.getId()
                        + "&search=false"));

        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        Mockito.verify(jobRepository).save(jobCaptor.capture());
        Job capturedJob = jobCaptor.getValue();
        Assertions.assertEquals("Job 1", capturedJob.getName());
        Assertions.assertEquals("Job 1 Description", capturedJob.getDescription());
        Assertions.assertEquals("01/01/3025", capturedJob.getDueDate());
        Assertions.assertEquals("01/01/3024", capturedJob.getStartDate());
    }

    @Test
    void testPostJobForm_validInputWithRooms_callsJobRepository() throws Exception {
        List<Room> testRooms = new ArrayList<>(List.of(new Room("Kitchen"), new Room("Bathroom"), new Room("Laundry")));
        for (Room room : testRooms) {
            room.setRenovationRecord(record);
            roomRepository.save(room);
        }
        record.setRooms(testRooms);
        when(jobRepository.save(any())).thenReturn(job);

        mockMvc.perform(post("/my-renovations/create-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("newJobName", "Job 1")
                        .param("jobDescription", "Job 1 Description")
                        .param("jobType", "No Type")
                        .param("jobDueDate", "01/01/3025")
                        .param("jobStartDate", "01/01/3024")
                        .param("selectedRooms", "Kitchen, Laundry")
                        .param("search", "false")
                        .principal(mockPrincipal))
                .andExpect(view().name("redirect:/my-renovations/job-details?jobId=" + job.getId()
                        + "&search=false"));

        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        Mockito.verify(jobRepository).save(jobCaptor.capture());
        Job capturedJob = jobCaptor.getValue();
        for (Room room : testRooms) {
            capturedJob.addRoom(room);
        }
        Assertions.assertEquals("Job 1", capturedJob.getName());
        Assertions.assertEquals("Job 1 Description", capturedJob.getDescription());
        Assertions.assertEquals("01/01/3025", capturedJob.getDueDate());
        Assertions.assertEquals("01/01/3024", capturedJob.getStartDate());
        Assertions.assertEquals(List.of("Kitchen", "Bathroom", "Laundry"), capturedJob.getRooms().stream().map(Room::getName).toList());
    }

    static Stream<Arguments> invalidNames() {
        return Stream.of(
                Arguments.of("", JobService.JOB_NAME_EMPTY_OR_INVALID),
                Arguments.of("Job !", JobService.JOB_NAME_EMPTY_OR_INVALID),
                Arguments.of("Job #", JobService.JOB_NAME_EMPTY_OR_INVALID),
                Arguments.of("Job ()", JobService.JOB_NAME_EMPTY_OR_INVALID),
                Arguments.of("Job :", JobService.JOB_NAME_EMPTY_OR_INVALID),
                Arguments.of("A".repeat(256), JobService.JOB_NAME_OVER_255_CHARS)
        );
    }
    @ParameterizedTest
    @MethodSource("invalidNames")
    void testPostJobForm_invalidInputName_invalidNameMessage(String invalidName, String expectedErrorMessage)
            throws Exception {
        mockMvc.perform(post("/my-renovations/create-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("newJobName", invalidName)
                        .param("jobDescription", "Job 1 Description")
                        .param("jobType", "No Type")
                        .param("jobDueDate", "01/01/3025")
                        .param("jobStartDate", "01/01/3024")
                        .param("search", "false")
                        .principal(mockPrincipal))
                .andExpect(view().name("jobFormTemplate"))
                .andExpect(model().attribute("newJobNameErrorMessage", expectedErrorMessage));
    }

    static Stream<Arguments> invalidDescriptions() {
        return Stream.of(
                Arguments.of("", JobService.JOB_DESCRIPTION_EMPTY),
                Arguments.of("A".repeat(513), JobService.JOB_DESCRIPTION_OVER_512_CHARS)
        );
    }
    @ParameterizedTest
    @MethodSource("invalidDescriptions")
    void testPostJobForm_invalidInputDescriptionBlank_invalidDescriptionMessage(
            String invalidDescription, String expectedErrorMessage) throws Exception {
        mockMvc.perform(post("/my-renovations/create-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("newJobName", "Job")
                        .param("jobDescription", invalidDescription)
                        .param("jobType", "No Type")
                        .param("jobDueDate", "01/01/3025")
                        .param("jobStartDate", "")
                        .principal(mockPrincipal))
                .andExpect(view().name("jobFormTemplate"))
                .andExpect(model().attribute("jobDescriptionErrorMessage", expectedErrorMessage));
    }

    static Stream<Arguments> invalidDueDates() {
        // Code for getting yesterday's date provided from Stack Overflow: https://stackoverflow.com/questions/55394546/how-to-get-yesterday-instance-from-calendar/55394637
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);

        return Stream.of(
                Arguments.of("3025/01/01", JobService.JOB_DUE_DATE_INVALID_FORMAT),
                Arguments.of("01-01-3025", JobService.JOB_DUE_DATE_INVALID_FORMAT),
                Arguments.of(sdf.format(calendar.getTime()), JobService.JOB_DUE_DATE_NOT_IN_FUTURE),
                Arguments.of("30/02/3025", JobService.JOB_DUE_DATE_INVALID_DATE)
        );
    }
    @ParameterizedTest
    @MethodSource("invalidDueDates")
    void testPostJobForm_invalidInputDueDate_invalidDueDateMessage(String invalidDueDate, String expectedErrorMessage)
            throws Exception {
        mockMvc.perform(post("/my-renovations/create-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("newJobName", "Job")
                        .param("jobDescription", "Job 1 Description")
                        .param("jobType", "No Type")
                        .param("jobDueDate", invalidDueDate)
                        .param("jobStartDate", "")
                        .principal(mockPrincipal))
                .andExpect(view().name("jobFormTemplate"))
                .andExpect(model().attribute("jobDueDateErrorMessage", expectedErrorMessage));
    }

    @Test
    void testGetEditJobForm_JobFound() throws Exception {
        mockMvc.perform(get("/my-renovations/edit-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("jobId", Long.toString(job.getId()))
                        .param("search", "false")
                        .principal(mockPrincipal))
                .andExpect(view().name("jobFormTemplate"))
                .andExpect(model().attribute("record", record))
                .andExpect(model().attribute("job", job))
                .andExpect(model().attribute("jobIcon", job.getIcon()))
                .andExpect(model().attribute("newJobName", job.getName()))
                .andExpect(model().attribute("jobDescription", job.getDescription()))
                .andExpect(model().attribute("descriptionLength", job.getDescription().length() + "/512"))
                .andExpect(model().attribute("jobDueDate", job.getDueDate()))
                .andExpect(model().attribute("renoRooms", "[" + String.join("`", record.getRooms().stream().map(Room::getName).toList()) + "]"))
                .andExpect(model().attribute("jobRooms", job.getRooms().stream().map(Room::getName).toList()))
                .andExpect(model().attribute("mode", "edit"));
    }

    @Test
    void testGetEditJobForm_JobNotFound() throws Exception {
        mockMvc.perform(get("/my-renovations/edit-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("jobId", Long.toString(2L))
                        .param("search", "false")
                        .principal(mockPrincipal))
                .andExpect(view().name("error"));
    }

    @Test
    void testPostEditJobForm_JobNotFound() throws Exception {
        mockMvc.perform(post("/my-renovations/edit-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("jobId", Long.toString(2L))
                        .param("newJobName", "New Job Name")
                        .param("jobDescription", "New Job Description")
                        .param("jobType", "No Type")
                        .param("jobDueDate", "01/01/3025")
                        .param("jobStartDate", "01/01/3024")
                        .param("selectedRooms", "")
                        .principal(mockPrincipal))
                .andExpect(view().name("error"));
    }

    @Test
    void testPostEditJobForm_validInputWithoutRooms_callsJobRepository() throws Exception {
        mockMvc.perform(post("/my-renovations/edit-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("jobId", Long.toString(job.getId()))
                        .param("newJobName", "New Job Name")
                        .param("jobDescription", "New Job Description")
                        .param("jobType", "No Type")
                        .param("jobDueDate", "01/01/3025")
                        .param("jobStartDate", "")
                        .param("selectedRooms", "")
                        .param("search", "false-")
                        .param("fromJob", "true")
                        .principal(mockPrincipal))
                .andExpect(view().name("redirect:/my-renovations/job-details?jobId="+job.getId() + "&search=false-"));

        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        Mockito.verify(jobRepository).save(jobCaptor.capture());
        Job capturedJob = jobCaptor.getValue();
        Assertions.assertEquals("New Job Name", capturedJob.getName());
        Assertions.assertEquals("New Job Description", capturedJob.getDescription());
        Assertions.assertEquals("01/01/3025", capturedJob.getDueDate());
    }

    @Test
    void testPostEditJobForm_validInputWithRooms_callsJobRepository() throws Exception {
        List<Room> testRooms = List.of(new Room("Kitchen"), new Room("Bathroom"), new Room("Laundry"));
        record.setRooms(testRooms);
        for (Room room : testRooms) {
            room.setRenovationRecord(record);
            roomRepository.save(room);
        }

        mockMvc.perform(post("/my-renovations/edit-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("jobId", Long.toString(job.getId()))
                        .param("newJobName", "New Job Name")
                        .param("jobDescription", "New Job Description")
                        .param("jobType", "No Type")
                        .param("jobDueDate", "01/01/3025")
                        .param("jobStartDate", "01/01/3024")
                        .param("selectedRooms", "Kitchen,Laundry")
                        .param("search", "false")
                        .param("fromJob", "true")
                        .principal(mockPrincipal))
                .andExpect(view().name("redirect:/my-renovations/job-details?jobId="+job.getId()
                        + "&search=false"));

        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        Mockito.verify(jobRepository).save(jobCaptor.capture());
        Job capturedJob = jobCaptor.getValue();
        for (Room room : testRooms) {
            capturedJob.addRoom(room);
        }
        Assertions.assertEquals("New Job Name", capturedJob.getName());
        Assertions.assertEquals("New Job Description", capturedJob.getDescription());
        Assertions.assertEquals("01/01/3025", capturedJob.getDueDate());
        Assertions.assertEquals(List.of("Kitchen", "Bathroom", "Laundry"), capturedJob.getRooms().stream().map(Room::getName).toList());
    }

    @ParameterizedTest
    @MethodSource("invalidNames")
    void testPostEditJobForm_invalidInputName_invalidNameMessage(String invalidName, String expectedErrorMessage)
            throws Exception {
        mockMvc.perform(post("/my-renovations/edit-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("jobId", Long.toString(job.getId()))
                        .param("newJobName", invalidName)
                        .param("jobDescription", "New Job Description")
                        .param("jobType", "No Type")
                        .param("jobDueDate", "01/01/3025")
                        .param("jobStartDate", "")
                        .param("selectedRooms", "")
                        .param("search", "false")
                        .principal(mockPrincipal))
                .andExpect(view().name("jobFormTemplate"))
                .andExpect(model().attribute("newJobNameErrorMessage", expectedErrorMessage));
    }

    @ParameterizedTest
    @MethodSource("invalidDescriptions")
    void testPostEditJobForm_invalidInputDescription_invalidDescriptionMessage(
            String invalidDescription, String expectedErrorMessage) throws Exception {
        mockMvc.perform(post("/my-renovations/edit-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("jobId", Long.toString(job.getId()))
                        .param("newJobName", "New Job Name")
                        .param("jobDescription", invalidDescription)
                        .param("jobType", "No Type")
                        .param("jobDueDate", "01/01/3025")
                        .param("jobStartDate", "01/01/3024")
                        .param("selectedRooms", "")
                        .param("search", "false")
                        .principal(mockPrincipal))
                .andExpect(view().name("jobFormTemplate"))
                .andExpect(model().attribute("jobDescriptionErrorMessage", expectedErrorMessage));
    }

    @ParameterizedTest
    @MethodSource("invalidDueDates")
    void testPostEditJobForm_invalidInputDueDates_invalidDescriptionMessage(
            String invalidDueDate, String expectedErrorMessage) throws Exception {
        mockMvc.perform(post("/my-renovations/edit-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("jobId", Long.toString(job.getId()))
                        .param("newJobName", "New Job Name")
                        .param("jobDescription", "New Job Description")
                        .param("jobType", "No Type")
                        .param("jobDueDate", invalidDueDate)
                        .param("jobStartDate", "20/12/2024")
                        .param("selectedRooms", "")
                        .param("search", "false")
                        .principal(mockPrincipal))
                .andExpect(view().name("jobFormTemplate"))
                .andExpect(model().attribute("jobDueDateErrorMessage", expectedErrorMessage));
    }

    @Test
    void testGetPostJobForm_validInput_EmojiDescription() throws Exception {
        mockMvc.perform(post("/my-renovations/create-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("newJobName", "Job")
                        .param("jobDescription", "🤩")
                        .param("jobType", "No Type")
                        .param("jobDueDate", "12/10/2025")
                        .param("jobStartDate", "12/12/2025")
                        .principal(mockPrincipal))
                .andExpect(view().name("jobFormTemplate"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetEditJobForm_AccessedFromCalendar_FromCalendarTrue() throws Exception {
        mockMvc.perform(get("/my-renovations/edit-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("jobId", Long.toString(job.getId()))
                        .param("search", "false")
                        .param("fromCalendar", "true")
                        .principal(mockPrincipal))
                .andExpect(view().name("jobFormTemplate"))
                .andExpect(model().attribute("mode", "edit"))
                .andExpect(model().attribute("fromCalendar", true));
    }

    @Test
    public void testPostJobForm_AccessedFromCalendarAndInvalidInputs_FromCalendarTrue() throws Exception {
        mockMvc.perform(post("/my-renovations/create-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("newJobName", "")
                        .param("jobDescription", "")
                        .param("jobType", "No Type")
                        .param("jobDueDate", "")
                        .param("jobStartDate", "")
                        .param("search", "false")
                        .param("fromCalendar", "true")
                        .principal(mockPrincipal))
                .andExpect(view().name("jobFormTemplate"))
                .andExpect(model().attribute("fromCalendar", true))
                .andExpect(model().attribute("fromJob", false));
    }

    @Test
    public void testPostJobForm_AccessedFromJobDetailsPageAndInvalidInputs_FromJobTrue() throws Exception {
        mockMvc.perform(post("/my-renovations/create-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("newJobName", "")
                        .param("jobDescription", "")
                        .param("jobType", "No Type")
                        .param("jobDueDate", "")
                        .param("jobStartDate", "")
                        .param("search", "false")
                        .param("fromJob", "true")
                        .principal(mockPrincipal))
                .andExpect(view().name("jobFormTemplate"))
                .andExpect(model().attribute("fromJob", true))
                .andExpect(model().attribute("fromCalendar", false));
    }

    @Test
    public void testPostEditJob_AccessedFromCalendarAndInvalidInputs_FromCalendarTrue() throws Exception {
        mockMvc.perform(post("/my-renovations/edit-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("jobId", Long.toString(job.getId()))
                        .param("newJobName", "")
                        .param("jobDescription", "")
                        .param("jobType", "No Type")
                        .param("jobDueDate", "")
                        .param("jobStartDate", "")
                        .param("selectedRooms", "")
                        .param("search", "false")
                        .param("fromCalendar", "true")
                        .principal(mockPrincipal))
                .andExpect(view().name("jobFormTemplate"))
                .andExpect(model().attribute("fromCalendar", true))
                .andExpect(model().attribute("fromJob", false))
                .andExpect(model().attribute("fromWidget", false));
    }

    @Test
    public void testPostEditJob_AccessedFromJobDetailsPageAndInvalidInputs_FromJobTrue() throws Exception {
        mockMvc.perform(post("/my-renovations/edit-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("jobId", Long.toString(job.getId()))
                        .param("newJobName", "")
                        .param("jobDescription", "")
                        .param("jobType", "No Type")
                        .param("jobDueDate", "")
                        .param("jobStartDate", "")
                        .param("selectedRooms", "")
                        .param("search", "false")
                        .param("fromJob", "true")
                        .principal(mockPrincipal))
                .andExpect(view().name("jobFormTemplate"))
                .andExpect(model().attribute("fromJob", true))
                .andExpect(model().attribute("fromCalendar", false))
                .andExpect(model().attribute("fromWidget", false));
    }

    @Test
    public void testPostEditJob_AccessedFromHomePageWidgetAndInvalidInputs_FromWidgetTrue() throws Exception {
        mockMvc.perform(post("/my-renovations/edit-job")
                        .param("recordId", Long.toString(record.getId()))
                        .param("jobId", Long.toString(job.getId()))
                        .param("newJobName", "")
                        .param("jobDescription", "")
                        .param("jobType", "No Type")
                        .param("jobDueDate", "")
                        .param("jobStartDate", "")
                        .param("selectedRooms", "")
                        .param("search", "false")
                        .param("fromWidget", "true")
                        .principal(mockPrincipal))
                .andExpect(view().name("jobFormTemplate"))
                .andExpect(model().attribute("fromWidget", true))
                .andExpect(model().attribute("fromJob", false))
                .andExpect(model().attribute("fromCalendar", false));
    }
}