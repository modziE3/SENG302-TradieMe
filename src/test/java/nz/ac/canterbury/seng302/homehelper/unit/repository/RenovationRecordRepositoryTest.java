package nz.ac.canterbury.seng302.homehelper.unit.repository;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;



@DataJpaTest
public class RenovationRecordRepositoryTest {

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;
    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TestEntityManager entityManager;

    private RenovationRecord firstRecord;
    private RenovationRecord secondRecord;
    private RenovationRecord thirdRecord;
    private RenovationRecord fourthRecord;
    private Tag firstTag;
    private Tag secondTag;
    private Tag thirdTag;
    private Tag fourthTag;


    @BeforeEach
    void setUp() {
        firstTag = new Tag("firstTag");
        secondTag = new Tag("secondTag");
        thirdTag = new Tag("thirdTag");
        fourthTag = new Tag("fourthTag");

        firstRecord = new RenovationRecord("Kitchen", "blue tiles and black oven", new ArrayList<>(), "user@email.com");
        secondRecord = new RenovationRecord("Bathroom", "white and blue tiles", new ArrayList<>(), "user@email.com");
        thirdRecord = new RenovationRecord("NotKitchen", "no tiles, Bathroom", new ArrayList<>(), "user@email.com");
        fourthRecord = new RenovationRecord("GARAGE", "THE GARAGE", new ArrayList<>(), "user@email.com");

        firstTag.addRenovationRecord(firstRecord);
        secondTag.addRenovationRecord(secondRecord);
        thirdTag.addRenovationRecord(thirdRecord);
        firstTag.addRenovationRecord(fourthRecord);
        fourthTag.addRenovationRecord(firstRecord);
        fourthTag.addRenovationRecord(fourthRecord);
        firstRecord.addTag(firstTag);
        firstRecord.addTag(fourthTag);
        secondRecord.addTag(secondTag);
        thirdRecord.addTag(thirdTag);
        fourthRecord.addTag(firstTag);
        fourthRecord.addTag(fourthTag);


        entityManager.persist(firstRecord);
        entityManager.persist(secondRecord);
        entityManager.persist(thirdRecord);
        entityManager.persist(fourthRecord);
        entityManager.persist(firstTag);
        entityManager.persist(secondTag);
        entityManager.persist(thirdTag);
        entityManager.persist(fourthTag);


    }

    @Test
    public void returnRecordsMatching_StringAndName() {
        List<RenovationRecord> retrievedRecord = renovationRecordRepository.findMatchingRenovationRecordsBySearchString("Kitchen", "user@email.com");
        List<RenovationRecord> expectedRecords = Arrays.asList(firstRecord, thirdRecord);
        Assertions.assertEquals(expectedRecords, retrievedRecord);
    }

    @Test
    public void returnRecordsMatching_StringAndDescription() {
        List<RenovationRecord> retrievedRecord = renovationRecordRepository.findMatchingRenovationRecordsBySearchString("blue tiles", "user@email.com");
        List<RenovationRecord> expectedRecords = Arrays.asList(firstRecord, secondRecord);
        Assertions.assertEquals(expectedRecords, retrievedRecord);
    }

    @Test
    public void returnRecordsMatchingString_NoMatches() {
        List<RenovationRecord> retrievedRecord = renovationRecordRepository.findMatchingRenovationRecordsBySearchString("Living room", "user@email.com");
        Assertions.assertEquals(0, retrievedRecord.size());
    }

    @Test
    public void returnRecordsMatching_StringAndNameAndDescription() {
        List<RenovationRecord> retrievedRecord = renovationRecordRepository.findMatchingRenovationRecordsBySearchString("bathroom", "user@email.com");
        List<RenovationRecord> expectedRecords = Arrays.asList(secondRecord, thirdRecord);
        Assertions.assertEquals(expectedRecords, retrievedRecord);
    }

    @Test
    public void returnRecordsMatching_StringCaseInsensitive_UpperCase() {
        List<RenovationRecord> retrievedRecord = renovationRecordRepository.findMatchingRenovationRecordsBySearchString("KITCHEN", "user@email.com");
        List<RenovationRecord> expectedRecords = Arrays.asList(firstRecord, thirdRecord);
        Assertions.assertEquals(expectedRecords, retrievedRecord);
    }

    @Test
    public void returnRecordsMatching_StringCaseInsensitive_LowerCase() {
        List<RenovationRecord> retrievedRecord = renovationRecordRepository.findMatchingRenovationRecordsBySearchString("garage", "user@email.com");
        List<RenovationRecord> expectedRecords = Collections.singletonList(fourthRecord);
        Assertions.assertEquals(expectedRecords, retrievedRecord);
    }

    @Test
    public void returnRecordsMatchingStringAndTag_OnlyTags() {
        List<RenovationRecord> retrievedRecords = renovationRecordRepository.findMatchingRecordsByEmptyStringAndTags("user@email.com", List.of("firstTag", "fourthTag"));
        List<RenovationRecord> expectedRecords = Arrays.asList(firstRecord, fourthRecord);
        Assertions.assertEquals(expectedRecords, retrievedRecords);
    }

    @Test
    public void returnRecordsMatchingStringAndTags_StringAndTags_NoMatch() {
        List<RenovationRecord> retrievedRecords = renovationRecordRepository.findMatchingRecordsByStringAndTags("NotKitchen","user@email.com", List.of("firstTag", "secondTag"));
        List<RenovationRecord> expectedRecords = Collections.emptyList();
        Assertions.assertEquals(expectedRecords, retrievedRecords);
    }

    @Test
    public void returnRecordsMatchingStringAndTags_StringAndTags_Match() {
        List<RenovationRecord> retrievedRecord = renovationRecordRepository.findMatchingRecordsByStringAndTags("Kitchen", "user@email.com", List.of("firstTag", "fourthTag"));
        List<RenovationRecord> expectedRecords = Collections.singletonList(firstRecord);
        Assertions.assertEquals(expectedRecords, retrievedRecord);
    }
}
