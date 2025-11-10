package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.entity.dto.TradieRating;
import nz.ac.canterbury.seng302.homehelper.service.ValidationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockMultipartFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

public class ValidationServiceTest {

    private final ValidationService validationService = new ValidationService();
    private final User user = new User("firstName", "lastName", "email@example.com", "P4$$word", null, null);

    @Test
    public void checkPasswordTest() {
        String password = "password";
        boolean result = validationService.checkPassword(password, user);
        Assertions.assertFalse(result);
        String shortPassword = "pass";
        result = validationService.checkPassword(shortPassword, user);
        Assertions.assertFalse(result);
        String capitalizedPassword = "PASSWORD1!";
        result = validationService.checkPassword(capitalizedPassword, user);
        Assertions.assertFalse(result);
        String lowerCasePassword = "password1!";
        Assertions.assertFalse(validationService.checkPassword(lowerCasePassword, user));
        String strongPassword = "Password2!";
        result = validationService.checkPassword(strongPassword, user);
        Assertions.assertTrue(result);
    }

    @Test
    public void passwordMatchTest() {
        String password = "password";
        String password2 = "password2";
        String password3 = "password";
        Assertions.assertFalse(validationService.passwordMatch(password, password2));
        Assertions.assertTrue(validationService.passwordMatch(password, password3));
    }

    @Test
    public void checkNameTest() {
        String name = "name";
        String name2 = "na-me";
        String name3 = "na'me";
        String name4 = "na me";
        String name5 = "na_me";
        Assertions.assertTrue(validationService.checkName(name));
        Assertions.assertTrue(validationService.checkName(name2));
        Assertions.assertTrue(validationService.checkName(name3));
        Assertions.assertTrue(validationService.checkName(name4));
        Assertions.assertFalse(validationService.checkName(name5));
    }

    @Test
    public void checkNameLengthTest() {
        String name = "name";
        String longName = "namenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamenamename";
        Assertions.assertTrue(validationService.correctNameLength(name));
        Assertions.assertFalse(validationService.correctNameLength(longName));
    }

    @Test
    public void checkStringEmptyTest() {
        String name = "name";
        String string = "";
        Assertions.assertFalse(validationService.stringEmpty(name));
        Assertions.assertTrue(validationService.stringEmpty(string));
    }

    @Test
    public void checkEmailTest() {
        String email1 = "email1@example.com";
        String email2 = "email2@exam.ple.com";
        String email3 = "email3@exam-ple.com";
        String email4 = "email4@3xample.com";
        String email5 = "ema!il5@3xample.com";
        String email6 = "email6@exa!mple.com";
        String email7 = "email7@example";
        Assertions.assertTrue(validationService.checkEmailForm(email1));
        Assertions.assertTrue(validationService.checkEmailForm(email2));
        Assertions.assertTrue(validationService.checkEmailForm(email3));
        Assertions.assertTrue(validationService.checkEmailForm(email4));
        Assertions.assertTrue(validationService.checkEmailForm(email5));
        Assertions.assertFalse(validationService.checkEmailForm(email6));
        Assertions.assertFalse(validationService.checkEmailForm(email7));
    }

    @Test
    void checkNonAlphaNumericRenovationName() {
        String name = "Kitchen?";
        Assertions.assertTrue(validationService.containsNonAlphaNumeric(name));
    }

    @Test
    void checkAllowedNonAlphaRenovationName() {
        String name = "Kitchen,.-' ";
        Assertions.assertFalse(validationService.containsNonAlphaNumeric(name));
    }

    @Test
    void validateImage_ValidTypeJpg_ReturnsTrue() {
        MockMultipartFile file = new MockMultipartFile("submittedFile", "test.jpg", "image/jpeg", "test".getBytes());
        Assertions.assertTrue(validationService.checkImageType(file));
    }

    @Test
    void validateImage_ValidTypeSvg_ReturnsTrue() {
        MockMultipartFile file = new MockMultipartFile("submittedFile", "test.svg", "image/svg+xml", "test".getBytes());
        Assertions.assertTrue(validationService.checkImageType(file));
    }

