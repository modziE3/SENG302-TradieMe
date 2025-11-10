package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RoomRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.Tuple;


import java.util.*;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
public class RenovationRecordServiceTest {
    @Mock
    public RenovationRecordRepository renovationRecordRepository;
    @Mock
    public RoomRepository roomRepository;
    @InjectMocks
    public RenovationRecordService renovationRecordService;

    public List<RenovationRecord> makeRenovationRecordList(int value) {
        List<RenovationRecord> renovationRecords = new ArrayList<>();
        int i = 0;
        while (i < value) {
            renovationRecords.add(new RenovationRecord(("example" + i), "text", null, null));
            i++;
        }
        return renovationRecords;
    }


    @Test
    public void setEmptyLocationsToNull_LocationInfoProvided_EmptyInfoSetToNull() {
        List<String> locationInfo = List.of("", "Suburb", "", "Postcode", "Country");
        locationInfo = renovationRecordService.setEmptyLocationsToNull(locationInfo);
        Assertions.assertNull(locationInfo.get(0));
        Assertions.assertNotNull(locationInfo.get(1));
        Assertions.assertNull(locationInfo.get(2));
        Assertions.assertNotNull(locationInfo.get(3));
        Assertions.assertNotNull(locationInfo.get(4));
    }

    @Test
    public void setRenovationRecordLocation_NonEmptyLocationInfo_LocationInformationSaved() {
        RenovationRecord testRecord = new RenovationRecord("Name", "Description",
                null, null);
        List<String> locationInfo = List.of("Street Address", "Suburb", "City", "Postcode", "Country");
        renovationRecordService.setRenovationRecordLocation(testRecord, locationInfo);
        Assertions.assertEquals("Street Address", testRecord.getStreetAddress());
        Assertions.assertEquals("Suburb", testRecord.getSuburb());
        Assertions.assertEquals("City", testRecord.getCity());
        Assertions.assertEquals("Postcode", testRecord.getPostcode());
        Assertions.assertEquals("Country", testRecord.getCountry());
    }

    @Test
    public void editRenovationRecord_AllDetailsDifferent_RenovationRecordEdited() {
        List<Room> roomList = new ArrayList<>(List.of(new Room("Room 1"), new Room("Room 2")));
        RenovationRecord testRecord = new RenovationRecord("Record Name", "Record Description",
                roomList, null);

        List<Room> newRoomList = new ArrayList<>(List.of(new Room("Room 3"), new Room("Room 4")));
        renovationRecordService.editRenovationRecord(testRecord, "New Record Name",
                "New Record Description", newRoomList);
        Mockito.verify(renovationRecordRepository, Mockito.times(1)).save(testRecord);
        Mockito.verify(roomRepository, Mockito.times(2)).deleteById(Mockito.any());
        Mockito.verify(roomRepository, Mockito.times(2)).save(Mockito.any(Room.class));

        Assertions.assertEquals("New Record Name", testRecord.getName());
        Assertions.assertEquals("New Record Description", testRecord.getDescription());
        Assertions.assertEquals("New Record Name", newRoomList.get(0).getRenovationRecord().getName());
        Assertions.assertEquals("New Record Name", newRoomList.get(1).getRenovationRecord().getName());
    }

    @Test
    public void editRenovationRecordPublicStatus_StatusGiven_PublicStatusChanged() {
        RenovationRecord renovationRecord = new RenovationRecord("Record Name", "Record Description",
                null, null);
        Assertions.assertFalse(renovationRecord.getIsPublic());
        renovationRecordService.editRenovationRecordPublicStatus(renovationRecord, true);

        ArgumentCaptor<RenovationRecord> recordCaptor = ArgumentCaptor.forClass(RenovationRecord.class);
        Mockito.verify(renovationRecordRepository).save(recordCaptor.capture());
        RenovationRecord capturedRecord = recordCaptor.getValue();
        Assertions.assertTrue(capturedRecord.getIsPublic());
    }

