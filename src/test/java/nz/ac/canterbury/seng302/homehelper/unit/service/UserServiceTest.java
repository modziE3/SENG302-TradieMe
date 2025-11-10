package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.*;
import nz.ac.canterbury.seng302.homehelper.repository.QuoteRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RecentRenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.service.ValidationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.TaskScheduler;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    public UserRepository userRepository;
    @Mock
    public RenovationRecordRepository renovationRecordRepository;
    @Mock
    public RecentRenovationRepository recentRenovationRepository;
    @Mock
    public QuoteRepository quoteRepository;
    @Mock
    public ValidationService validationService;
    @Mock
    public RenovationRecordService renovationRecordService;
    @Mock
    public TaskScheduler taskScheduler;
    @InjectMocks
    public UserService userService;

    public User testUser;
    public User testUser2;

    @Spy
    @InjectMocks
    private UserService spyUserService;

    @BeforeEach
    public void setup() {
        spyUserService = spy(userService);
        testUser = new User("John", "Doe", "john@example.com", "P4$$word", null, null);
        testUser.setId(1L);
        testUser2 = new User("Jane", "Doe", "jane@example.com", "P4$$word", null, null);
        testUser2.setId(2L);
    }

    @Test
    public void addUser() {
        userService.addUser(testUser);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, times(1)).save(userCaptor.capture());

        User user = userCaptor.getValue();
        Assertions.assertNotNull(user);
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("P4$$word", user.getPassword());
    }

    static Stream<Arguments> expectedErrorMessages() {
        return Stream.of(
                Arguments.of(UserService.FIRST_NAME_EMPTY,
                        UserService.FIRST_NAME_EMPTY, "", "", "", ""),
                Arguments.of(UserService.FIRST_NAME_EMPTY +
                                UserService.FIRST_NAME_INVALID_CHARS +
                                UserService.FIRST_NAME_OVER_64_CHARS,
                        UserService.FIRST_NAME_OVER_64_CHARS, "", "", "", ""),
                Arguments.of(UserService.FIRST_NAME_INVALID_CHARS +
                                UserService.LAST_NAME_INVALID_CHARS +
                                UserService.LAST_NAME_OVER_64_CHARS,
                        UserService.FIRST_NAME_INVALID_CHARS, UserService.LAST_NAME_OVER_64_CHARS, "", "", ""),
                Arguments.of(UserService.FIRST_NAME_INVALID_CHARS +
                                UserService.LAST_NAME_INVALID_CHARS +
                                UserService.EMAIL_INVALID,
                        UserService.FIRST_NAME_INVALID_CHARS, UserService.LAST_NAME_INVALID_CHARS, UserService.EMAIL_INVALID, "", ""),
                Arguments.of(UserService.FIRST_NAME_INVALID_CHARS +
                                UserService.LAST_NAME_INVALID_CHARS +
                                UserService.EMAIL_EXISTS,
                        UserService.FIRST_NAME_INVALID_CHARS, UserService.LAST_NAME_INVALID_CHARS, UserService.EMAIL_EXISTS, "", ""),
                Arguments.of(UserService.FIRST_NAME_INVALID_CHARS +
                                UserService.LAST_NAME_INVALID_CHARS +
                                UserService.EMAIL_EXISTS +
                                UserService.PASSWORD_INVALID +
                                UserService.PASSWORDS_DONT_MATCH,
                        UserService.FIRST_NAME_INVALID_CHARS, UserService.LAST_NAME_INVALID_CHARS, UserService.EMAIL_EXISTS,
                        UserService.PASSWORD_INVALID, UserService.PASSWORDS_DONT_MATCH)
        );
    }
    @ParameterizedTest
    @MethodSource("expectedErrorMessages")
    public void getErrorMessages_AllErrorMessagesStringProvided_CorrectSeparateErrorMessagesReturned
            (String allErrorMessages, String firstNameErrorMessage, String lastNameErrorMessage,
             String emailErrorMessage, String passwordErrorMessage, String secondPasswordErrorMessage) {

        List<String> errorMessages = userService.getErrorMessages(allErrorMessages);

        assertEquals(firstNameErrorMessage, errorMessages.get(0));
        assertEquals(lastNameErrorMessage, errorMessages.get(1));
        assertEquals(emailErrorMessage, errorMessages.get(2));
        assertEquals(passwordErrorMessage, errorMessages.get(3));
        assertEquals(secondPasswordErrorMessage, errorMessages.get(4));
    }

    @Test
    public void generateValidationCode_GenerateTwoCodes_HaveSameLength() {
        when(userRepository.findVerificationCodeByVerificationCode(Mockito.any())).thenReturn(null);
        String code1 = userService.generateValidationCode();
        String code2 = userService.generateValidationCode();
        assertEquals(code1.length(), code2.length());
    }

    @Test
    public void generateValidationCode_GenerateTwoCodes_AreUnique() {
        when(userRepository.findVerificationCodeByVerificationCode(Mockito.any())).thenReturn(null);
        String code1 = userService.generateValidationCode();
        String code2 = userService.generateValidationCode();
        Assertions.assertNotEquals(code1, code2);
    }

    @Test
    public void setUserLocation_NonEmptyLocationInfo_LocationInformationSaved() {
        List<String> locationInfo = List.of("Street Address", "Suburb", "City", "Postcode", "Country");
        userService.setUserLocation(testUser, locationInfo);
        assertEquals("Street Address", testUser.getStreetAddress());
        assertEquals("Suburb", testUser.getSuburb());
        assertEquals("City", testUser.getCity());
        assertEquals("Postcode", testUser.getPostcode());
        assertEquals("Country", testUser.getCountry());
    }

    @Test
    public void addJobToUsersMostRecent_NewJobToEmpty_JobIsOnlyRecent() {
        testUser.setId(1L);
        Job job = Mockito.mock(Job.class);
        userService.addJobToUsersMostRecent(testUser, job);
        assertEquals(1, testUser.getRecentJobs().size());
        assertEquals(job.getId(), testUser.getRecentJobs().getFirst());
    }

    @Test
    public void addJobToUsersMostRecent_NewJobToFull_JobIsAtFrontAndFull() {
        testUser.setId(1L);
        Job newJob = Mockito.mock(Job.class);
        Job job1 = Mockito.mock(Job.class);
        Job job2 = Mockito.mock(Job.class);
        Job job3 = Mockito.mock(Job.class);
        Job job4 = Mockito.mock(Job.class);
        when(newJob.getId()).thenReturn(5L);
        when(job1.getId()).thenReturn(1L);
        when(job2.getId()).thenReturn(2L);
        when(job3.getId()).thenReturn(3L);
        when(job4.getId()).thenReturn(4L);
        testUser.setRecentJobs(new ArrayList<>(java.util.List.of(job1.getId(), job2.getId(), job3.getId(), job4.getId())));
        userService.addJobToUsersMostRecent(testUser, newJob);
        assertEquals(4, testUser.getRecentJobs().size());
        assertEquals(newJob.getId(), testUser.getRecentJobs().getFirst());
        assertEquals(job1.getId(), testUser.getRecentJobs().get(1));
        assertEquals(job2.getId(), testUser.getRecentJobs().get(2));
        assertEquals(job3.getId(), testUser.getRecentJobs().get(3));
        Assertions.assertFalse(testUser.getRecentJobs().contains(job4.getId()));
    }

    @Test
    public void updateUserRecentRenovations_AddToEmptyList_ElementsInOrderNewestFirst() {
        RenovationRecord testRenovationRecord1 = new RenovationRecord();
        testRenovationRecord1.setId(1L);
        RenovationRecord testRenovationRecord2 = new RenovationRecord();
        testRenovationRecord2.setId(2L);
        RenovationRecord testRenovationRecord3 = new RenovationRecord();
        testRenovationRecord3.setId(3L);
        RenovationRecord testRenovationRecord4 = new RenovationRecord();
        testRenovationRecord4.setId(4L);
        RenovationRecord testRenovationRecord5 = new RenovationRecord();
        testRenovationRecord5.setId(5L);
        when(renovationRecordService.getRecordById(1L)).thenReturn(testRenovationRecord1);
        when(renovationRecordService.getRecordById(2L)).thenReturn(testRenovationRecord2);
        when(renovationRecordService.getRecordById(3L)).thenReturn(testRenovationRecord3);
        when(renovationRecordService.getRecordById(4L)).thenReturn(testRenovationRecord4);
        when(renovationRecordService.getRecordById(5L)).thenReturn(testRenovationRecord5);

        userService.updateRecentRenovations(testUser, testRenovationRecord1);
        userService.updateRecentRenovations(testUser, testRenovationRecord2);
        userService.updateRecentRenovations(testUser, testRenovationRecord3);
        userService.updateRecentRenovations(testUser, testRenovationRecord4);
        userService.updateRecentRenovations(testUser, testRenovationRecord5);

        List<RenovationRecord> actual = userService.getRecentRenovations(testUser);
        assertTrue(actual.containsAll(Arrays.asList(
                testRenovationRecord1,
                testRenovationRecord2,
                testRenovationRecord3,
                testRenovationRecord4,
                testRenovationRecord5
        )));
        assertEquals(5, actual.size());
    }

    @Test
    public void updateUserRecentRenovations_AddRepeatElement_RepeatRemovedAndMadeNewest() {
        RenovationRecord testRenovationRecord1 = new RenovationRecord();
        testRenovationRecord1.setId(1L);
        RenovationRecord testRenovationRecord2 = new RenovationRecord();
        testRenovationRecord2.setId(2L);
        RenovationRecord testRenovationRecord3 = new RenovationRecord();
        testRenovationRecord3.setId(3L);
        RenovationRecord testRenovationRecord4 = new RenovationRecord();
        testRenovationRecord4.setId(4L);
        RenovationRecord testRenovationRecord5 = new RenovationRecord();
        testRenovationRecord5.setId(5L);
        when(renovationRecordService.getRecordById(1L)).thenReturn(testRenovationRecord1);
        when(renovationRecordService.getRecordById(2L)).thenReturn(testRenovationRecord2);
        when(renovationRecordService.getRecordById(3L)).thenReturn(testRenovationRecord3);
        when(renovationRecordService.getRecordById(4L)).thenReturn(testRenovationRecord4);
        when(renovationRecordService.getRecordById(5L)).thenReturn(testRenovationRecord5);

        userService.updateRecentRenovations(testUser, testRenovationRecord1);
        userService.updateRecentRenovations(testUser, testRenovationRecord2);
        userService.updateRecentRenovations(testUser, testRenovationRecord3);
        userService.updateRecentRenovations(testUser, testRenovationRecord4);
        userService.updateRecentRenovations(testUser, testRenovationRecord5);
        userService.updateRecentRenovations(testUser, testRenovationRecord3);

        List<RenovationRecord> actual = userService.getRecentRenovations(testUser);
        assertTrue(actual.containsAll(Arrays.asList(
                testRenovationRecord1,
                testRenovationRecord2,
                testRenovationRecord3,
                testRenovationRecord4,
                testRenovationRecord5
        )));
        assertEquals(5, actual.size());
    }

    @Test
    public void updateUserRecentRenovations_AddMoreThan10Elements_OldestDeleted() {
        RenovationRecord testRenovationRecord1 = new RenovationRecord();
        testRenovationRecord1.setId(1L);
        RenovationRecord testRenovationRecord2 = new RenovationRecord();
        testRenovationRecord2.setId(2L);
        RenovationRecord testRenovationRecord3 = new RenovationRecord();
        testRenovationRecord3.setId(3L);
        RenovationRecord testRenovationRecord4 = new RenovationRecord();
        testRenovationRecord4.setId(4L);
        RenovationRecord testRenovationRecord5 = new RenovationRecord();
        testRenovationRecord5.setId(5L);
        RenovationRecord testRenovationRecord6 = new RenovationRecord();
        testRenovationRecord6.setId(6L);
        RenovationRecord testRenovationRecord7 = new RenovationRecord();
        testRenovationRecord7.setId(7L);
        RenovationRecord testRenovationRecord8 = new RenovationRecord();
        testRenovationRecord8.setId(8L);
        RenovationRecord testRenovationRecord9 = new RenovationRecord();
        testRenovationRecord9.setId(9L);
        RenovationRecord testRenovationRecord10 = new RenovationRecord();
        testRenovationRecord10.setId(10L);
        RenovationRecord testRenovationRecord11 = new RenovationRecord();
        testRenovationRecord11.setId(11L);

        when(renovationRecordService.getRecordById(2L)).thenReturn(testRenovationRecord2);
        when(renovationRecordService.getRecordById(3L)).thenReturn(testRenovationRecord3);
        when(renovationRecordService.getRecordById(4L)).thenReturn(testRenovationRecord4);
        when(renovationRecordService.getRecordById(5L)).thenReturn(testRenovationRecord5);
        when(renovationRecordService.getRecordById(6L)).thenReturn(testRenovationRecord6);
        when(renovationRecordService.getRecordById(7L)).thenReturn(testRenovationRecord7);
        when(renovationRecordService.getRecordById(8L)).thenReturn(testRenovationRecord8);
        when(renovationRecordService.getRecordById(9L)).thenReturn(testRenovationRecord9);
        when(renovationRecordService.getRecordById(10L)).thenReturn(testRenovationRecord10);
        when(renovationRecordService.getRecordById(11L)).thenReturn(testRenovationRecord11);

        userService.updateRecentRenovations(testUser, testRenovationRecord1);
        userService.updateRecentRenovations(testUser, testRenovationRecord2);
        userService.updateRecentRenovations(testUser, testRenovationRecord3);
        userService.updateRecentRenovations(testUser, testRenovationRecord4);
        userService.updateRecentRenovations(testUser, testRenovationRecord5);
        userService.updateRecentRenovations(testUser, testRenovationRecord6);
        userService.updateRecentRenovations(testUser, testRenovationRecord7);
        userService.updateRecentRenovations(testUser, testRenovationRecord8);
        userService.updateRecentRenovations(testUser, testRenovationRecord9);
        userService.updateRecentRenovations(testUser, testRenovationRecord10);
        userService.updateRecentRenovations(testUser, testRenovationRecord11);

        List<RenovationRecord> expected = Arrays.asList(testRenovationRecord11, testRenovationRecord10, testRenovationRecord9, testRenovationRecord8, testRenovationRecord7,
                testRenovationRecord6, testRenovationRecord5, testRenovationRecord4, testRenovationRecord3, testRenovationRecord2);
        List<RenovationRecord> actual = userService.getRecentRenovations(testUser);
        assertTrue(actual.containsAll(expected));
        assertEquals(expected.size(), actual.size());
    }

    @Test
    public void userHasNoRatings_CallGetAverageRating_ZeroReturned() {
        assertEquals(0, testUser.getAverageRating());
    }

    @Test
    public void userWithNoRatingsReceivesNewRating_CallGetAverageRating_NewRatingValueReturned() {
        User sendingUser = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        Rating rating = new Rating(5, testUser, sendingUser);
        testUser.setReceivedRatings(List.of(rating));
        assertEquals(5, testUser.getAverageRating());
    }

    @Test
    public void userWithMultipleRatings_CallGetAverageRating_AverageRatingValueReturned() {
        User sendingUser = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        List<Rating> ratings = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ratings.add(new Rating(i, testUser, sendingUser));
        }
        testUser.setReceivedRatings(ratings);
        assertEquals(3, testUser.getAverageRating());

        ratings.add(new Rating(5, testUser, sendingUser));
        testUser.setReceivedRatings(ratings);
        assertEquals(3.33, testUser.getAverageRating());
    }

    @Test
    public void getPortfolioJobs_NoCompletedJobs_NoPortfolioJobsReturned() {
        Job job = new Job(null, null, null, null);

        Quote quote = new Quote(null, null, null, null, null);
        quote.setId(1L);
        quote.setUser(testUser);
        quote.setJob(job);
        quote.setStatus("Accepted");

        when(quoteRepository.findAllByUserId(testUser.getId())).thenReturn(List.of(quote));
        assertEquals(0, userService.getPortfolioJobs(testUser).size());
    }

    @Test
    public void getPortfolioJobs_CompletedJobsExistAndInUserPortfolioJobs_PortfolioJobsReturned() {
        Job job = new Job(null, null, null, null);
        job.setStatus("Completed");

        Quote quote = new Quote(null, null, null, null, null);
        quote.setId(1L);
        quote.setUser(testUser);
        quote.setJob(job);
        quote.setStatus("Accepted");

        job.addPortfolioUser(testUser);
        testUser.addPortfolioJob(job);

        when(quoteRepository.findAllByUserId(testUser.getId())).thenReturn(List.of(quote));
        assertEquals(1, userService.getPortfolioJobs(testUser).size());
    }

    @Test
    public void getHottestTradies_BlueSkyScenario() {
        User dumbledore = new User("dumbledore", "dumbledore", "hogwarts@gmail.com", "dumbledore", "dumbledore", "dumbledore");
        User harry2 = new User("harry2", "potter", "harrypotter", "potter", "potter", "potter");
        User harry3 = new User("harry3", "potter", "harrypotter", "potter", "potter", "potter");
        User harry4 = new User("harry4", "potter", "harrypotter", "potter", "potter", "potter");
        User harry5 = new User("harry5", "potter", "harrypotter", "potter", "potter", "potter");
        dumbledore.setId(1L);
        harry2.setId(2L);
        harry3.setId(3L);
        harry4.setId(4L);
        harry5.setId(5L);
        RenovationRecord hogwarts = new RenovationRecord();
        hogwarts.setId(1L);
        hogwarts.setUserEmail("hogwarts@gmail.com");
        Job hogwartsJob = new Job(null, null, null, null);
        hogwartsJob.setId(1L);
        hogwartsJob.setStatus("Completed");
        Job hogwartsJob2 = new Job(null, null, null, null);
        hogwartsJob2.setId(2L);
        hogwartsJob2.setStatus("Completed");
        Job hogwartsJob3 = new Job(null, null, null, null);
        hogwartsJob3.setId(3L);
        hogwartsJob3.setStatus("Completed");
        Job hogwartsJob4 = new Job(null, null, null, null);
        hogwartsJob4.setId(4L);
        hogwartsJob4.setStatus("Completed");
        hogwartsJob4.setRenovationRecord(hogwarts);
        hogwartsJob2.setRenovationRecord(hogwarts);
        hogwartsJob3.setRenovationRecord(hogwarts);

        Quote hogwartsQuote1 = new Quote(null, null, null, null, null);
        hogwartsQuote1.setId(1L);
        hogwartsQuote1.setJob(hogwartsJob);
        hogwartsQuote1.setStatus("Accepted");
        Quote hogwartsQuote2 = new Quote(null, null, null, null, null);
        hogwartsQuote2.setId(2L);
        hogwartsQuote2.setJob(hogwartsJob);
        hogwartsQuote2.setStatus("Accepted");
        Quote hogwartsQuote3 = new Quote(null, null, null, null, null);
        hogwartsQuote3.setId(3L);
        hogwartsQuote3.setJob(hogwartsJob2);
        hogwartsQuote3.setStatus("Accepted");
        Quote hogwartsQuote4 = new Quote(null, null, null, null, null);
        hogwartsQuote4.setId(4L);
        hogwartsQuote4.setJob(hogwartsJob);
        hogwartsQuote4.setStatus("Accepted");
        Quote hogwartsQuote5 = new Quote(null, null, null, null, null);
        hogwartsQuote5.setId(5L);
        hogwartsQuote5.setJob(hogwartsJob2);
        hogwartsQuote5.setStatus("Accepted");
        Quote hogwartsQuote6 = new Quote(null, null, null, null, null);
        hogwartsQuote6.setId(6L);
        hogwartsQuote6.setJob(hogwartsJob3);
        hogwartsQuote6.setStatus("Accepted");
        Quote hogwartsQuote7 = new Quote(null, null, null, null, null);
        hogwartsQuote7.setId(7L);
        hogwartsQuote7.setJob(hogwartsJob);
        hogwartsQuote7.setStatus("Accepted");
        Quote hogwartsQuote8 = new Quote(null, null, null, null, null);
        hogwartsQuote8.setId(8L);
        hogwartsQuote8.setJob(hogwartsJob2);
        hogwartsQuote8.setStatus("Accepted");
        Quote hogwartsQuote9 = new Quote(null, null, null, null, null);
        hogwartsQuote9.setId(9L);
        hogwartsQuote9.setJob(hogwartsJob3);
        hogwartsQuote9.setStatus("Accepted");
        Quote hogwartsQuote10 = new Quote(null, null, null, null, null);
        hogwartsQuote10.setId(10L);
        hogwartsQuote10.setJob(hogwartsJob4);
        hogwartsQuote10.setStatus("Accepted");

        hogwartsQuote1.setUser(harry2);
        hogwartsQuote2.setUser(harry3);
        hogwartsQuote3.setUser(harry3);
        hogwartsQuote4.setUser(harry4);
        hogwartsQuote5.setUser(harry4);
        hogwartsQuote6.setUser(harry4);
        hogwartsQuote7.setUser(harry5);
        hogwartsQuote8.setUser(harry5);
        hogwartsQuote9.setUser(harry5);
        hogwartsQuote10.setUser(harry5);
        harry2.setQuotes(new ArrayList<>(List.of(hogwartsQuote1)));
        harry3.setQuotes(new ArrayList<>(List.of(hogwartsQuote2, hogwartsQuote3)));
        harry4.setQuotes(new ArrayList<>(List.of(hogwartsQuote4, hogwartsQuote5, hogwartsQuote6)));
        harry5.setQuotes(new ArrayList<>(List.of(hogwartsQuote7, hogwartsQuote8, hogwartsQuote9, hogwartsQuote10)));

        List<User> mockedTradies = List.of(
                dumbledore, harry5, harry4, harry2, harry3
        ) ;

        List<User> expectedTradies = List.of(
                harry5, harry4, harry3, harry2, dumbledore
        ) ;
        Mockito.when(userRepository.findAllQuoteSenders()).thenReturn(mockedTradies);

        assertEquals(expectedTradies, userService.getHottestTradies());
    }

    @Test
    public void getUsersWorkEfficiency_NoCompletedJobs_ReturnsZero() {
        doReturn(Collections.emptyList()).when(spyUserService).getCompletedJobsUserHasWorkedOn(testUser2.getId());

        double efficiency = spyUserService.getUsersWorkEfficiency(testUser2.getId());
        assertEquals(0.0, efficiency);
    }

    @Test
    public void getUsersWorkEfficiency_SingleJob_OnTime_ReturnsOne() {
        Job job = new Job();
        job.setStartDate("01/01/2023");
        job.setDueDate("11/01/2023");
        job.setCompletedTimestamp(LocalDate.of(2023, 1, 11));

        doReturn(List.of(job)).when(spyUserService).getCompletedJobsUserHasWorkedOn(testUser2.getId());

        double efficiency = spyUserService.getUsersWorkEfficiency(testUser2.getId());
        assertEquals(1.0, efficiency);
    }

    @Test
    public void getUsersWorkEfficiency_SingleJob_FinishedEarly_ReturnsLessThanOne() {
        Job job = new Job();
        job.setStartDate("01/01/2023");
        job.setDueDate("10/01/2023");
        job.setCompletedTimestamp(LocalDate.of(2023, 1, 5));

        doReturn(List.of(job)).when(spyUserService).getCompletedJobsUserHasWorkedOn(testUser2.getId());

        double efficiency = spyUserService.getUsersWorkEfficiency(testUser2.getId());
        assertTrue(efficiency < 1);
    }

    @Test
    public void getUsersWorkEfficiency_SingleJob_FinishedLate_ReturnsGreaterThanOne() {
        Job job = new Job();
        job.setStartDate("01/01/2023");
        job.setDueDate("11/01/2023");
        job.setCompletedTimestamp(LocalDate.of(2023, 1, 16));

        doReturn(List.of(job)).when(spyUserService).getCompletedJobsUserHasWorkedOn(testUser2.getId());

        double efficiency = spyUserService.getUsersWorkEfficiency(testUser2.getId());
        assertTrue(efficiency > 1);
    }

    @Test
    public void getUsersWorkEfficiency_MultipleJobs_AverageCalculatedCorrectly() {
        Job job1 = new Job();
        job1.setStartDate("01/01/2023");
        job1.setDueDate("11/01/2023");
        job1.setCompletedTimestamp(LocalDate.of(2023, 1, 10));

        Job job2 = new Job();
        job2.setStartDate("01/01/2023");
        job2.setDueDate("11/01/2023");
        job2.setCompletedTimestamp(LocalDate.of(2023, 1, 5));

        doReturn(List.of(job1, job2)).when(spyUserService).getCompletedJobsUserHasWorkedOn(testUser2.getId());

        double efficiency = spyUserService.getUsersWorkEfficiency(testUser2.getId());
        assertEquals(0.65, efficiency);
    }
}