    @Test
    void validateImage_ValidTypePng_ReturnsTrue() {
        MockMultipartFile file = new MockMultipartFile("submittedFile", "test.png", "image/png", "test".getBytes());
        Assertions.assertTrue(validationService.checkImageType(file));
    }

    @Test
    void correctLocationInfo_ValidLocationInfo_ReturnsTrue() {
        List<String> locationInfo = List.of("Street Address", "Suburb", "City", "Postcode", "Country");
        Assertions.assertTrue(validationService.correctLocationInfo(locationInfo));
    }

    @Test
    void correctLocationInfo_OnlyStreetAddress_ReturnsTrue() {
        List<String> locationInfo = Arrays.asList("Street Address", null, null, null, null);
        Assertions.assertTrue(validationService.correctLocationInfo(locationInfo));
    }

    @Test
    void correctLocationInfo_InvalidLocationInfo_ReturnsFalse() {
        List<String> locationInfo = Arrays.asList(null, null, "City", null, "Country");
        Assertions.assertFalse(validationService.correctLocationInfo(locationInfo));
    }

    // Location Checks

    @Test
    void checkStreetAddress_ValidStreetAddressContainsOnlyLetters_ReturnsTrue() {
        String streetAddress = "Street";
        Assertions.assertTrue(validationService.checkStreetAddress(streetAddress));
    }

    @Test
    void checkStreetAddress_ValidStreetAddressContainsMacron_ReturnsTrue() {
        String streetAddress = "Strāet";
        Assertions.assertTrue(validationService.checkStreetAddress(streetAddress));
    }

    @Test
    void checkStreetAddress_ValidStreetAddressContainsLettersAndDigits_ReturnsTrue() {
        String streetAddress = "Street Address 123";
        Assertions.assertTrue(validationService.checkStreetAddress(streetAddress));
    }

    @Test
    void checkStreetAddress_ValidStreetAddressContainsOnlyDigits_ReturnsTrue() {
        String streetAddress = "123";
        Assertions.assertTrue(validationService.checkStreetAddress(streetAddress));
    }

    @Test
    void checkStreetAddress_ValidStreetAddressContainsLettersAndDigitsAndHyphens_ReturnsTrue() {
        String streetAddress = "Street-Address-123";
        Assertions.assertTrue(validationService.checkStreetAddress(streetAddress));
    }

    @Test
    void checkStreetAddress_ValidStreetAddressContainsLettersAndApostrophe_ReturnsTrue() {
        String streetAddress = "Str'eet";
        Assertions.assertTrue(validationService.checkStreetAddress(streetAddress));
    }

    @Test
    void checkStreetAddress_ValidStreetAddressContainsLettersAndDot_ReturnsFalse() {
        String streetAddress = "Str.eet";
        Assertions.assertTrue(validationService.checkStreetAddress(streetAddress));
    }

    @Test
    void checkStreetAddress_InvalidStreetAddress_ReturnsFalse() {
        String streetAddress = "#Street-Address-123#";
        Assertions.assertFalse(validationService.checkStreetAddress(streetAddress));
    }

    @Test
    void checkSuburb_ValidSuburbOnlyLetters_ReturnsTrue() {
        String suburb = "Suburb";
        Assertions.assertTrue(validationService.checkSuburb(suburb));
    }

    @Test
    void checkSuburb_ValidSuburbContainsMacron_ReturnsTrue() {
        String suburb = "Suburbā";
        Assertions.assertTrue(validationService.checkSuburb(suburb));
    }

    @Test
    void checkSuburb_ValidSuburbOnlyNumbers_ReturnsTrue() {
        String suburb = "123";
        Assertions.assertTrue(validationService.checkSuburb(suburb));
    }

    @Test
    void checkSuburb_ValidSuburbOnlyLettersAndNumbers_ReturnsTrue() {
        String suburb = "Suburb 123";
        Assertions.assertTrue(validationService.checkSuburb(suburb));
    }