    static Stream<Arguments> expectedErrorMessages() {
        return Stream.of(
                Arguments.of(RenovationRecordService.RENO_TITLE_NOT_UNIQUE,
                        RenovationRecordService.RENO_TITLE_NOT_UNIQUE, "", ""),
                Arguments.of(RenovationRecordService.RENO_TITLE_NOT_UNIQUE +
                                RenovationRecordService.RENO_TITLE_INCORRECT_CHARACTER,
                        RenovationRecordService.RENO_TITLE_INCORRECT_CHARACTER, "", ""),
                Arguments.of(RenovationRecordService.RENO_TITLE_NOT_UNIQUE +
                                RenovationRecordService.RENO_TITLE_INCORRECT_CHARACTER +
                                RenovationRecordService.RENO_TITLE_EMPTY,
                        RenovationRecordService.RENO_TITLE_EMPTY, "", ""),
                Arguments.of(RenovationRecordService.RENO_TITLE_NOT_UNIQUE +
                                RenovationRecordService.RENO_TITLE_INCORRECT_CHARACTER +
                                RenovationRecordService.RENO_TITLE_EMPTY +
                                RenovationRecordService.RENO_TITLE_TOO_LONG,
                        RenovationRecordService.RENO_TITLE_TOO_LONG, "", ""),
                Arguments.of(RenovationRecordService.RENO_TITLE_TOO_LONG +
                                RenovationRecordService.RENO_DESCRIPTION_EMPTY,
                        RenovationRecordService.RENO_TITLE_TOO_LONG, RenovationRecordService.RENO_DESCRIPTION_EMPTY, ""),
                Arguments.of(RenovationRecordService.RENO_TITLE_TOO_LONG +
                                RenovationRecordService.RENO_DESCRIPTION_EMPTY +
                                RenovationRecordService.RENO_DESCRIPTION_TOO_LONG,
                        RenovationRecordService.RENO_TITLE_TOO_LONG, RenovationRecordService.RENO_DESCRIPTION_TOO_LONG, ""),
                Arguments.of(RenovationRecordService.RENO_TITLE_TOO_LONG +
                                RenovationRecordService.RENO_DESCRIPTION_TOO_LONG +
                                RenovationRecordService.RENO_ROOM_NAME_INVALID,
                        RenovationRecordService.RENO_TITLE_TOO_LONG, RenovationRecordService.RENO_DESCRIPTION_TOO_LONG,
                        RenovationRecordService.RENO_ROOM_NAME_INVALID)
        );
    }
    @ParameterizedTest
    @MethodSource("expectedErrorMessages")
    public void getErrorMessages_AllErrorMessagesStringProvided_CorrectSeparateErrorMessagesReturned
            (String allErrorMessages, String renovationTitleErrorMessage, String renovationDescriptionErrorMessage,
             String renovationRoomsErrorMessage) {

        List<String> errorMessages = renovationRecordService.getErrorMessages(allErrorMessages);

        Assertions.assertEquals(renovationTitleErrorMessage, errorMessages.get(0));
        Assertions.assertEquals(renovationDescriptionErrorMessage, errorMessages.get(1));
        Assertions.assertEquals(renovationRoomsErrorMessage, errorMessages.get(2));
    }

    @Test
    public void checkIsUnique_UniqueNameGiven_ReturnsTrue() {
        Mockito.when(renovationRecordRepository.findRenovationRecordsByEmail(Mockito.anyString())).thenReturn(
                List.of(new RenovationRecord("Record 1", "Record Description",
                        null, null)));
        Assertions.assertTrue(renovationRecordService.recordNameUnique("Record 2", "john@example.com"));
    }

    @Test
    public void checkIsUnique_NotUniqueNameGiven_ReturnsFalse() {
        Mockito.when(renovationRecordRepository.findRenovationRecordsByEmail(Mockito.anyString())).thenReturn(
                List.of(new RenovationRecord("Record 1", "Record Description",
                        null, null)));
        Assertions.assertFalse(renovationRecordService.recordNameUnique("Record 1", "john@example.com"));
    }

