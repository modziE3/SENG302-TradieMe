package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.controller.renovations.RenovationFormController;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class RenovationFormControllerIntegrationTest {

    @Autowired
    private RenovationFormController renovationFormController;

    private MockMvc mockMvc;

    @Autowired
    private RenovationRecordService renovationRecordService;

    @MockBean
    private RenovationRecordRepository renovationRecordRepository;

    private Principal mockPrincipal;

    @PostConstruct
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(renovationFormController).build();
    }

    @BeforeEach
    public void before() {
        mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("john@example.com");
    }

    @Test
    void testAddRenovationFormWithValidValues_NoRooms() throws Exception {
        mockMvc.perform(post("/my-renovations/create-renovation")
                .param("title", "Test")
                .param("description", "Valid Description")
                .param("rooms", "")
                .param("prevPage", "")
                .param("streetAddress", "")
                .param("suburb","")
                .param("city", "")
                .param("postcode", "")
                .param("country", "")
                .param("latitude", String.valueOf(0))
                .param("longitude", String.valueOf(0)).principal(mockPrincipal));

        ArgumentCaptor<RenovationRecord> argument = ArgumentCaptor.forClass(RenovationRecord.class);
        Mockito.verify(renovationRecordRepository).save(argument.capture());
        RenovationRecord renovationRecord = argument.getValue();
        Assertions.assertEquals("Test", renovationRecord.getName());
        Assertions.assertEquals("Valid Description", renovationRecord.getDescription());
        Assertions.assertEquals(0, renovationRecord.getRooms().size());
    }

    @Test
    void testAddRenovationFormWithValidValues_WithRooms() throws Exception {
        mockMvc.perform(post("/my-renovations/create-renovation")
                .param("title", "Test")
                .param("description", "Valid Description")
                .param("rooms", "Kitchen`Bathroom")
                .param("prevPage", "")
                .param("streetAddress", "")
                .param("suburb","")
                .param("city", "")
                .param("postcode", "")
                .param("country", "")
                .param("latitude", String.valueOf(0))
                .param("longitude", String.valueOf(0)).principal(mockPrincipal));

        List<String> rooms = new ArrayList<>();
        rooms.add("Kitchen");
        rooms.add("Bathroom");

        ArgumentCaptor<RenovationRecord> argument = ArgumentCaptor.forClass(RenovationRecord.class);
        Mockito.verify(renovationRecordRepository).save(argument.capture());
        RenovationRecord renovationRecord = argument.getValue();
        Assertions.assertEquals("Test", renovationRecord.getName());
        Assertions.assertEquals("Valid Description", renovationRecord.getDescription());
        Assertions.assertEquals(2, renovationRecord.getRooms().size());
        Assertions.assertEquals(rooms, renovationRecord.getRooms().stream().map(Room::getName).toList());
    }

    @Test
    void testAddRenovationFormWithBlankTitle() throws Exception {
        mockMvc.perform(post("/my-renovations/create-renovation")
                        .param("title", "")
                        .param("description", "Valid Description")
                        .param("rooms", "")
                        .param("prevPage", "")
                        .param("streetAddress", "")
                        .param("suburb","")
                        .param("city", "")
                        .param("postcode", "")
                        .param("country", "")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)).principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("renoName", ""))
                .andExpect(model().attribute("renoDescription", "Valid Description"))
                .andExpect(model().attribute("renoRooms", "[]"))
                .andExpect(model().attribute("renovationTitleErrorMessage", RenovationRecordService.RENO_TITLE_EMPTY));
    }

    @Test
    void testAddRenovationFormWithInvalidTitle () throws Exception {
        mockMvc.perform(post("/my-renovations/create-renovation")
                        .param("title", "title?")
                        .param("description", "Valid Description")
                        .param("rooms", "")
                        .param("prevPage", "")
                        .param("streetAddress", "")
                        .param("suburb","")
                        .param("city", "")
                        .param("postcode", "")
                        .param("country", "")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)).principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("renoName", "title?"))
                .andExpect(model().attribute("renoDescription", "Valid Description"))
                .andExpect(model().attribute("renoRooms", "[]"))
                .andExpect(model().attribute("renovationTitleErrorMessage", RenovationRecordService.RENO_TITLE_INCORRECT_CHARACTER));
    }

    @Test
    void testAddRenovationFormWithEmptyDescription () throws Exception {
        mockMvc.perform(post("/my-renovations/create-renovation")
                        .param("title", "Test")
                        .param("description", "")
                        .param("rooms", "")
                        .param("prevPage", "")
                        .param("streetAddress", "")
                        .param("suburb","")
                        .param("city", "")
                        .param("postcode", "")
                        .param("country", "")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)).principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("renoName", "Test"))
                .andExpect(model().attribute("renoDescription", ""))
                .andExpect(model().attribute("renoRooms", "[]"))
                .andExpect(model().attribute("renovationDescriptionErrorMessage",
                        RenovationRecordService.RENO_DESCRIPTION_EMPTY));
    }

    @Test
    void testAddRenovationFormWithTooLongDescription () throws Exception {
        mockMvc.perform(post("/my-renovations/create-renovation")
                        .param("title", "Test")
                        .param("description", "A".repeat(513))
                        .param("rooms", "")
                        .param("prevPage", "")
                        .param("streetAddress", "")
                        .param("suburb","")
                        .param("city", "")
                        .param("postcode", "")
                        .param("country", "")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)).principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("renoName", "Test"))
                .andExpect(model().attribute("renoDescription", "A".repeat(513)))
                .andExpect(model().attribute("renoRooms", "[]"))
                .andExpect(model().attribute("renovationDescriptionErrorMessage",
                        RenovationRecordService.RENO_DESCRIPTION_TOO_LONG));
    }

    @Test
    void testAddRenovationWithInvalidRoomName () throws Exception {
        mockMvc.perform(post("/my-renovations/create-renovation")
                        .param("title", "Test")
                        .param("description", "Valid Description")
                        .param("rooms", "Kitchen?")
                        .param("prevPage", "")
                        .param("streetAddress", "")
                        .param("suburb","")
                        .param("city", "")
                        .param("postcode", "")
                        .param("country", "")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)).principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("renoName", "Test"))
                .andExpect(model().attribute("renoDescription", "Valid Description"))
                .andExpect(model().attribute("renoRooms", "[Kitchen?]"))
                .andExpect(model().attribute("renovationRoomErrorMessage",
                        RenovationRecordService.RENO_ROOM_NAME_INVALID));
    }

    @Test
    void testAddRenovationWithDuplicateRenovationTitle () throws Exception {
        RenovationRecord existingRenovation = new RenovationRecord("Test", "Existing Description",
                new ArrayList<>(), "john@example.com");
        when(renovationRecordRepository.findRenovationRecordsByEmail("john@example.com")).thenReturn(List.of(existingRenovation));

        mockMvc.perform(post("/my-renovations/create-renovation")
                        .param("title", "Test")
                        .param("description", "Valid Description")
                        .param("rooms", "")
                        .param("prevPage", "")
                        .param("streetAddress", "")
                        .param("suburb","")
                        .param("city", "")
                        .param("postcode", "")
                        .param("country", "")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)).principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("renoName", "Test"))
                .andExpect(model().attribute("renoDescription", "Valid Description"))
                .andExpect(model().attribute("renoRooms", "[]"))
                .andExpect(model().attribute("renovationTitleErrorMessage",
                        RenovationRecordService.RENO_TITLE_NOT_UNIQUE));
    }

    @Test
    void testAddRenovationWithInvalidLocation_EmptyStreetAddress () throws Exception {
        mockMvc.perform(post("/my-renovations/create-renovation")
                        .param("title", "Test")
                        .param("description", "Valid Description")
                        .param("rooms", "")
                        .param("prevPage", "")
                        .param("streetAddress", "")
                        .param("suburb","suburb")
                        .param("city", "")
                        .param("postcode", "")
                        .param("country", "")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)).principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("renoName", "Test"))
                .andExpect(model().attribute("renoDescription", "Valid Description"))
                .andExpect(model().attribute("renoRooms", "[]"))
                .andExpect(model().attribute("addressErrorMessage", LocationService.STREET_ADDRESS_EMPTY))
                .andExpect(model().attributeDoesNotExist("suburbErrorMessage"))
                .andExpect(model().attributeDoesNotExist("cityErrorMessage"))
                .andExpect(model().attributeDoesNotExist("postcodeErrorMessage"))
                .andExpect(model().attributeDoesNotExist("countryErrorMessage"));
    }

    @Test
    void testAddRenovationWithInvalidLocation_InvalidStreetAddress () throws Exception {
        mockMvc.perform(post("/my-renovations/create-renovation")
                        .param("title", "Test")
                        .param("description", "Valid Description")
                        .param("rooms", "")
                        .param("prevPage", "")
                        .param("streetAddress", "###")
                        .param("suburb","")
                        .param("city", "City")
                        .param("postcode", "Postcode")
                        .param("country", "Country")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)).principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("renoName", "Test"))
                .andExpect(model().attribute("renoDescription", "Valid Description"))
                .andExpect(model().attribute("renoRooms", "[]"))
                .andExpect(model().attribute("addressErrorMessage", LocationService.STREET_ADDRESS_INVALID))
                .andExpect(model().attributeDoesNotExist("suburbErrorMessage"))
                .andExpect(model().attributeDoesNotExist("cityErrorMessage"))
                .andExpect(model().attributeDoesNotExist("postcodeErrorMessage"))
                .andExpect(model().attributeDoesNotExist("countryErrorMessage"));
    }

    @Test
    void testAddRenovationWithInvalidLocation_InvalidSuburb () throws Exception {
        mockMvc.perform(post("/my-renovations/create-renovation")
                        .param("title", "Test")
                        .param("description", "Valid Description")
                        .param("rooms", "")
                        .param("prevPage", "")
                        .param("streetAddress", "street")
                        .param("suburb","###")
                        .param("city", "City")
                        .param("postcode", "Postcode")
                        .param("country", "Country")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)).principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("renoName", "Test"))
                .andExpect(model().attribute("renoDescription", "Valid Description"))
                .andExpect(model().attribute("renoRooms", "[]"))
                .andExpect(model().attributeDoesNotExist("addressErrorMessage"))
                .andExpect(model().attribute("suburbErrorMessage", LocationService.SUBURB_INVALID))
                .andExpect(model().attributeDoesNotExist("cityErrorMessage"))
                .andExpect(model().attributeDoesNotExist("postcodeErrorMessage"))
                .andExpect(model().attributeDoesNotExist("countryErrorMessage"));
    }

    @Test
    void testAddRenovationWithInvalidLocation_InvalidCity () throws Exception {
        mockMvc.perform(post("/my-renovations/create-renovation")
                        .param("title", "Test")
                        .param("description", "Valid Description")
                        .param("rooms", "")
                        .param("prevPage", "")
                        .param("streetAddress", "street")
                        .param("suburb","Suburb")
                        .param("city", "###")
                        .param("postcode", "Postcode")
                        .param("country", "Country")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)).principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("renoName", "Test"))
                .andExpect(model().attribute("renoDescription", "Valid Description"))
                .andExpect(model().attribute("renoRooms", "[]"))
                .andExpect(model().attributeDoesNotExist("addressErrorMessage"))
                .andExpect(model().attributeDoesNotExist("suburbErrorMessage"))
                .andExpect(model().attribute("cityErrorMessage", LocationService.CITY_INVALID))
                .andExpect(model().attributeDoesNotExist("postcodeErrorMessage"))
                .andExpect(model().attributeDoesNotExist("countryErrorMessage"));
    }

    @Test
    void testAddRenovationWithInvalidLocation_InvalidPostCode () throws Exception {
        mockMvc.perform(post("/my-renovations/create-renovation")
                        .param("title", "Test")
                        .param("description", "Valid Description")
                        .param("rooms", "")
                        .param("prevPage", "")
                        .param("streetAddress", "street")
                        .param("suburb","")
                        .param("city", "City")
                        .param("postcode", "###")
                        .param("country", "Country")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)).principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("renoName", "Test"))
                .andExpect(model().attribute("renoDescription", "Valid Description"))
                .andExpect(model().attribute("renoRooms", "[]"))
                .andExpect(model().attributeDoesNotExist("addressErrorMessage"))
                .andExpect(model().attributeDoesNotExist("suburbErrorMessage"))
                .andExpect(model().attributeDoesNotExist("cityErrorMessage"))
                .andExpect(model().attribute("postcodeErrorMessage", LocationService.POSTCODE_INVALID))
                .andExpect(model().attributeDoesNotExist("countryErrorMessage"));
    }

    @Test
    void testAddRenovationWithInvalidLocation_InvalidCountry () throws Exception {
        mockMvc.perform(post("/my-renovations/create-renovation")
                        .param("title", "Test")
                        .param("description", "Valid Description")
                        .param("rooms", "")
                        .param("prevPage", "")
                        .param("streetAddress", "street")
                        .param("suburb","")
                        .param("city", "City")
                        .param("postcode", "Postcode")
                        .param("country", "###")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)).principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("renoName", "Test"))
                .andExpect(model().attribute("renoDescription", "Valid Description"))
                .andExpect(model().attribute("renoRooms", "[]"))
                .andExpect(model().attributeDoesNotExist("addressErrorMessage"))
                .andExpect(model().attributeDoesNotExist("suburbErrorMessage"))
                .andExpect(model().attributeDoesNotExist("cityErrorMessage"))
                .andExpect(model().attributeDoesNotExist("postcodeErrorMessage"))
                .andExpect(model().attribute("countryErrorMessage", LocationService.COUNTRY_INVALID));
    }

    @Test
    void testEditRenovationFormWithValidValues_NoRooms() throws Exception {
        RenovationRecord record = new RenovationRecord(
                "Record Name", "Record Description", List.of(), "john@example.com");
        record.setId(1L);
        Mockito.when(renovationRecordRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(record));

        mockMvc.perform(post("/my-renovations/edit-renovation")
                        .param("title", "New Record Name")
                        .param("oldName", record.getName())
                        .param("description", "New Record Description")
                        .param("rooms", "")
                        .param("prevPage", "")
                        .param("streetAddress", "")
                        .param("suburb","")
                        .param("city", "")
                        .param("postcode", "")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0))
                        .param("country", "")
                        .param("recordId", record.getId().toString())
                        .param("search", "")
                .principal(mockPrincipal));

        ArgumentCaptor<RenovationRecord> argument = ArgumentCaptor.forClass(RenovationRecord.class);
        Mockito.verify(renovationRecordRepository).save(argument.capture());
        RenovationRecord renovationRecord = argument.getValue();
        Assertions.assertEquals("New Record Name", renovationRecord.getName());
        Assertions.assertEquals("New Record Description", renovationRecord.getDescription());
        Assertions.assertEquals(0, renovationRecord.getRooms().size());
    }

    @Test
    void testEditRenovationFormWithValidLocationInfo() throws Exception {
        RenovationRecord record = new RenovationRecord(
                "Record Name", "Record Description", List.of(), "john@example.com");
        record.setId(1L);
        renovationRecordService.setRenovationRecordLocation(record, List.of("123 ABC Street", "ABC", "ABC", "123", "ABC"));
        Mockito.when(renovationRecordRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(record));

        mockMvc.perform(post("/my-renovations/edit-renovation")
                .param("title", record.getName())
                .param("oldName", record.getName())
                .param("description", record.getDescription())
                .param("rooms", "")
                .param("prevPage", "")
                .param("streetAddress", "456 XYZ Street")
                .param("suburb","XYZ")
                .param("city", "XYZ")
                .param("postcode", "456")
                .param("country", "XYZ")
                .param("latitude", String.valueOf(0))
                .param("longitude", String.valueOf(0))
                .param("recordId", record.getId().toString())
                .param("search", "")
                .principal(mockPrincipal));

        ArgumentCaptor<RenovationRecord> argument = ArgumentCaptor.forClass(RenovationRecord.class);
        Mockito.verify(renovationRecordRepository).save(argument.capture());
        RenovationRecord renovationRecord = argument.getValue();
        Assertions.assertEquals("456 XYZ Street", renovationRecord.getStreetAddress());
        Assertions.assertEquals("XYZ", renovationRecord.getSuburb());
        Assertions.assertEquals("XYZ", renovationRecord.getCity());
        Assertions.assertEquals("456", renovationRecord.getPostcode());
        Assertions.assertEquals("XYZ", renovationRecord.getCountry());
    }

    static Stream<Arguments> invalidStreetAddresses() {
        return Stream.of(
                Arguments.of("", LocationService.STREET_ADDRESS_EMPTY),
                Arguments.of("street#address", LocationService.STREET_ADDRESS_INVALID),
                Arguments.of("❤️", LocationService.STREET_ADDRESS_INVALID)
        );
    }
    @ParameterizedTest
    @MethodSource("invalidStreetAddresses")
    void testEditRenovationWithInvalidLocation_InvalidStreetAddress(String invalidStreetAddress, String expectedErrorMessage)
            throws Exception {
        RenovationRecord record = new RenovationRecord(
                "Record Name", "Record Description", List.of(), "john@example.com");
        record.setId(1L);
        renovationRecordService.setRenovationRecordLocation(record, List.of("123 ABC Street", "ABC", "ABC", "123", "ABC"));
        Mockito.when(renovationRecordRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(record));

        mockMvc.perform(post("/my-renovations/edit-renovation")
                        .param("title", record.getName())
                        .param("oldName", record.getName())
                        .param("description", record.getDescription())
                        .param("rooms", "")
                        .param("prevPage", "")
                        .param("streetAddress", invalidStreetAddress)
                        .param("suburb", invalidStreetAddress.isEmpty() ? "XYZ" : "")
                        .param("city", "City")
                        .param("postcode", "Postcode")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0))
                        .param("country", "Country")
                        .param("recordId", record.getId().toString())
                        .param("search", "")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("renoName", record.getName()))
                .andExpect(model().attribute("renoDescription", record.getDescription()))
                .andExpect(model().attribute("renoRooms", "[]"))
                .andExpect(model().attribute("addressErrorMessage", expectedErrorMessage))
                .andExpect(model().attributeDoesNotExist("suburbErrorMessage"))
                .andExpect(model().attributeDoesNotExist("cityErrorMessage"))
                .andExpect(model().attributeDoesNotExist("postcodeErrorMessage"))
                .andExpect(model().attributeDoesNotExist("countryErrorMessage"));
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
    void testEditRenovationWithInvalidLocation_InvalidSuburb(String invalidSuburb)
            throws Exception {
        RenovationRecord record = new RenovationRecord(
                "Record Name", "Record Description", List.of(), "john@example.com");
        record.setId(1L);
        renovationRecordService.setRenovationRecordLocation(record, List.of("123 ABC Street", "ABC", "ABC", "123", "ABC"));
        Mockito.when(renovationRecordRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(record));

        mockMvc.perform(post("/my-renovations/edit-renovation")
                        .param("title", record.getName())
                        .param("oldName", record.getName())
                        .param("description", record.getDescription())
                        .param("rooms", "")
                        .param("prevPage", "")
                        .param("streetAddress", "456 XYZ Street")
                        .param("suburb", invalidSuburb)
                        .param("city", "City")
                        .param("postcode", "Postcode")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0))
                        .param("country", "Country")
                        .param("recordId", record.getId().toString())
                        .param("search", "")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("renoName", record.getName()))
                .andExpect(model().attribute("renoDescription", record.getDescription()))
                .andExpect(model().attribute("renoRooms", "[]"))
                .andExpect(model().attributeDoesNotExist("addressErrorMessage"))
                .andExpect(model().attribute("suburbErrorMessage", LocationService.SUBURB_INVALID))
                .andExpect(model().attributeDoesNotExist("cityErrorMessage"))
                .andExpect(model().attributeDoesNotExist("postcodeErrorMessage"))
                .andExpect(model().attributeDoesNotExist("countryErrorMessage"));
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
    void testEditRenovationWithInvalidLocation_InvalidCity(String invalidCity)
            throws Exception {
        RenovationRecord record = new RenovationRecord(
                "Record Name", "Record Description", List.of(), "john@example.com");
        record.setId(1L);
        renovationRecordService.setRenovationRecordLocation(record, List.of("123 ABC Street", "ABC", "ABC", "123", "ABC"));
        Mockito.when(renovationRecordRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(record));

        mockMvc.perform(post("/my-renovations/edit-renovation")
                        .param("title", record.getName())
                        .param("oldName", record.getName())
                        .param("description", record.getDescription())
                        .param("rooms", "")
                        .param("prevPage", "")
                        .param("streetAddress", "456 XYZ Street")
                        .param("suburb", "XYZ")
                        .param("city", invalidCity)
                        .param("postcode", "Postcode")
                        .param("country", "Country")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0))
                        .param("recordId", record.getId().toString())
                        .param("search", "")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("renoName", record.getName()))
                .andExpect(model().attribute("renoDescription", record.getDescription()))
                .andExpect(model().attribute("renoRooms", "[]"))
                .andExpect(model().attributeDoesNotExist("addressErrorMessage"))
                .andExpect(model().attributeDoesNotExist("suburbErrorMessage"))
                .andExpect(model().attribute("cityErrorMessage", LocationService.CITY_INVALID))
                .andExpect(model().attributeDoesNotExist("postcodeErrorMessage"))
                .andExpect(model().attributeDoesNotExist("countryErrorMessage"));
    }

    static Stream<Arguments> invalidPostcodes() {
        return Stream.of(
                Arguments.of("post#code"),
                Arguments.of("post.code"),
                Arguments.of("post-code"),
                Arguments.of("post'code"),
                Arguments.of("p o s t c o d e"),
                Arguments.of("❤️")
        );
    }
    @ParameterizedTest
    @MethodSource("invalidPostcodes")
    void testEditRenovationWithInvalidLocation_InvalidPostcode(String invalidPostcode)
            throws Exception {
        RenovationRecord record = new RenovationRecord(
                "Record Name", "Record Description", List.of(), "john@example.com");
        record.setId(1L);
        renovationRecordService.setRenovationRecordLocation(record, List.of("123 ABC Street", "ABC", "ABC", "123", "ABC"));
        Mockito.when(renovationRecordRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(record));

        mockMvc.perform(post("/my-renovations/edit-renovation")
                        .param("title", record.getName())
                        .param("oldName", record.getName())
                        .param("description", record.getDescription())
                        .param("rooms", "")
                        .param("prevPage", "")
                        .param("streetAddress", "456 XYZ Street")
                        .param("suburb", "XYZ")
                        .param("city", "XYZ")
                        .param("postcode", invalidPostcode)
                        .param("country", "Country")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0))
                        .param("recordId", record.getId().toString())
                        .param("search", "")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("renoName", record.getName()))
                .andExpect(model().attribute("renoDescription", record.getDescription()))
                .andExpect(model().attribute("renoRooms", "[]"))
                .andExpect(model().attributeDoesNotExist("addressErrorMessage"))
                .andExpect(model().attributeDoesNotExist("suburbErrorMessage"))
                .andExpect(model().attributeDoesNotExist("cityErrorMessage"))
                .andExpect(model().attribute("postcodeErrorMessage", LocationService.POSTCODE_INVALID))
                .andExpect(model().attributeDoesNotExist("countryErrorMessage"));
    }

    static Stream<Arguments> invalidCountries() {
        return Stream.of(
                Arguments.of("country#"),
                Arguments.of("country."),
                Arguments.of("c o u n t r y"),
                Arguments.of("country123"),
                Arguments.of("❤️")
        );
    }
    @ParameterizedTest
    @MethodSource("invalidCountries")
    void testEditRenovationWithInvalidLocation_InvalidCountry(String invalidCountry)
            throws Exception {
        RenovationRecord record = new RenovationRecord(
                "Record Name", "Record Description", List.of(), "john@example.com");
        record.setId(1L);
        renovationRecordService.setRenovationRecordLocation(record, List.of("123 ABC Street", "ABC", "ABC", "123", "ABC"));
        Mockito.when(renovationRecordRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(record));

        mockMvc.perform(post("/my-renovations/edit-renovation")
                        .param("title", record.getName())
                        .param("oldName", record.getName())
                        .param("description", record.getDescription())
                        .param("rooms", "")
                        .param("prevPage", "")
                        .param("streetAddress", "456 XYZ Street")
                        .param("suburb", "XYZ")
                        .param("city", "XYZ")
                        .param("postcode", "456")
                        .param("country", invalidCountry)
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0))
                        .param("recordId", record.getId().toString())
                        .param("search", "")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("renoName", record.getName()))
                .andExpect(model().attribute("renoDescription", record.getDescription()))
                .andExpect(model().attribute("renoRooms", "[]"))
                .andExpect(model().attributeDoesNotExist("addressErrorMessage"))
                .andExpect(model().attributeDoesNotExist("suburbErrorMessage"))
                .andExpect(model().attributeDoesNotExist("cityErrorMessage"))
                .andExpect(model().attributeDoesNotExist("postcodeErrorMessage"))
                .andExpect(model().attribute("countryErrorMessage", LocationService.COUNTRY_INVALID));
    }
}