    @Test
    void checkSuburb_ValidSuburbLettersAndHyphen_ReturnsTrue() {
        String suburb = "Sub-urb";
        Assertions.assertTrue(validationService.checkSuburb(suburb));
    }

    @Test
    void checkSuburb_ValidSuburbLettersAndApostrophe_ReturnsTrue() {
        String suburb = "Sub'urb123";
        Assertions.assertTrue(validationService.checkSuburb(suburb));
    }

    @Test
    void checkSuburb_InvalidSuburbContainsDot_ReturnsFalse() {
        String suburb = "Sub.urb";
        Assertions.assertFalse(validationService.checkSuburb(suburb));
    }

    @Test
    void checkSuburb_InvalidSuburbContainsInvalidCharacters_ReturnsFalse() {
        String suburb = "$uburb";
        Assertions.assertFalse(validationService.checkSuburb(suburb));
    }

    @Test
    void checkCity_ValidCityOnlyLetters_ReturnsTrue() {
        String city = "City";
        Assertions.assertTrue(validationService.checkCity(city));
    }

    @Test
    void checkCity_ValidCityContainsMacron_ReturnsTrue() {
        String city = "Cityā";
        Assertions.assertTrue(validationService.checkCity(city));
    }

    @Test
    void checkCity_ValidCityLettersAndHyphen_ReturnsTrue() {
        String city = "Ci-ty";
        Assertions.assertTrue(validationService.checkCity(city));
    }

    @Test
    void checkCity_ValidCityLettersAndApostrophe_ReturnsTrue() {
        String city = "Ci'ty";
        Assertions.assertTrue(validationService.checkCity(city));
    }

    @Test
    void checkCity_validCitySpace_ReturnsTrue() {
        String city = "Ci ty";
        Assertions.assertTrue(validationService.checkCity(city));
    }

    @Test
    void checkCity_InvalidCityNumbers_ReturnsFalse() {
        String city = "City123";
        Assertions.assertFalse(validationService.checkCity(city));
    }

    @Test
    void checkCity_InvalidCityDot_ReturnsFalse() {
        String city = "Ci.ty";
        Assertions.assertFalse(validationService.checkCity(city));
    }

    @Test
    void checkCity_InvalidCityInvalidCharacter_ReturnsFalse() {
        String city = "City$";
        Assertions.assertFalse(validationService.checkCity(city));
    }

    @Test
    void checkPostCode_ValidPostCodeOnlyLetters_ReturnsTrue() {
        String postCode = "PostCode";
        Assertions.assertTrue(validationService.checkPostCode(postCode));
    }

    @Test
    void checkPostCode_ValidPostCodeContainsMacron_ReturnsTrue() {
        String postCode = "PostCodeā";
        Assertions.assertTrue(validationService.checkPostCode(postCode));
    }

    @Test
    void checkPostCode_ValidPostCodeOnlyNumbers_ReturnsTrue() {
        String postCode = "123";
        Assertions.assertTrue(validationService.checkPostCode(postCode));
    }

    @Test
    void checkPostCode_ValidPostCodeLettersAndNumbers_ReturnsTrue() {
        String postCode = "PostCode123";
        Assertions.assertTrue(validationService.checkPostCode(postCode));
    }

    @Test
    void checkPostCode_ValidPostCodeLettersNumbersAndSingleSpace_ReturnsTrue() {
        String postCode = "PostCode 123";
        Assertions.assertTrue(validationService.checkPostCode(postCode));
    }

    @Test
    void checkPostCode_InvalidPostCodeLettersNumbersAndMultipleSpace_ReturnsTrue() {
        String postCode = "Post Code 123";
        Assertions.assertFalse(validationService.checkPostCode(postCode));
    }

    @Test
    void checkPostCode_InvalidPostCodeLettersNumbersAndTab_ReturnsTrue() {
        String postCode = "Post    Code123";
        Assertions.assertFalse(validationService.checkPostCode(postCode));
    }