    @Test
    public void getVisiblePaginationPages_GreaterThan10_FinalPage() {
        int currentPage = 11;
        List<Integer> expectedPages = Arrays.asList(1, 9, 10, 11);

        Assertions.assertEquals(expectedPages, renovationRecordService.getPageList(currentPage, makeRenovationRecordList(12*11)));
    }

    @Test
    public void getVisiblePaginationPages_GreaterThan10_FirstPage() {
        int currentPage = 1;
        List<Integer> expectedPages = Arrays.asList(1, 2, 3, 11);

        Assertions.assertEquals(expectedPages, renovationRecordService.getPageList(currentPage, makeRenovationRecordList(12*11)));
    }

    @Test
    public void getVisiblePaginationPages_GreaterThan10_TwoFromFirstPage() {
        int currentPage = 3;
        List<Integer> expectedPages = Arrays.asList(1, 2, 3, 4, 5, 11);

        Assertions.assertEquals(expectedPages, renovationRecordService.getPageList(currentPage, makeRenovationRecordList(12*11)));
    }

    @Test
    public void getVisiblePaginationPages_GreaterThan10_TwoFromFinalPage() {
        int currentPage = 9;
        List<Integer> expectedPages = Arrays.asList(1, 7, 8, 9, 10, 11);

        Assertions.assertEquals(expectedPages, renovationRecordService.getPageList(currentPage, makeRenovationRecordList(12*11)));
    }

    @Test
    public void getVisiblePaginationPages_GreaterThan10_MiddlePage() {
        int currentPage = 6;
        List<Integer> expectedPages = Arrays.asList(1, 4, 5, 6, 7, 8, 11);

        Assertions.assertEquals(expectedPages, renovationRecordService.getPageList(currentPage, makeRenovationRecordList(12*11)));
    }

    @Test
    public void getVisiblePaginationPages_LessThan10Pages() {
        int currentPage = 2;
        List<Integer> expectedPages = Arrays.asList(1, 2, 3, 4, 5, 6);

        Assertions.assertEquals(expectedPages, renovationRecordService.getPageList(currentPage, makeRenovationRecordList(6*12)));
    }

    @Test
    public void getFirstPageResults_MoreThanOnePage() {
        int currentPage = 1;
        List<RenovationRecord> recordList = makeRenovationRecordList(46);

        Assertions.assertEquals(recordList.subList(0, 12), renovationRecordService.getRenovationRecordPages(currentPage, recordList));
    }

    @Test
    public void getCityandSuburb_ReturnsExpectedCityandSuburb() {
        Tuple tuple1 = Mockito.mock(Tuple.class);
        Tuple tuple2 = Mockito.mock(Tuple.class);
        Tuple tuple3 = Mockito.mock(Tuple.class);
        Tuple tuple4 = Mockito.mock(Tuple.class);

        Mockito.when(tuple1.get(0)).thenReturn("Christchurch");
        Mockito.when(tuple1.get(1)).thenReturn("Ilam");

        Mockito.when(tuple2.get(0)).thenReturn("Auckland");
        Mockito.when(tuple2.get(1)).thenReturn("Epsom");

        Mockito.when(tuple3.get(0)).thenReturn("Tauranga");
        Mockito.when(tuple3.get(1)).thenReturn("Greerton");

        Mockito.when(tuple4.get(0)).thenReturn("Christchurch");
        Mockito.when(tuple4.get(1)).thenReturn("Riccarton");

        Mockito.when(renovationRecordRepository.findCitySuburbs()).thenReturn(List.of(tuple1, tuple2, tuple3, tuple4));

        Map<String, List<String>> expected = new HashMap<>();
        expected.put("Christchurch", List.of("Ilam", "Riccarton"));
        expected.put("Auckland", List.of("Epsom"));
        expected.put("Tauranga", List.of("Greerton"));

        Map<String, List<String>> actual = renovationRecordService.getCityAndSuburb();

        Assertions.assertEquals(expected, actual);
    }
}
