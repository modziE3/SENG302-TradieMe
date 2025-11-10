package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.*;
import nz.ac.canterbury.seng302.homehelper.entity.dto.JobFilter;
import nz.ac.canterbury.seng302.homehelper.repository.RoomRepository;
import nz.ac.canterbury.seng302.homehelper.repository.JobRepository;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.service.ValidationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobServiceTest {
    @Mock
    public JobRepository jobRepository;
    @Mock
    public RoomRepository roomRepository;
    @Mock
    public ValidationService validationService;
    @InjectMocks
    public JobService jobService;

    @Test
    public void addJobTest_WithDueDate() {
        Mockito.doAnswer(invocation -> invocation.getArgument(0))
                .when(jobRepository)
                .save(Mockito.any(Job.class));

        jobService.addJob(new Job("Job 1", "First job", "01/01/3025", "01/01/3024"));

        ArgumentCaptor<Job> formResultCaptor = ArgumentCaptor.forClass(Job.class);
        Mockito.verify(jobRepository).save(formResultCaptor.capture());

        Job capturedJob = formResultCaptor.getValue();
        Assertions.assertNotNull(capturedJob);
        assertEquals("Job 1", capturedJob.getName());
        assertEquals("First job", capturedJob.getDescription());
        assertEquals("01/01/3025", capturedJob.getDueDate());
    }

    @Test
    public void addJobTest_WithoutDueDate() {
        Mockito.doAnswer(invocation -> invocation.getArgument(0))
                .when(jobRepository)
                .save(Mockito.any(Job.class));

        jobService.addJob(new Job("Job 2", "Second job", null, null));

        ArgumentCaptor<Job> formResultCaptor = ArgumentCaptor.forClass(Job.class);
        Mockito.verify(jobRepository).save(formResultCaptor.capture());

        Job capturedJob = formResultCaptor.getValue();
        Assertions.assertNotNull(capturedJob);
        assertEquals("Job 2", capturedJob.getName());
        assertEquals("Second job", capturedJob.getDescription());
        assertNull(capturedJob.getDueDate());
    }

    static Stream<Arguments> jobNameErrorMessages() {
        return Stream.of(
                Arguments.of(JobService.JOB_NAME_EMPTY_OR_INVALID,
                        JobService.JOB_NAME_EMPTY_OR_INVALID),
                Arguments.of(JobService.JOB_NAME_EMPTY_OR_INVALID + JobService.JOB_NAME_OVER_255_CHARS,
                        JobService.JOB_NAME_OVER_255_CHARS)
        );
    }
    @ParameterizedTest
    @MethodSource("jobNameErrorMessages")
    public void getErrorMessages_jobNameErrorMessageStringProvided_CorrectSeparateErrorMessageReturned
            (String allErrorMessages, String jobNameErrorMessage) {

        List<String> errorMessages = jobService.getErrorMessages(allErrorMessages);

        assertEquals(jobNameErrorMessage, errorMessages.get(0));
    }

    static Stream<Arguments> jobDescriptionErrorMessages() {
        return Stream.of(
                Arguments.of(JobService.JOB_DESCRIPTION_EMPTY,
                        JobService.JOB_DESCRIPTION_EMPTY),
                Arguments.of(JobService.JOB_DESCRIPTION_EMPTY + JobService.JOB_DESCRIPTION_OVER_512_CHARS,
                        JobService.JOB_DESCRIPTION_OVER_512_CHARS)
        );
    }
    @ParameterizedTest
    @MethodSource("jobDescriptionErrorMessages")
    public void getErrorMessages_jobDescriptionErrorMessageStringProvided_CorrectSeparateErrorMessageReturned
            (String allErrorMessages, String jobDescriptionErrorMessage) {

        List<String> errorMessages = jobService.getErrorMessages(allErrorMessages);

        assertEquals(jobDescriptionErrorMessage, errorMessages.get(1));
    }

    static Stream<Arguments> jobTypeErrorMessages() {
        return Stream.of(
                Arguments.of(JobService.JOB_TYPE_INVALID,
                        JobService.JOB_TYPE_INVALID)
        );
    }
    @ParameterizedTest
    @MethodSource("jobTypeErrorMessages")
    public void getErrorMessages_jobTypeErrorMessageStringProvided_CorrectSeparateErrorMessageReturned
            (String allErrorMessages, String jobTypeErrorMessage) {

        List<String> errorMessages = jobService.getErrorMessages(allErrorMessages);

        assertEquals(jobTypeErrorMessage, errorMessages.get(2));
    }

    static Stream<Arguments> jobDueDateErrorMessages() {
        return Stream.of(
                Arguments.of(JobService.JOB_DUE_DATE_INVALID_FORMAT,
                        JobService.JOB_DUE_DATE_INVALID_FORMAT),
                Arguments.of(JobService.JOB_DUE_DATE_INVALID_FORMAT + JobService.JOB_DUE_DATE_NOT_IN_FUTURE,
                        JobService.JOB_DUE_DATE_NOT_IN_FUTURE),
                Arguments.of(JobService.JOB_DUE_DATE_INVALID_FORMAT + JobService.JOB_DUE_DATE_NOT_IN_FUTURE
                        + JobService.JOB_DUE_DATE_INVALID_DATE,
                        JobService.JOB_DUE_DATE_INVALID_DATE),
                Arguments.of(JobService.JOB_DUE_DATE_INVALID_FORMAT + JobService.JOB_DUE_DATE_NOT_IN_FUTURE
                                + JobService.JOB_DUE_DATE_INVALID_DATE + JobService.JOB_DUE_DATE_BEFORE_START,
                        JobService.JOB_DUE_DATE_BEFORE_START),
                Arguments.of(JobService.JOB_DUE_DATE_INVALID_FORMAT + JobService.JOB_DUE_DATE_NOT_IN_FUTURE
                                + JobService.JOB_DUE_DATE_INVALID_DATE + JobService.JOB_DUE_DATE_BEFORE_START + JobService.JOB_DUE_DATE_EMPTY,
                        JobService.JOB_DUE_DATE_EMPTY)
                );
    }
    @ParameterizedTest
    @MethodSource("jobDueDateErrorMessages")
    public void getErrorMessages_jobDueDateErrorMessageStringProvided_CorrectSeparateErrorMessageReturned
            (String allErrorMessages, String jobDueDateErrorMessage) {

        List<String> errorMessages = jobService.getErrorMessages(allErrorMessages);

        assertEquals(jobDueDateErrorMessage, errorMessages.get(3));
    }

    static Stream<Arguments> jobStartDateErrorMessages() {
        return Stream.of(
                Arguments.of(JobService.JOB_START_DATE_INVALID_FORMAT,
                        JobService.JOB_START_DATE_INVALID_FORMAT),
                Arguments.of(JobService.JOB_START_DATE_INVALID_FORMAT + JobService.JOB_START_DATE_NOT_IN_FUTURE,
                        JobService.JOB_START_DATE_NOT_IN_FUTURE),
                Arguments.of(JobService.JOB_START_DATE_INVALID_FORMAT + JobService.JOB_START_DATE_NOT_IN_FUTURE
                                + JobService.JOB_START_DATE_INVALID_DATE,
                        JobService.JOB_START_DATE_INVALID_DATE),
                Arguments.of(JobService.JOB_START_DATE_INVALID_FORMAT + JobService.JOB_START_DATE_NOT_IN_FUTURE
                                + JobService.JOB_START_DATE_INVALID_DATE + JobService.JOB_START_DATE_AFTER_DUE,
                        JobService.JOB_START_DATE_AFTER_DUE),
                Arguments.of(JobService.JOB_START_DATE_INVALID_FORMAT + JobService.JOB_START_DATE_NOT_IN_FUTURE
                                + JobService.JOB_START_DATE_INVALID_DATE + JobService.JOB_START_DATE_AFTER_DUE + JobService.JOB_START_DATE_EMPTY,
                        JobService.JOB_START_DATE_EMPTY)
        );
    }
    @ParameterizedTest
    @MethodSource("jobStartDateErrorMessages")
    public void getErrorMessages_jobStartDateErrorMessageStringProvided_CorrectSeparateErrorMessageReturned
            (String allErrorMessages, String jobStartDateErrorMessage) {

        List<String> errorMessages = jobService.getErrorMessages(allErrorMessages);

        assertEquals(jobStartDateErrorMessage, errorMessages.get(4));
    }

    private RenovationRecord establishRenovationRecord() {
        RenovationRecord renovationRecord = new RenovationRecord("Test", "Test", Arrays.asList(new Room("room"), new Room("secondRoom")), "test@email.com");
        Job job = new Job("Test", "Test", "05/10/2049", "01/01/2048");
        Job job2 = new Job("secondTest", "Test", "05/10/2049", "01/01/2048");
        Job job3 = new Job("thirdTest", "Test", "05/10/2049", "01/01/2048");
        Job job4 = new Job("fourthTest", "Test", "05/10/2049", "01/01/2048");
        renovationRecord.setJobs(Arrays.asList(job, job2, job3, job4));

        return renovationRecord;
    }
    @Test
    public void getNumberOfJobPages_DefaultJobAmount_LessJobsThanFullPage() {
        RenovationRecord renovationRecord = establishRenovationRecord();

        assertEquals(1, jobService.getNumPages(renovationRecord, "false"));
    }

    @Test
    public void getNumberOfJobPages_OneJobPerPage_MoreJobsThanFullPage() {
        jobService.setNumberOfJobs(1);
        RenovationRecord renovationRecord = establishRenovationRecord();

        assertEquals(4, jobService.getNumPages(renovationRecord, "false"));
    }

    @Test
    public void getNumberOfJobPages_NoJobs() {
        RenovationRecord renovationRecord = new RenovationRecord("Test", "Test", Arrays.asList(new Room("room"), new Room("secondRoom")), "test@email.com");

        assertEquals(0, jobService.getNumPages(renovationRecord, "false"));
    }

    private RenovationRecord establishRenovationRecordMoreThanTenJobs() {
        RenovationRecord renovationRecord = new RenovationRecord("Test", "Test", Arrays.asList(new Room("room"), new Room("secondRoom")), "test@email.com");
        Job job = new Job("Test", "Test", "05/10/2049", "01/01/2048");
        Job job2 = new Job("secondTest", "Test", "05/10/2049", "01/01/2048");
        Job job3 = new Job("thirdTest", "Test", "05/10/2049", "01/01/2048");
        Job job4 = new Job("fourthTest", "Test", "05/10/2049", "01/01/2048");
        Job job5 = new Job("fifthTest", "Test", "05/10/2049", "01/01/2048");
        Job job6 = new Job("sixthTest", "Test", "05/10/2049", "01/01/2048");
        Job job7 = new Job("seventhTest", "Test", "05/10/2049", "01/01/2048");
        Job job8 = new Job("eighthTest", "Test", "05/10/2049", "01/01/2048");
        Job job9 = new Job("ninthTest", "Test", "05/10/2049", "01/01/2048");
        Job job10 = new Job("tenthTest", "Test", "05/10/2049", "01/01/2048");
        Job job11 = new Job("eleventhTest", "Test", "05/10/2049", "01/01/2048");
        job.setStatus("Not Started");
        job2.setStatus("Not Started");
        job3.setStatus("In Progress");
        job4.setStatus("In Progress");
        job5.setStatus("In Progress");
        job6.setStatus("In Progress");
        job7.setStatus("In Progress");
        job8.setStatus("In Progress");
        job9.setStatus("In Progress");
        job10.setStatus("In Progress");
        job11.setStatus("In Progress");

        renovationRecord.setJobs(Arrays.asList(job, job2, job3, job4, job5, job6, job7, job8, job9, job10, job11));

        return renovationRecord;
    }

    private RenovationRecord establishRenovationRecordTenJobs() {
        RenovationRecord renovationRecord = new RenovationRecord("Test", "Test", Arrays.asList(new Room("room"), new Room("secondRoom")), "test@email.com");
        Job job = new Job("Test", "Test", "05/10/2049", "01/01/2048");
        Job job2 = new Job("secondTest", "Test", "05/10/2049", "01/01/2048");
        Job job3 = new Job("thirdTest", "Test", "05/10/2049", "01/01/2048");
        Job job4 = new Job("fourthTest", "Test", "05/10/2049", "01/01/2048");
        Job job5 = new Job("fifthTest", "Test", "05/10/2049", "01/01/2048");
        Job job6 = new Job("sixthTest", "Test", "05/10/2049", "01/01/2048");
        Job job7 = new Job("seventhTest", "Test", "05/10/2049", "01/01/2048");
        Job job8 = new Job("eighthTest", "Test", "05/10/2049", "01/01/2048");
        Job job9 = new Job("ninthTest", "Test", "05/10/2049", "01/01/2048");
        Job job10 = new Job("tenthTest", "Test", "05/10/2049", "01/01/2048");
        renovationRecord.setJobs(Arrays.asList(job, job2, job3, job4, job5, job6, job7, job8, job9, job10));

        return renovationRecord;
    }

    @Test
    public void getVisiblePaginationPages_GreaterThan10_FinalPage() {
        RenovationRecord renovationRecord = establishRenovationRecordMoreThanTenJobs();
        jobService.setNumberOfJobs(1);
        int currentPage = 11;
        List<Integer> expectedPages = Arrays.asList(1, 9, 10, 11);

        assertEquals(expectedPages, jobService.getPageList(currentPage, renovationRecord, "false"));
    }

    @Test
    public void getVisiblePaginationPages_GreaterThan10_FirstPage() {
        RenovationRecord renovationRecord = establishRenovationRecordMoreThanTenJobs();
        jobService.setNumberOfJobs(1);
        int currentPage = 1;
        List<Integer> expectedPages = Arrays.asList(1, 2, 3, 11);

        assertEquals(expectedPages, jobService.getPageList(currentPage, renovationRecord, "false"));
    }

    @Test
    public void getVisiblePaginationPages_GreaterThan10_TwoFromFirstPage() {
        RenovationRecord renovationRecord = establishRenovationRecordMoreThanTenJobs();
        jobService.setNumberOfJobs(1);
        int currentPage = 3;
        List<Integer> expectedPages = Arrays.asList(1, 2, 3, 4, 5, 11);

        assertEquals(expectedPages, jobService.getPageList(currentPage, renovationRecord, "false"));
    }

    @Test
    public void getVisiblePaginationPages_GreaterThan10_TwoFromFinalPage() {
        RenovationRecord renovationRecord = establishRenovationRecordMoreThanTenJobs();
        jobService.setNumberOfJobs(1);
        int currentPage = 9;
        List<Integer> expectedPages = Arrays.asList(1, 7, 8, 9, 10, 11);

        assertEquals(expectedPages, jobService.getPageList(currentPage, renovationRecord, "false"));
    }

    @Test
    public void getVisiblePaginationPages_GreaterThan10_MiddlePage() {
        RenovationRecord renovationRecord = establishRenovationRecordMoreThanTenJobs();
        jobService.setNumberOfJobs(1);
        int currentPage = 6;
        List<Integer> expectedPages = Arrays.asList(1, 4, 5, 6, 7, 8, 11);

        assertEquals(expectedPages, jobService.getPageList(currentPage, renovationRecord, "false"));
    }

    @Test
    public void getVisiblePaginationPages_10Pages() {
        RenovationRecord renovationRecord = establishRenovationRecordTenJobs();
        jobService.setNumberOfJobs(1);
        int currentPage = 9;
        List<Integer> expectedPages = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        assertEquals(expectedPages, jobService.getPageList(currentPage, renovationRecord, "false"));
    }

    @Test
    public void getVisiblePaginationPages_LessThan10Pages() {
        RenovationRecord renovationRecord = establishRenovationRecord();
        jobService.setNumberOfJobs(1);
        int currentPage = 2;
        List<Integer> expectedPages = Arrays.asList(1, 2, 3, 4);

        assertEquals(expectedPages, jobService.getPageList(currentPage, renovationRecord, "false"));
    }

    @Test
    public void editIcon_null_IsAddedToRepository() {
        Job job = Mockito.mock(Job.class);
        when(jobRepository.findById(any())).thenReturn(Optional.of(job));

        jobService.addJob(job);

        verify(jobRepository, times(1)).save(job); // job has added

        jobService.editIcon(job.getId(), null);

        verify(jobRepository, times(2)).save(job); // job has been updated
    }

    @Test
    public void editIcon_ValidPaintRoller_IsAddedToRepository() {
        Job job = Mockito.mock(Job.class);
        when(jobRepository.findById(any())).thenReturn(Optional.of(job));

        jobService.addJob(job);

        verify(jobRepository, times(1)).save(job); // job has added

        jobService.editIcon(job.getId(), "paint-roller-solid");

        verify(jobRepository, times(2)).save(job); // job has been updated
    }

    @Test
    public void editIcon_ValidHammer_IsAddedToRepository() {
        Job job = Mockito.mock(Job.class);
        when(jobRepository.findById(any())).thenReturn(Optional.of(job));

        jobService.addJob(job);

        verify(jobRepository, times(1)).save(job); // job has added

        jobService.editIcon(job.getId(), "hammer-solid");

        verify(jobRepository, times(2)).save(job); // job has been updated
    }

    @Test
    public void editIcon_ValidFaucet_IsAddedToRepository() {
        Job job = Mockito.mock(Job.class);
        when(jobRepository.findById(any())).thenReturn(Optional.of(job));

        jobService.addJob(job);

        verify(jobRepository, times(1)).save(job); // job has added

        jobService.editIcon(job.getId(), "faucet-solid");

        verify(jobRepository, times(2)).save(job); // job has been updated
    }

    @Test
    public void editIcon_ValidBroom_IsAddedToRepository() {
        Job job = Mockito.mock(Job.class);
        when(jobRepository.findById(any())).thenReturn(Optional.of(job));

        jobService.addJob(job);

        verify(jobRepository, times(1)).save(job); // job has added

        jobService.editIcon(job.getId(), "broom-solid");

        verify(jobRepository, times(2)).save(job); // job has been updated
    }

    @Test
    public void editIcon_ValidBolt_IsAddedToRepository() {
        Job job = Mockito.mock(Job.class);
        when(jobRepository.findById(any())).thenReturn(Optional.of(job));

        jobService.addJob(job);

        verify(jobRepository, times(1)).save(job); // job has added

        jobService.editIcon(job.getId(), "bolt-solid");

        verify(jobRepository, times(2)).save(job); // job has been updated
    }

    @Test
    public void editIcon_ValidHouseDamage_IsAddedToRepository() {
        Job job = Mockito.mock(Job.class);
        when(jobRepository.findById(any())).thenReturn(Optional.of(job));

        jobService.addJob(job);

        verify(jobRepository, times(1)).save(job); // job has added

        jobService.editIcon(job.getId(), "house-chimney-crack-solid");

        verify(jobRepository, times(2)).save(job); // job has been updated
    }

    @Test
    public void editIcon_InvalidImage_NotAddedToRepository() {
        Job job = Mockito.mock(Job.class);

        jobService.addJob(job);

        verify(jobRepository, times(1)).save(job);

        jobService.editIcon(job.getId(), "images/DefaultProfileImage.png");

        verify(jobRepository, times(1)).save(job);
    }

    @Test
    public void editIcon_InvalidEmptyString_NotAddedToRepository() {
        Job job = Mockito.mock(Job.class);

        jobService.addJob(job);

        verify(jobRepository, times(1)).save(job);

        jobService.editIcon(job.getId(), "");

        verify(jobRepository, times(1)).save(job);
    }

    @Test
    public void editIcon_InvalidHammerAndExtra_NotAddedToRepository() {
        Job job = Mockito.mock(Job.class);

        jobService.addJob(job);

        verify(jobRepository, times(1)).save(job);

        jobService.editIcon(job.getId(), "fa-solid fa-hammerfa-solid fa-hammer");

        verify(jobRepository, times(1)).save(job);
    }

    @Test
    public void getVisiblePaginationPages_GreaterThan10_Filtered_OnePage() {
        RenovationRecord renovationRecord = establishRenovationRecordMoreThanTenJobs();
        jobService.setNumberOfJobs(5);
        int currentPage = 1;
        List<Integer> expectedPages = List.of(1);

        assertEquals(expectedPages, jobService.getPageList(currentPage, renovationRecord, "Not Started"));
    }

    @Test
    public void getVisiblePaginationPages_GreaterThan10_Filtered_MultiplePages() {
        RenovationRecord renovationRecord = establishRenovationRecordMoreThanTenJobs();
        jobService.setNumberOfJobs(5);
        int currentPage = 1;
        List<Integer> expectedPages = List.of(1, 2);

        assertEquals(expectedPages, jobService.getPageList(currentPage, renovationRecord, "In Progress"));
    }

    @Test
    public void getVisiblePaginationPages_GreaterThan10_Filtered_NoPages() {
        RenovationRecord renovationRecord = establishRenovationRecordMoreThanTenJobs();
        jobService.setNumberOfJobs(5);
        int currentPage = 1;
        List<Integer> expectedPages = List.of();

        assertEquals(expectedPages, jobService.getPageList(currentPage, renovationRecord, "Completed"));
    }

    @Test
    public void getJobCards_RepoHasPostedJobs_AllJobsAreMadeIntoCardsPosted() {
        UserService userService = Mockito.mock(UserService.class);
        User user = Mockito.mock(User.class);
        RenovationRecord renovationRecord = Mockito.mock(RenovationRecord.class);
        when(renovationRecord.getStreetAddress()).thenReturn("street address");
        when(renovationRecord.getCity()).thenReturn("city");
        when(renovationRecord.getSuburb()).thenReturn("suburb");
        when(renovationRecord.getUserEmail()).thenReturn("abc@abc.abc");
        when(user.getFirstName()).thenReturn("John");
        when(user.getLastName()).thenReturn("Doe");
        when(userService.getUser("abc@abc.abc")).thenReturn(user);

        Job job = Mockito.mock(Job.class);
        when(job.getName()).thenReturn("job");
        when(job.getRenovationRecord()).thenReturn(renovationRecord);
        when(job.getDueDate()).thenReturn("01/01/01");
        when(job.getStartDate()).thenReturn("01/01/01");
        when(job.getIsPosted()).thenReturn(true);

        when(jobRepository.findPostedJobs()).thenReturn(List.of(job));
        List<Job> jobSlice = jobRepository.findPostedJobs();

        Assertions.assertTrue(jobService.getJobCardsPosted(userService, jobSlice).size() == 1);
        Assertions.assertTrue(jobService.getJobCardsPosted(userService, jobSlice).get(0).getTitle().equals("job"));
    }

    @Test
    public void getLocationFilteredJobs_ReturnsCorrectJobs() {
        RenovationRecord ChristchurchIlam = new RenovationRecord();
        ChristchurchIlam.setCity("Christchurch");
        ChristchurchIlam.setSuburb("Ilam");

        RenovationRecord ChristchurchRiccarton = new RenovationRecord();
        ChristchurchRiccarton.setCity("Christchurch");
        ChristchurchRiccarton.setSuburb("Riccarton");

        RenovationRecord WellingtonTeAro = new RenovationRecord();
        WellingtonTeAro.setCity("Wellington");
        WellingtonTeAro.setSuburb("Te Aro");

        Job job1 = new Job("Job1", "Desc", "01/01/2027", "01/01/2026");
        job1.setRenovationRecord(ChristchurchIlam);

        Job job2 = new Job("Job2", "Desc", "01/01/2027", "01/01/2026");
        job2.setRenovationRecord(ChristchurchRiccarton);

        Job job3 = new Job("Job3", "Desc", "01/01/2027", "01/01/2026");
        job3.setRenovationRecord(WellingtonTeAro);

        Mockito.when(jobRepository.findPostedJobs()).thenReturn(List.of(job1, job2, job3));
        JobFilter filter = new JobFilter(null, null, "Christchurch", "Ilam", null, null);
        List<Job> filteredJobs = jobService.getFilteredJobs(filter);
        Assertions.assertEquals(1, filteredJobs.size());
        Assertions.assertEquals("Ilam", filteredJobs.get(0).getRenovationRecord().getSuburb());
        Assertions.assertEquals("Christchurch", filteredJobs.get(0).getRenovationRecord().getCity());
    }

    @Test
    public void convertJobStartDatesForCalendar() {
        Job job1 = new Job("job1", "job1", "28/03/2077", "28/02/2077");
        job1.setId(1L);
        Job job2 = new Job("job2", "job2", null, "28/02/2077");
        job2.setId(2L);
        Job job3 = new Job("job3", "job3", "28/03/2077", null);
        job3.setId(2L);
        Job job4 = new Job("job4", "job4", null, null);
        job4.setId(2L);

        List<String> convertedDates = jobService.convertJobStartDatesForCalendar(List.of(job1, job2, job3, job4));
        assertEquals("2077-02-28", convertedDates.get(0));
        assertEquals("2077-02-28", convertedDates.get(1));
        assertNull(convertedDates.get(2));
        assertNull(convertedDates.get(3));
    }

    @Test
    public void convertJobDueDatesForCalendar() {
        Job job1 = new Job("job1", "job1", "28/03/2077", "28/02/2077");
        Job job2 = new Job("job2", "job2", null, "28/02/2077");
        Job job3 = new Job("job3", "job3", "28/03/2077", null);
        Job job4 = new Job("job4", "job4", null, null);

        List<String> convertedDates = jobService.convertJobDueDatesForCalendar(List.of(job1, job2, job3, job4));
        assertEquals("2077-03-29", convertedDates.get(0));
        assertNull(convertedDates.get(1));
        assertEquals("2077-03-28", convertedDates.get(2));
        assertNull(convertedDates.get(3));
    }

    @Test
    public void jobsWereModified() {
        LocalDateTime tenSecondsAgo = LocalDateTime.now().minusSeconds(10);
        LocalDateTime now = LocalDateTime.now();

        Job job1 = new Job("job1", "job1", "28/03/2077", "28/02/2077");
        job1.setLastUpdated(tenSecondsAgo);
        Job job2 = new Job("job2", "job2", null, "28/02/2077");
        job2.setLastUpdated(tenSecondsAgo);
        Job job3 = new Job("job3", "job3", "28/03/2077", null);
        job3.setLastUpdated(tenSecondsAgo);
        Job job4 = new Job("job4", "job4", null, null);
        job4.setLastUpdated(now);

        List<Boolean> jobWasModified = jobService.jobsWereModified(List.of(job1, job2, job3, job4));
        assertFalse(jobWasModified.get(0));
        assertFalse(jobWasModified.get(1));
        assertFalse(jobWasModified.get(2));
        assertTrue(jobWasModified.get(3));
    }

    @Test
    public void getRecommendedPostedJobs_UserHasNoLocation_PostedJobsUserDoesNotOwnReturned() {
        User user = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        RenovationRecord userRecord = new RenovationRecord("Record", "Record", List.of(), "jane@doe.nz");

        RenovationRecord anotherUserRecord = new RenovationRecord("Record", "Record", List.of(), "john@doe.nz");
        Job job1 = new Job("job1", "job1", "28/03/2077", "28/02/2077");
        job1.setIsPosted(true);
        job1.setRenovationRecord(userRecord);
        Job job2 = new Job("job2", "job2", null, "28/02/2077");
        job2.setIsPosted(true);
        job2.setRenovationRecord(userRecord);
        Job job3 = new Job("job3", "job3", "28/03/2077", null);
        job3.setIsPosted(true);
        job3.setRenovationRecord(anotherUserRecord);
        Job job4 = new Job("job4", "job4", null, null);
        job4.setIsPosted(true);
        job4.setRenovationRecord(anotherUserRecord);
        List<Job> postedJobs = List.of(job1, job2, job3, job4);
        List<Job> expectedJobs = List.of(job3, job4);

        when(jobRepository.findPostedJobs()).thenReturn(postedJobs);
        List<Job> recommendedJobs = jobService.getRecommendedPostedJobs(user);
        assertTrue(recommendedJobs.containsAll(expectedJobs));
    }

    @Test
    public void getRecommendedPostedJobs_UserHasLocation_PostedJobsInSameLocationAppearFirst() {
        User user = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        user.setCity("Christchurch");

        RenovationRecord anotherUserRecord1 = new RenovationRecord("Record", "Record", List.of(), "john@doe.nz");
        anotherUserRecord1.setCity("Christchurch");
        RenovationRecord anotherUserRecord2 = new RenovationRecord("Record", "Record", List.of(), "john@doe.nz");
        anotherUserRecord2.setCity("Auckland");

        Job job1 = new Job("job1", "job1", "28/03/2077", "28/02/2077");
        job1.setIsPosted(true);
        job1.setRenovationRecord(anotherUserRecord1);
        Job job2 = new Job("job2", "job2", null, "28/02/2077");
        job2.setIsPosted(true);
        job2.setRenovationRecord(anotherUserRecord2);
        Job job3 = new Job("job3", "job3", "28/03/2077", null);
        job3.setIsPosted(true);
        job3.setRenovationRecord(anotherUserRecord1);
        Job job4 = new Job("job4", "job4", null, null);
        job4.setIsPosted(true);
        job4.setRenovationRecord(anotherUserRecord2);
        List<Job> postedJobs = List.of(job1, job2, job3, job4);
        List<Job> expectedJobs = List.of(job3, job1, job2, job4);

        when(jobRepository.findPostedJobs()).thenReturn(postedJobs);
        List<Job> recommendedJobs = jobService.getRecommendedPostedJobs(user);
        assertTrue(recommendedJobs.containsAll(expectedJobs));
        assertTrue(recommendedJobs.getFirst().equals(job3) || recommendedJobs.getFirst().equals(job1));
        assertTrue(recommendedJobs.get(1).equals(job1) || recommendedJobs.get(1).equals(job3));
    }
}