    @Test
    void checkPostCode_InvalidPostCodeLettersHyphen_ReturnsFalse() {
        String postCode = "post-code";
        Assertions.assertFalse(validationService.checkPostCode(postCode));
    }

    @Test
    void checkPostCode_InvalidPostCodeLettersDot_ReturnsFalse() {
        String postCode = "post.code";
        Assertions.assertFalse(validationService.checkPostCode(postCode));
    }

    @Test
    void checkPostCode_InvalidPostCodeLettersInvalidCharacter_ReturnsFalse() {
        String postCode = "post#code";
        Assertions.assertFalse(validationService.checkPostCode(postCode));
    }

    @Test
    void checkCountry_ValidCountryOnlyLetters_ReturnsTrue() {
        String country = "Country";
        Assertions.assertTrue(validationService.checkCountry(country));
    }

    @Test
    void checkCountry_ValidCountryContainsMacron_ReturnsTrue() {
        String country = "Counātry";
        Assertions.assertTrue(validationService.checkCountry(country));
    }

    @Test
    void checkCountry_ValidCountryLettersAndHyphen_ReturnsTrue() {
        String country = "Coun-try";
        Assertions.assertTrue(validationService.checkCountry(country));
    }

    @Test
    void checkCountry_ValidCountryLettersAndApostrophe_ReturnsTrue() {
        String country = "Coun'try";
        Assertions.assertTrue(validationService.checkCountry(country));
    }

    @Test
    void checkCountry_ValidCountryLettersAndSingleSpace_ReturnsTrue() {
        String country = "Coun try";
        Assertions.assertTrue(validationService.checkCountry(country));
    }

    @Test
    void checkCountry_InvalidCountryLettersAndMultipleSpace_ReturnsFalse() {
        String country = "Coun try f";
        Assertions.assertFalse(validationService.checkCountry(country));
    }

    @Test
    void checkCountry_InvalidCountryLettersAndTab_ReturnsFalse() {
        String country = "Coun     try";
        Assertions.assertFalse(validationService.checkCountry(country));
    }

    @Test
    void checkCountry_InvalidCountryLettersAndDoubleSpace_ReturnsFalse() {
        String country = "Coun  try";
        Assertions.assertFalse(validationService.checkCountry(country));
    }

    @Test
    void checkCountry_InvalidCountryContainsNumber_ReturnsFalse() {
        String country = "Country1";
        Assertions.assertFalse(validationService.checkCountry(country));
    }

    @Test
    void checkCountry_InvalidCountryContainsInvalidCharacter_ReturnsFalse() {
        String country = "Country#";
        Assertions.assertFalse(validationService.checkCountry(country));
    }

    // checkLocation Tests

    @Test
    void checkLocation_ValidLocation_NoErrors() {
        List<String> locations = Arrays.asList("Street", "Suburb", "City", "PostCode", "Country");
        Assertions.assertTrue(validationService.checkLocation(locations).isEmpty());
    }

    @Test
    void checkLocation_InvalidLocation_Address() {
        List<String> locations = Arrays.asList("###", "Suburb", "City", "PostCode", "Country");
        Assertions.assertEquals(List.of("ADDRESS"), validationService.checkLocation(locations));
    }

    @Test
    void checkLocation_InvalidLocation_Suburb() {
        List<String> locations = Arrays.asList("Street", "###", "City", "PostCode", "Country");
        Assertions.assertEquals(List.of("SUBURB"), validationService.checkLocation(locations));
    }

    @Test
    void checkLocation_InvalidLocation_City() {
        List<String> locations = Arrays.asList("Street", "Suburb", "###", "PostCode", "Country");
        Assertions.assertEquals(List.of("CITY"), validationService.checkLocation(locations));
    }

    @Test
    void checkLocation_InvalidLocation_PostCode() {
        List<String> locations = Arrays.asList("Street", "Suburb", "City", "###", "Country");
        Assertions.assertEquals(List.of("POSTCODE"), validationService.checkLocation(locations));
    }

