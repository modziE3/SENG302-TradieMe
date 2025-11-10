package nz.ac.canterbury.seng302.homehelper.unit.repository;

import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.repository.JobRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@DataJpaTest
public class JobRepositoryTest {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private TestEntityManager entityManager;

    private RenovationRecord firstRecord;
    private Job firstJob;
    private Job secondJob;
    private Job thirdJob;
    private Job fourthJob;
    private Job fifthJob;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        firstRecord = new RenovationRecord("Kitchen", "blue tiles and black oven", new ArrayList<>(), "user@email.com");

        firstJob = new Job("job1", "job1", "28/02/2077", "28/03/2077");
        firstJob.setRenovationRecord(firstRecord);
        firstJob.setStatus("Not Started");
        firstJob.setType("Carpentry");
        firstJob.setIsPosted(true);

        secondJob = new Job("job2", "job2", "28/02/2077", "28/03/2077");
        secondJob.setRenovationRecord(firstRecord);
        secondJob.setStatus("In Progress");
        secondJob.setType("Carpentry");
        secondJob.setIsPosted(true);

        thirdJob = new Job("job3", "job3", "28/02/2077", "28/03/2077");
        thirdJob.setRenovationRecord(firstRecord);
        thirdJob.setStatus("Completed");
        thirdJob.setType("Plumbing");
        thirdJob.setIsPosted(true);

        fourthJob = new Job("job4", "job4", "28/02/2077", "28/03/2077");
        fourthJob.setRenovationRecord(firstRecord);
        fourthJob.setStatus("Blocked");
        fourthJob.setIsPosted(true);

        fifthJob = new Job("job5", "job5 \uD83D\uDE00", "28/02/2077", "28/03/2077");
        fifthJob.setRenovationRecord(firstRecord);
        fifthJob.setStatus("Cancelled");
        fifthJob.setIsPosted(true);

        entityManager.persist(firstRecord);
        entityManager.persist(firstJob);
        entityManager.persist(secondJob);
        entityManager.persist(thirdJob);
        entityManager.persist(fourthJob);
        entityManager.persist(fifthJob);

        pageable  = PageRequest.of(0, 5);

    }

    @Test
    public void returnJobs_Filter_NotStarted() {
        List<Job> returnedJobs = jobRepository.findAllFromRenovationRecordFiltered(firstRecord, pageable, "Not Started").stream().toList();
        List<Job> expectedJobs = Collections.singletonList(firstJob);
        Assertions.assertEquals(expectedJobs, returnedJobs);
    }

    @Test
    public void returnJobs_Filter_InProgress() {
        List<Job> returnedJobs = jobRepository.findAllFromRenovationRecordFiltered(firstRecord, pageable, "In Progress").stream().toList();
        List<Job> expectedJobs = Collections.singletonList(secondJob);
        Assertions.assertEquals(expectedJobs, returnedJobs);
    }

    @Test
    public void returnJobs_Filter_Completed() {
        List<Job> returnedJobs = jobRepository.findAllFromRenovationRecordFiltered(firstRecord, pageable, "Completed").stream().toList();
        List<Job> expectedJobs = Collections.singletonList(thirdJob);
        Assertions.assertEquals(expectedJobs, returnedJobs);
    }

    @Test
    public void returnJobs_Filter_Blocked() {
        List<Job> returnedJobs = jobRepository.findAllFromRenovationRecordFiltered(firstRecord, pageable, "Blocked").stream().toList();
        List<Job> expectedJobs = Collections.singletonList(fourthJob);
        Assertions.assertEquals(expectedJobs, returnedJobs);
    }

    @Test
    public void returnJobs_Filter_Cancelled() {
        List<Job> returnedJobs = jobRepository.findAllFromRenovationRecordFiltered(firstRecord, pageable, "Cancelled").stream().toList();
        List<Job> expectedJobs = Collections.singletonList(fifthJob);
        Assertions.assertEquals(expectedJobs, returnedJobs);
    }

    @Test
    public void returnPostedJobs_TypeFilter_NoFilter() {
        List<Job> returnedJobs = jobRepository.findPostedJobs();
        List<Job> expectedJobs = Arrays.asList(firstJob, secondJob, thirdJob, fourthJob, fifthJob);
        Assertions.assertEquals(expectedJobs, returnedJobs);
    }

    @Test
    public void returnPostedJobs_TypeFilter_TypeWithJobs() {
        List<Job> returnedJobs = jobRepository.findPostedJobs();
        returnedJobs = returnedJobs.stream().filter(j -> j.getType().equals("Carpentry")).toList();
        List<Job> expectedJobs = Arrays.asList(firstJob, secondJob);
        Assertions.assertEquals(expectedJobs, returnedJobs);
    }

    @Test
    public void returnPostedJobs_TypeFilter_TypeWithNoJobs() {
        List<Job> returnedJobs = jobRepository.findPostedJobs();
        returnedJobs = returnedJobs.stream().filter(j -> j.getType().equals("Painting")).toList();
        List<Job> expectedJobs = Collections.emptyList();
        Assertions.assertEquals(expectedJobs, returnedJobs);
    }

    @Test
    public void findFilteredJobs_JobWithPartialKeywordsAreSearched_TheGivenJobsAreFound() {
        Job job = new Job("Keyword", "desc", "28/02/2077", "28/03/2077");
        job.setRenovationRecord(firstRecord);
        job.setIsPosted(true);
        entityManager.persist(job);
        List<Job> returnedJobs = jobRepository.findPostedJobs();
        returnedJobs = returnedJobs.stream().filter(j -> j.getName().contains("word") || j.getDescription().contains("word")).toList();
        List<Job> expectedJobs = Collections.singletonList(job);
        Assertions.assertEquals(expectedJobs, returnedJobs);
    }

    @ParameterizedTest
    @CsvSource({
            "job1, 1",
            "ob2, 1",
            "invalid, 0",
            "\uD83D\uDE00, 1",
            "job, 5",
            "job   , 5"
    })
    void testWithKeywordAndExpectedCount(String keyword, int expectedCount) {
        List<Job> result = jobRepository.findPostedJobs();
        result = result.stream().filter(j -> j.getName().contains(keyword) || j.getDescription().contains(keyword)).toList();
        Assertions.assertEquals(expectedCount, result.size());
    }

}
