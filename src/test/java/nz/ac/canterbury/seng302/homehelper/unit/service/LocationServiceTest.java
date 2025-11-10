package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.service.ValidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocationServiceTest {

    private final ValidationService validationService = new ValidationService();
    private final LocationService locationService = new LocationService(validationService);

    @Test
    public void validLocation_NoErrors() {
        List<String> locations = Arrays.asList("Street", "Suburb", "City", "PostCode", "Country");
        assertEquals("", locationService.locationErrors(locations));
    }

    @Test
    public void InvalidLocationOnlyStreetAddress_InvalidCityPostcodeCountry() {
        List<String> locations = Arrays.asList("Street", null, null, null, null);
        assertEquals("CITY_EMPTY POSTCODE_EMPTY COUNTRY_EMPTY", locationService.locationErrors(locations));
    }

    @Test
    public void invalidLocationNoStreetAddress_InvalidLocation() {
        List<String> locations = Arrays.asList(null, "Suburb", "City", "PostCode", "Country");
        assertEquals("INVALID-LOCATION", locationService.locationErrors(locations));
    }

    @Test
    public void invalidLocationInvalidSuburb_Suburb() {
        List<String> locations = Arrays.asList("Street", "###", "City", "PostCode", "Country");
        assertEquals("SUBURB", locationService.locationErrors(locations));
    }

    @Test
    public void invalidLocationInvalidCity_City() {
        List<String> locations = Arrays.asList("Street", "Suburb", "###", "PostCode", "Country");
        assertEquals("CITY", locationService.locationErrors(locations));
    }

    @Test
    public void invalidLocationInvalidPostCode_PostCode() {
        List<String> locations = Arrays.asList("Street", "Suburb", "City", "###", "Country");
        assertEquals("POSTCODE", locationService.locationErrors(locations));
    }

    @Test
    public void invalidLocationInvalidStreetAddress_Address() {
        List<String> locations = Arrays.asList("###", "Suburb", "City", "PostCode", "Country");
        assertEquals("ADDRESS", locationService.locationErrors(locations));
    }

    @Test
    public void invalidLocationInvalidCountry_Country() {
        List<String> locations = Arrays.asList("Street", "Suburb", "City", "PostCode", "###");
        assertEquals("COUNTRY", locationService.locationErrors(locations));
    }

    @Test
    public void InvalidLocationNoCityCountry() {
        List<String> locations = Arrays.asList("Street", "Suburb", null, "PostCode", null);
        assertEquals("CITY_EMPTY COUNTRY_EMPTY", locationService.locationErrors(locations));
    }

    @Test
    public void InvalidLocationInvalidCityCountry_CityCountry() {
        List<String> locations = Arrays.asList("Street", "Suburb", "###", "PostCode", "###");
        assertEquals("CITY COUNTRY", locationService.locationErrors(locations));
    }

    @Test
    public void invalidLocationNoStreetAddressInvalidCity_InvalidLocationCity() {
        List<String> locations = Arrays.asList(null, "Suburb", "###", "PostCode", "Country");
        assertEquals("INVALID-LOCATION CITY", locationService.locationErrors(locations));
    }

    @Test
    public void InvalidLocationOnlyStreetAddressInvalidStreetAddress_InvalidAddressCityPostcodeCountry() {
        List<String> locations = Arrays.asList("###", null, null, null, null);
        assertEquals("ADDRESS CITY_EMPTY POSTCODE_EMPTY COUNTRY_EMPTY", locationService.locationErrors(locations));
    }

    static Stream<Arguments> streetAddressErrorMessages() {
        return Stream.of(
                Arguments.of(LocationService.STREET_ADDRESS_EMPTY, LocationService.STREET_ADDRESS_EMPTY),
                Arguments.of(LocationService.STREET_ADDRESS_EMPTY + LocationService.STREET_ADDRESS_INVALID,
                        LocationService.STREET_ADDRESS_INVALID)
        );
    }
    @ParameterizedTest
    @MethodSource("streetAddressErrorMessages")
    public void getErrorMessages_streetAddressErrorMessageStringProvided_CorrectSeparateErrorMessageReturned(
            String allErrorMessages, String streetAddressErrorMessage) {
        List<String> errorMessages = locationService.getErrorMessages(allErrorMessages);
        assertEquals(streetAddressErrorMessage, errorMessages.get(0));
    }

    static Stream<Arguments> suburbErrorMessages() {
        return Stream.of(
                Arguments.of(LocationService.SUBURB_INVALID, LocationService.SUBURB_INVALID)
        );
    }
    @ParameterizedTest
    @MethodSource("suburbErrorMessages")
    public void getErrorMessages_suburbErrorMessageStringProvided_CorrectSeparateErrorMessageReturned(
            String allErrorMessages, String suburbAddressErrorMessage) {
        List<String> errorMessages = locationService.getErrorMessages(allErrorMessages);
        assertEquals(suburbAddressErrorMessage, errorMessages.get(1));
    }

    static Stream<Arguments> cityErrorMessages() {
        return Stream.of(
                Arguments.of(LocationService.CITY_EMPTY, LocationService.CITY_EMPTY),
                Arguments.of(LocationService.CITY_EMPTY + LocationService.CITY_INVALID,
                        LocationService.CITY_INVALID)
        );
    }
    @ParameterizedTest
    @MethodSource("cityErrorMessages")
    public void getErrorMessages_cityErrorMessageStringProvided_CorrectSeparateErrorMessageReturned(
            String allErrorMessages, String cityAddressErrorMessage) {
        List<String> errorMessages = locationService.getErrorMessages(allErrorMessages);
        assertEquals(cityAddressErrorMessage, errorMessages.get(2));
    }

    static Stream<Arguments> postcodeErrorMessages() {
        return Stream.of(
                Arguments.of(LocationService.POSTCODE_EMPTY, LocationService.POSTCODE_EMPTY),
                Arguments.of(LocationService.POSTCODE_EMPTY + LocationService.POSTCODE_INVALID,
                        LocationService.POSTCODE_INVALID)
        );
    }
    @ParameterizedTest
    @MethodSource("postcodeErrorMessages")
    public void getErrorMessages_postcodeErrorMessageStringProvided_CorrectSeparateErrorMessageReturned(
            String allErrorMessages, String postcodeAddressErrorMessage) {
        List<String> errorMessages = locationService.getErrorMessages(allErrorMessages);
        assertEquals(postcodeAddressErrorMessage, errorMessages.get(3));
    }

    static Stream<Arguments> countryErrorMessages() {
        return Stream.of(
                Arguments.of(LocationService.COUNTRY_EMPTY, LocationService.COUNTRY_EMPTY),
                Arguments.of(LocationService.COUNTRY_EMPTY + LocationService.COUNTRY_INVALID,
                        LocationService.COUNTRY_INVALID)
        );
    }
    @ParameterizedTest
    @MethodSource("countryErrorMessages")
    public void getErrorMessages_countryErrorMessageStringProvided_CorrectSeparateErrorMessageReturned(
            String allErrorMessages, String countryAddressErrorMessage) {
        List<String> errorMessages = locationService.getErrorMessages(allErrorMessages);
        assertEquals(countryAddressErrorMessage, errorMessages.get(4));
    }
}