    @Test
    void checkLocation_InvalidLocation_Country() {
        List<String> locations = Arrays.asList("Street", "Suburb", "City", "PostCode", "###");
        Assertions.assertEquals(List.of("COUNTRY"), validationService.checkLocation(locations));
    }

    @Test
    void checkLocation_InvalidLocation_StreetAndCountry() {
        List<String> locations = Arrays.asList("###", "Suburb", "City", "PostCode", "###");
        Assertions.assertEquals(List.of("ADDRESS", "COUNTRY"), validationService.checkLocation(locations));
    }

    @Test
    void checkTagFormat_TagContainsOnlyLetters_ReturnsNoErrors() {
        String tag = "Tag";
        Assertions.assertTrue(validationService.tagNameContainsLetters(tag));
    }

    @Test
    void checkTagFormat_TagContainsLettersAndNumbers_ReturnsNoErrors() {
        String tag = "Tag1";
        Assertions.assertTrue(validationService.tagNameContainsLetters(tag));
    }

    @Test
    void checkTagFormat_TagContainsLettersAndSymbols_ReturnsNoErrors() {
        String tag = "Tag!";
        Assertions.assertTrue(validationService.tagNameContainsLetters(tag));
    }

    @Test
    void checkTagFormat_TagContainsOnlyNumbers_ReturnsErrors() {
        String tag = "123";
        Assertions.assertFalse(validationService.tagNameContainsLetters(tag));
    }

    @Test
    void checkTagFormat_TagContainsOnlySymbols_ReturnsErrors() {
        String tag = "!$#";
        Assertions.assertFalse(validationService.tagNameContainsLetters(tag));
    }

    @Test
    void checkTagFormat_TagContainsNumbersAndSymbols_ReturnsErrors() {
        String tag = "!$#662";
        Assertions.assertFalse(validationService.tagNameContainsLetters(tag));
    }

    @Test
    void checkTagFormat_TagLength15_ReturnsNoErrors() {
        String tag = "TagTagTagTagTag";
        Assertions.assertTrue(validationService.correctTagNameLength(tag));
    }

    @Test
    void checkTagFormat_TagLengthGreater15_ReturnsErrors() {
        String tag = "TagTagTagTagTagT";
        Assertions.assertFalse(validationService.correctTagNameLength(tag));
    }

    @Test
    void checkTagFormat_TagLengthZero_ReturnsErrors() {
        String tag = "";
        Assertions.assertFalse(validationService.tagNameContainsLetters(tag));
    }

    @Test
    void checkRenoTagsAmount_ZeroTags_ReturnsTrue() {
        RenovationRecord record = new RenovationRecord("E", "e", null, "f.o@gmail.com");
        Assertions.assertTrue(validationService.checkTagsLessThanFive(record));
    }

    @Test
    void checkRenoTagsAmount_TwoTags_ReturnsTrue() {
        RenovationRecord record = new RenovationRecord("E", "e", null, "f.o@gmail.com");
        record.addTag(new Tag("a"));
        record.addTag(new Tag("b"));
        Assertions.assertTrue(validationService.checkTagsLessThanFive(record));
    }

    @Test
    void checkRenoTagsAmount_FiveTags_ReturnsFalse() {
        RenovationRecord record = new RenovationRecord("E", "e", null, "f.o@gmail.com");
        record.addTag(new Tag("a"));
        record.addTag(new Tag("b"));
        record.addTag(new Tag("c"));
        record.addTag(new Tag("d"));
        record.addTag(new Tag("e"));
        Assertions.assertFalse(validationService.checkTagsLessThanFive(record));
    }

    @Test
    void checkRenoTagsAmount_MoreThanFiveTags_ReturnsFalse() {
        RenovationRecord record = new RenovationRecord("E", "e", null, "f.o@gmail.com");
        record.addTag(new Tag("a"));
        record.addTag(new Tag("b"));
        record.addTag(new Tag("c"));
        record.addTag(new Tag("d"));
        record.addTag(new Tag("e"));
        record.addTag(new Tag("f"));
        record.addTag(new Tag("g"));
        Assertions.assertFalse(validationService.checkTagsLessThanFive(record));
    }

