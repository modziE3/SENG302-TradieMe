package nz.ac.canterbury.seng302.homehelper.integration.service;

import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.dto.JobFilter;
import nz.ac.canterbury.seng302.homehelper.repository.JobRepository;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.ValidationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.stream.Stream;

import static org.mockito.Mockito.never;

@SpringBootTest
public class JobServiceIntegrationTest {
    @MockBean
    private JobRepository jobRepository;
    @Autowired
    private ValidationService validationService;
    @Autowired
    private JobService jobService;

    static Stream<Arguments> validJobDetails() {
        return Stream.of(
                Arguments.of("Job 1", "First job", "01/01/3025", "01/01/3024"),
                Arguments.of("Job 1", "First job", "", ""),
                Arguments.of("Job 1", "First job", null, null),
                Arguments.of("A", "First job", "01/01/3025", "01/01/3024"),
                Arguments.of("A".repeat(255), "First job", "01/01/3025", "01/01/3024"),
                Arguments.of("Job .", "First job", "01/01/3025", "01/01/3024"),
                Arguments.of("Job -", "First job", "01/01/3025", "01/01/3024"),
                Arguments.of("Job '", "First job", "01/01/3025", "01/01/3024"),
                Arguments.of("Job 1", "A", "01/01/3025", "01/01/3024"),
                Arguments.of("Job 1", "A".repeat(512), "01/01/3025", "01/01/3024"),
                Arguments.of("Job 1", "First job", "31/12/3025", "01/01/3024"),
                Arguments.of("Job 1", "First job", "28/02/3025", "01/01/3024")
        );
    }
    @ParameterizedTest
    @MethodSource("validJobDetails")
    public void validateJob_invalidJobDetails_ExceptionNotThrown(String name, String description,
                                                                 String dueDate, String startDate) {
        Assertions.assertDoesNotThrow(() ->
                jobService.validateJob(new Job(name, description, dueDate, startDate)));
    }

    static Stream<Arguments> invalidJobDetails() {
        return Stream.of(
                Arguments.of("", "First job", "01/01/3025", "01/01/3024"),
                Arguments.of(null, "First job", "01/01/3025", "01/01/3024"),
                Arguments.of("A".repeat(256), "First job", "01/01/3025", "01/01/3024"),
                Arguments.of("Job !", "First job", "01/01/3025", "01/01/3024"),
                Arguments.of("Job 1", "", "01/01/3025", "01/01/3024"),
                Arguments.of("Job 1", null, "01/01/3025", "01/01/3024"),
                Arguments.of("Job 1", "A".repeat(513), "01/01/3025", "01/01/3024"),
                Arguments.of("Job 1", "First job", "01/01/2025", "01/01/3024"),
                Arguments.of("Job 1", "First job", "1/1/3025", "01/01/3024"),
                Arguments.of("Job 1", "First job", "3025/01/01", "01/01/3024"),
                Arguments.of("Job 1", "First job", "01-01-3025", "01/01/3024"),
                Arguments.of("Job 1", "First job", "29/02/3025", "01/01/3024")
        );
    }
    @ParameterizedTest
    @MethodSource("invalidJobDetails")
    public void validateJob_invalidJobDetails_ExceptionThrown(String name, String description,
                                                              String dueDate, String startDate) {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                jobService.validateJob(new Job(name , description , dueDate, startDate)));

        Mockito.verify(jobRepository, never()).findAll();
    }

    static Stream<Arguments> validPostedJobDetails() {
        return Stream.of(
                Arguments.of("Carpentry"),
                Arguments.of("Electrical"),
                Arguments.of("Plumbing"),
                Arguments.of("Insulating"),
                Arguments.of("Plastering"),
                Arguments.of("Painting")
        );
    }
    @ParameterizedTest
    @MethodSource("validPostedJobDetails")
    public void validateJobBeforePosting_validJobDetails_ExceptionNotThrown(String type) {
        Job job = new Job("Job 1", "First job", "01/01/3025", "01/01/3024");
        job.setType(type);

        Assertions.assertDoesNotThrow(() ->
                jobService.validateJobBeforePosting(job));
    }

    static Stream<Arguments> invalidPostedJobDetails() {
        return Stream.of(
                Arguments.of("No Type", "01/01/3025", "01/01/3024"),
                Arguments.of("Carpentry", null, "01/01/3024"),
                Arguments.of("Carpentry", "01/01/3025", null)
        );
    }
    @ParameterizedTest
    @MethodSource("invalidPostedJobDetails")
    public void validateJobAfterPosting_invalidJobDetails_ExceptionThrown(String type, String dueDate, String startDate) {
        Job job = new Job("Job 1", "First job", dueDate, startDate);
        job.setType(type);

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                jobService.validateJobBeforePosting(job));
    }

    static Stream<Arguments> validJobFilters() {
        return Stream.of(
                Arguments.of(null, null, null, null, null, null),
                Arguments.of("Keyword", null, null, null, null, null),
                Arguments.of(null, "Carpentry", null, null, null, null),
                Arguments.of(null, null, "Christchurch", null, null, null),
                Arguments.of(null, null, "-", null, null, null),
                Arguments.of(null, null, "'", null, null, null),
                Arguments.of(null, null, " ", null, null, null),
                Arguments.of(null, null, "Christchurch", "Ilam", null, null),
                Arguments.of(null, null, "Christchurch", "2", null, null),
                Arguments.of(null, null, "Christchurch", "-", null, null),
                Arguments.of(null, null, "Christchurch", "'", null, null),
                Arguments.of(null, null, "Christchurch", " ", null, null),
                Arguments.of(null, null, null, null, "01/01/2026", null),
                Arguments.of(null, null, null, null, null, "01/01/2026")
        );
    }
    @ParameterizedTest
    @MethodSource("validJobFilters")
    public void validateJobFilter_validFilters_ExceptionNotThrown(
            String keyword, String jobType, String city, String suburb, String startDate, String dueDate) {
        JobFilter filter = new JobFilter(keyword, jobType, city, suburb, startDate, dueDate);
        Assertions.assertDoesNotThrow(() -> {
            jobService.validateJobFilter(filter);
        });
    }

    static Stream<Arguments> invalidJobFilters() {
        return Stream.of(
                Arguments.of(null, null, "2", null, null, null),
                Arguments.of(null, null, "!", null, null, null),
                Arguments.of(null, null, "\"\"", null, null, null),
                Arguments.of(null, null, "Christchurch", "!", null, null),
                Arguments.of(null, null, "Christchurch", "\"\"", null, null),
                Arguments.of(null, null, null, null, "2026/01/01", null),
                Arguments.of(null, null, null, null, "01/01/2024", null),
                Arguments.of(null, null, null, null, "31/02/2026", null),
                Arguments.of(null, null, null, null, null, "2026/01/01"),
                Arguments.of(null, null, null, null, null, "01/01/2024"),
                Arguments.of(null, null, null, null, null, "31/02/2026"),
                Arguments.of(null, null, null, null, "01/01/2027", "01/01/2026")
        );
    }
    @ParameterizedTest
    @MethodSource("invalidJobFilters")
    public void validateJobFilter_invalidFilters_ExceptionThrown(
            String keyword, String jobType, String city, String suburb, String startDate, String dueDate) {
        JobFilter filter = new JobFilter(keyword, jobType, city, suburb, startDate, dueDate);
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            jobService.validateJobFilter(filter);
        });
    }
}
