package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.persistence.Tuple;
import nz.ac.canterbury.seng302.homehelper.controller.job.JobListingController;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.*;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class JobListingsControllerIntegrationTest {
    @Autowired
    private UserService userService;
    @Autowired
    private JobService jobService;
    @Autowired
    private RenovationRecordService renovationRecordService;
    @Autowired
    private JobListingController jobListingController;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private LocationService locationService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private RenovationRecordRepository renovationRecordRepository;
    @MockBean
    private RecentRenovationRepository recentRenovationRepository;
    @MockBean
    private JobRepository jobRepository;

    private MockMvc mockMvc;
    private Principal principal;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final User testUser = new User("John", "Doe", "john@test.com", passwordEncoder.encode("P4$$word"), null, null);
    private final RenovationRecord testRenovationRecord = new RenovationRecord("test record", "test desc", new ArrayList<>(), testUser.getEmail());
    private final Job testJob = new Job("Test Job", "Test desc", "01/01/01", "01/01/01");
    private List<Job> jobs;
    private Integer pageNumbers;

    @BeforeEach
    public void setup() {
        jobs = IntStream.range(0, 70)
                .mapToObj(i -> {
                    Job job = new Job("Test Job " + (i + 1), "Test desc", "01/01/01", "01/01/01");
                    job.setRenovationRecord(testRenovationRecord);
                    job.setIsPosted(true);
                    job.setRenovationRecord(testRenovationRecord);
                    return job;
                })
                .collect(Collectors.toList());

        testRenovationRecord.setJobs(jobs);
        pageNumbers = jobs.size()/9;

        mockMvc = MockMvcBuilders.standaloneSetup(jobListingController).setViewResolvers(new InternalResourceViewResolver()).build();
        testUser.grantAuthority("ROLE_USER");
        testUser.setId(9999L);
        principal = new UsernamePasswordAuthenticationToken(testUser.getEmail(), null, testUser.getAuthorities());

        when(renovationRecordRepository.findAll()).thenReturn(List.of(testRenovationRecord));
        when(jobRepository.findPostedJobs()).thenReturn(jobs);
        when(jobRepository.findById(testJob.getId())).thenReturn(Optional.of(testJob));
        when(userRepository.findByEmailContainingIgnoreCase(testUser.getEmail())).thenReturn(testUser);
    }

    @Test
    void getJobListing_RepoHasJobs_JobsAreDisplayed() throws Exception {
        mockMvc.perform(get("/job-listings").param("job-page", "1")
                        .principal(principal))
                .andExpect(model().attributeDoesNotExist("tagErrorMessage"))
                .andExpect(model().attributeExists("jobs"));
    }

    @Test
    void userInputtedPage_ValidInput() throws Exception {
        when(jobRepository.findPostedJobs()).thenReturn(jobs);
        mockMvc.perform(post("/job-listings").param("job-page", "1")
                        .param("pageNumber", "4")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("redirect:/job-listings?job-page=4")));

    }

    @Test
    void userInputtedPage_InputTooLarge() throws Exception {
        mockMvc.perform(post("/job-listings").param("job-page", "1")
                        .param("pageNumber", "1234512345123451234512345123451234512345123451234512" +
                                "12345123451234512345345123451234512345123451234512345123451234512345123451234512345")
        .principal(principal))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void userInputtedPage_NegativeInput() throws Exception {
        mockMvc.perform(post("/job-listings").param("job-page", "1")
                        .param("pageNumber", "-1")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/job-listings?job-page=1&keywords-filter=&jobStartDate=null&jobDueDate=null&type-filter=null&city-filter=null&suburb-filter=null"));
    }

    @Test
    void userInputtedPage_InvalidInput() throws Exception {
        mockMvc.perform(post("/job-listings")
                        .param("job-page", "1")
                        .param("pageNumber", "a")
                        .principal(principal))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void userInputtedPage_LastPage() throws Exception {
        when(jobRepository.findPostedJobs()).thenReturn(jobs);
        String last = String.valueOf(pageNumbers);
        mockMvc.perform(post("/job-listings").param("job-page", "1")
                        .param("pageNumber", last)
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("redirect:/job-listings?job-page=" + last)));
    }

    @Test
    void userInputtedPage_FirstPage() throws Exception {
        when(jobRepository.findPostedJobs()).thenReturn(jobs);
        mockMvc.perform(post("/job-listings").param("job-page", "5")
                        .param("pageNumber", "1")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(containsString("redirect:/job-listings?job-page=1")));
    }

    @Test
    void getJobListingPage_nullTypeGiven() throws Exception {
        when(jobRepository.findPostedJobs()).thenReturn(jobs);

        mockMvc.perform(get("/job-listings").param("job-page", "1")
                        .param("pageNumber", "1")
                        .param("type-filter", "null")
                        .principal(principal))
                .andExpect(status().isOk());
    }

    @Test
    void getJobListingPage_TypeGiven() throws Exception {
        when(jobRepository.findPostedJobs()).thenReturn(jobs);

        mockMvc.perform(get("/job-listings").param("job-page", "1")
                        .param("pageNumber", "1")
                        .param("type-filter", "Carpentry")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("typeFilter", "Carpentry"));
    }

    @Test
    public void testGetJobListing_CityAndSuburb() throws Exception {
        List<Tuple> mockTuples = new ArrayList<>();

        // Mocking individual Tuple objects
        Tuple tuple1 = mock(Tuple.class);
        when(tuple1.get(0)).thenReturn("Auckland");
        when(tuple1.get(1)).thenReturn("CBD");
        mockTuples.add(tuple1);

        Tuple tuple2 = mock(Tuple.class);
        when(tuple2.get(0)).thenReturn("Christchurch");
        when(tuple2.get(1)).thenReturn("Avonhead");
        mockTuples.add(tuple2);

        Tuple tuple3 = mock(Tuple.class);
        when(tuple3.get(0)).thenReturn("Christchurch");
        when(tuple3.get(1)).thenReturn("Sumner");
        mockTuples.add(tuple3);

        RenovationRecordRepository renovationRecordRepository = mock(RenovationRecordRepository.class);
        when(renovationRecordRepository.findCitySuburbs()).thenReturn(mockTuples);

        RenovationRecordService renovationRecordService = new RenovationRecordService(renovationRecordRepository, roomRepository, locationService, recentRenovationRepository);

        Map<String, List<String>> expectedMap = new HashMap<>();
        expectedMap.put("Christchurch", List.of("Avonhead", "Sumner"));
        expectedMap.put("Auckland", List.of("CBD"));

        Map<String, List<String>> result = renovationRecordService.getCityAndSuburb();

        assertEquals(expectedMap.keySet(), result.keySet());
        for (String city : expectedMap.keySet()) {
            assertTrue(result.containsKey(city));
            assertIterableEquals(expectedMap.get(city), result.get(city));
        }
    }

    @Test
    public void getJobListingPage_ValidCityGiven() throws Exception {
        mockMvc.perform(get("/job-listings")
                        .param("job-page", "1")
                        .param("city-filter", "Christchurch")
                        .principal(principal))
                .andExpect(view().name("jobListingTemplate"))
                .andExpect(model().attribute("cityFilter", "Christchurch"));
    }

    static Stream<Arguments> invalidCities() {
        return Stream.of(
                Arguments.of("city#"),
                Arguments.of("city."),
                Arguments.of("city123"),
                Arguments.of("❤️")
        );
    }
    @ParameterizedTest
    @MethodSource("invalidCities")
    public void getJobListingPage_invalidCityGiven_CorrectErrorMessageShown(String invalidCity) throws Exception {
        mockMvc.perform(get("/job-listings")
                        .param("job-page", "1")
                        .param("city-filter", invalidCity)
                        .principal(principal))
                .andExpect(view().name("jobListingTemplate"))
                .andExpect(model().attribute("cityFilter", invalidCity))
                .andExpect(model().attribute("cityErrorMessage", LocationService.CITY_INVALID));
    }

    static Stream<Arguments> invalidSuburbs() {
        return Stream.of(
                Arguments.of("suburb#"),
                Arguments.of("suburb."),
                Arguments.of("❤️")
        );
    }
    @ParameterizedTest
    @MethodSource("invalidSuburbs")
    public void getJobListingPage_invalidSuburbGiven_CorrectErrorMessageShown(String invalidSuburb) throws Exception {
        mockMvc.perform(get("/job-listings")
                        .param("job-page", "1")
                        .param("city-filter", "Christchurch")
                        .param("suburb-filter", invalidSuburb)
                        .principal(principal))
                .andExpect(view().name("jobListingTemplate"))
                .andExpect(model().attribute("suburbFilter", invalidSuburb))
                .andExpect(model().attribute("suburbErrorMessage", LocationService.SUBURB_INVALID));
    }

    static Stream<Arguments> invalidStartDates() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);

        return Stream.of(
                Arguments.of("3025/01/01", JobService.JOB_START_DATE_INVALID_FORMAT),
                Arguments.of("01-01-3025", JobService.JOB_START_DATE_INVALID_FORMAT),
                Arguments.of(sdf.format(calendar.getTime()), JobService.JOB_START_DATE_NOT_IN_FUTURE),
                Arguments.of("30/02/3025", JobService.JOB_START_DATE_INVALID_DATE)
        );
    }
    @ParameterizedTest
    @MethodSource("invalidStartDates")
    public void getJobListingPage_invalidStartDateGiven_CorrectErrorMessageShown(String invalidStartDate, String errorMessage) throws Exception {
        mockMvc.perform(get("/job-listings")
                        .param("job-page", "1")
                        .param("jobStartDate", invalidStartDate)
                        .principal(principal))
                .andExpect(view().name("jobListingTemplate"))
                .andExpect(model().attribute("jobStartDate", invalidStartDate))
                .andExpect(model().attribute("jobStartDateErrorMessage", errorMessage));
    }

    static Stream<Arguments> invalidDueDates() {
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
    public void getJobListingPage_invalidDueDateGiven_CorrectErrorMessageShown(String invalidDueDate, String errorMessage) throws Exception {
        mockMvc.perform(get("/job-listings")
                        .param("job-page", "1")
                        .param("jobDueDate", invalidDueDate)
                        .principal(principal))
                .andExpect(view().name("jobListingTemplate"))
                .andExpect(model().attribute("jobDueDate", invalidDueDate))
                .andExpect(model().attribute("jobDueDateErrorMessage", errorMessage));
    }

    @Test
    public void getJobListingPage_startDateAfterDueDate_CorrectErrorMessageShown() throws Exception {
        mockMvc.perform(get("/job-listings")
                        .param("job-page", "1")
                        .param("jobStartDate", "01/01/2026")
                        .param("jobDueDate", "31/12/2025")
                        .principal(principal))
                .andExpect(view().name("jobListingTemplate"))
                .andExpect(model().attribute("jobStartDate", "01/01/2026"))
                .andExpect(model().attribute("jobDueDate", "31/12/2025"))
                .andExpect(model().attribute("jobStartDateErrorMessage", JobService.JOB_START_DATE_AFTER_DUE));
    }
}