    @Test
    void checkRenoContainsTag_ContainTag_ReturnsTrue() {
        RenovationRecord record = new RenovationRecord("E", "e", null, "f.o@gmail.com");
        record.addTag(new Tag("a"));
        Assertions.assertTrue(validationService.checkRenovationContainsTag(record, "a"));
    }

    @Test
    void checkRenoContainsTag_DoesNotContainTag_ReturnsFalse() {
        RenovationRecord record = new RenovationRecord("E", "e", null, "f.o@gmail.com");
        record.addTag(new Tag("a"));
        Assertions.assertFalse(validationService.checkRenovationContainsTag(record, "b"));
    }

    @Test
    void checkRenoContainsTag_HasNoTags_ReturnsFalse() {
        RenovationRecord record = new RenovationRecord("E", "e", null, "f.o@gmail.com");
        Assertions.assertFalse(validationService.checkRenovationContainsTag(record, "b"));
    }

    @Test
    void correctExpenseCostFormat_ValidCost_ReturnsTrue() {
        Assertions.assertTrue(validationService.correctExpenseCostFormat("10"));
        Assertions.assertTrue(validationService.correctExpenseCostFormat("10.5"));
        Assertions.assertTrue(validationService.correctExpenseCostFormat(".5"));
    }

    @Test
    void correctExpenseCostFormat_InvalidCost_ReturnsFalse() {
        Assertions.assertFalse(validationService.correctExpenseCostFormat("1.1.1"));
        Assertions.assertFalse(validationService.correctExpenseCostFormat("10."));
        Assertions.assertFalse(validationService.correctExpenseCostFormat("0"));
        Assertions.assertFalse(validationService.correctExpenseCostFormat("0.0"));
        Assertions.assertFalse(validationService.correctExpenseCostFormat("000000.000000"));
    }

    @Test
    void correctExpenseCostLength_ValidLength_ReturnsTrue() {
        Assertions.assertTrue(validationService.correctExpenseCostLength("1"));
        Assertions.assertTrue(validationService.correctExpenseCostLength("123456789012345"));
        Assertions.assertTrue(validationService.correctExpenseCostLength("1234567890123.4"));
    }

    @Test
    void correctExpenseCostLength_InvalidLength_ReturnsFalse() {
        Assertions.assertFalse(validationService.correctExpenseCostLength("1234567890123456"));
        Assertions.assertFalse(validationService.correctExpenseCostLength("12345678901234.5"));
    }

    @Test
    void dateInThePast_PastDates_ReturnsTrue() throws ParseException {
        Assertions.assertTrue(validationService.dateInThePast("01/01/2025"));

        // Code for getting yesterday's date provided from Stack Overflow:
        // https://stackoverflow.com/questions/55394546/how-to-get-yesterday-instance-from-calendar/55394637
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);

        Assertions.assertTrue(validationService.dateInThePast(sdf.format(calendar.getTime())));
    }

    @Test
    void dateInThePast_FutureDates_ReturnsFalse() throws ParseException {
        Assertions.assertFalse(validationService.dateInThePast("01/01/3025"));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        String todayDateString = dateFormat.format(new Date());

        Assertions.assertFalse(validationService.dateInThePast(todayDateString));
    }

    @Test
    void dateInTheFuture_FutureDates_ReturnsTrue() throws ParseException {
        Assertions.assertTrue(validationService.dateInTheFuture("01/01/3025"));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, +1);

        Assertions.assertTrue(validationService.dateInTheFuture(sdf.format(calendar.getTime())));
    }

    @Test
    void dateInTheFuture_PastDates_ReturnsFalse() throws ParseException {
        Assertions.assertFalse(validationService.dateInTheFuture("01/01/2025"));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        String todayDateString = dateFormat.format(new Date());

        Assertions.assertFalse(validationService.dateInTheFuture(todayDateString));
    }

    static Stream<String> validPhoneNumbers() {
        return Stream.of(
            "1234567890",
            "(123) 456-7890",
            "123-456-7890",
            "123.456.7890",
            "+1 (123) 456-7890",
            "+11234567890",
            "11234567890",
            "+44 20 7946 0958",
            "+64 21 123 4567",
            "021 123 4567",
            "+919876543210"
        );
    }

    @ParameterizedTest
    @MethodSource("validPhoneNumbers")
    void checkValidPhoneNumber_validPhoneNumber_ReturnsTrue(String phoneNumber) {
        Assertions.assertTrue(validationService.checkValidPhoneNumber(phoneNumber));
    }

    public static Stream<String> invalidPhoneNumbers() {
        return Stream.of(
                "",
                "123",
                "123456789012345",
                "abcdefghij",
                "123-abc-7890",
                "123 456 78 90",
                "+1 (123) 456-78901",
                "++1 123 456 7890",
                "123_456_7890",
                "+1-800-FLOWERS",
                "٠١٢٣٤٥٦٧٨٩",
                "１２３４５６７８９０",
                "123–456–7890",
                "   "
        );
    }

    @ParameterizedTest
    @MethodSource("invalidPhoneNumbers")
    void checkInvalidPhoneNumber_invalidPhoneNumber_ReturnsFalse(String phoneNumber) {
        Assertions.assertFalse(validationService.checkValidPhoneNumber(phoneNumber));
    }

    public static Stream<String> validPositiveNumberStrings() {
        return Stream.of(
                "1",
                "123",
                "0.1",
                "3.14159",
                "99",
                "7.0",
                "0.000001",
                "999999999"
        );
    }

    @ParameterizedTest
    @MethodSource("validPositiveNumberStrings")
    void checkValidPositiveNumberString_validPositiveNumber_ReturnsTrue(String positiveNumberString) {
        Assertions.assertTrue(validationService.checkPositiveNumber(positiveNumberString));
    }

    public static Stream<String> invalidPositiveNumberStrings() {
        return Stream.of(
                "",
                "   ",
                "zero",
                "-1",
                "-0.0001",
                "-1e5",
                "abc",
                "no numbers here",
                "$-100",
                "--5",
                ".",
                "1-1",
                "e5",
                "NaN",
                "Infinity",
                "-Infinity",
                "0"
        );
    }

    @ParameterizedTest
    @MethodSource("invalidPositiveNumberStrings")
    void checkInvalidPositiveNumberString_invalidPositiveNumber_ReturnsFalse(String positiveNumberString) {
        Assertions.assertFalse(validationService.checkPositiveNumber(positiveNumberString));
    }

    @Test
    public void submitRatings_ShouldNotReturnZeroRatings() {
        User user = new User("Jane", "Doe", "jane@example.com", "Pass", null, null);
        user.setId(1L);
        List<TradieRating> tradieRatings = new ArrayList<>();
        TradieRating tradieRating1 = new TradieRating();
        tradieRating1.setRating(0);
        tradieRating1.setTradieId(1L);
        tradieRatings.add(tradieRating1);
        TradieRating tradieRating2 = new TradieRating();
        tradieRating2.setRating(4);
        tradieRating2.setTradieId(1L);
        tradieRatings.add(tradieRating2);
        TradieRating tradieRating3 = new TradieRating();
        tradieRating3.setRating(-1);
        tradieRating3.setTradieId(1L);
        tradieRatings.add(tradieRating3);
        TradieRating tradieRating4 = new TradieRating();
        tradieRating4.setRating(6);
        tradieRating4.setTradieId(1L);
        tradieRatings.add(tradieRating4);


        List<TradieRating> expectedTradieRatings = List.of(tradieRating2);
        List<TradieRating> resultTradieRatings = validationService.checkTradieRatings(tradieRatings);
        Assertions.assertEquals(expectedTradieRatings, resultTradieRatings);
    }

}
