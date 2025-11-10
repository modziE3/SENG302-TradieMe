package nz.ac.canterbury.seng302.homehelper.integration.service;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.service.ValidationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.scheduling.TaskScheduler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@SpringBootTest
public class UserServiceIntegrationTest {
    @MockBean
    private UserRepository userRepository;
    @SpyBean
    private ValidationService validationService;
    @SpyBean
    private TaskScheduler taskScheduler;
    @Autowired
    private UserService userService;

    static Stream<Arguments> validUserDetails() {
        return Stream.of(
                Arguments.of("John", "Doe", "john@example.com", "P4$$word", "P4$$word"),
                Arguments.of("John ", "Doe", "john@example.com", "P4$$word", "P4$$word"),
                Arguments.of("John -", "Doe", "john@example.com", "P4$$word", "P4$$word"),
                Arguments.of("John '", "Doe", "john@example.com", "P4$$word", "P4$$word"),
                Arguments.of("A", "Doe", "john@example.com", "P4$$word", "P4$$word"),
                Arguments.of("A".repeat(64), "Doe", "john@example.com", "P4$$word", "P4$$word"),
                Arguments.of("John", "Doe ", "john@example.com", "P4$$word", "P4$$word"),
                Arguments.of("John", "Doe -", "john@example.com", "P4$$word", "P4$$word"),
                Arguments.of("John", "Doe '", "john@example.com", "P4$$word", "P4$$word"),
                Arguments.of("John", "", "john@example.com", "P4$$word", "P4$$word"),
                Arguments.of("John", "A".repeat(64), "john@example.com", "P4$$word", "P4$$word"),
                Arguments.of("John", "Doe", "a@a.aa", "P4$$word", "P4$$word"),
                Arguments.of("John", "Doe", "john.doe@example.com", "P4$$word", "P4$$word"),
                Arguments.of("John", "Doe", "john@example.com", "Aaaaaa1!", "Aaaaaa1!")
        );
    }
    @ParameterizedTest
    @MethodSource("validUserDetails")
    public void validateUser_ValidUserWithPasswordAndEmailUniqueDetails_ExceptionNotThrown(
            String firstName, String lastName, String email, String password, String secondPassword) {

        Assertions.assertDoesNotThrow(() ->
                userService.validateUserWithPasswordAndEmailUnique(firstName, lastName, email, password, secondPassword,
                        List.of("Street Address", "Suburb", "City", "Postcode", "Country"), null, true));
    }

    static Stream<Arguments> invalidUserDetails() {
        return Stream.of(
                Arguments.of("", "Doe", "john@example.com", "P4$$word", "P4$$word"),
                Arguments.of("John1", "Doe", "john@example.com", "P4$$word", "P4$$word"),
                Arguments.of("John!", "Doe", "john@example.com", "P4$$word", "P4$$word"),
                Arguments.of("John@", "Doe", "john@example.com", "P4$$word", "P4$$word"),
                Arguments.of("A".repeat(65), "Doe", "john@example.com", "P4$$word", "P4$$word"),
                Arguments.of("John", "Doe1", "john@example.com", "P4$$word", "P4$$word"),
                Arguments.of("John", "Doe!", "john@example.com", "P4$$word", "P4$$word"),
                Arguments.of("John", "Doe@", "john@example.com", "P4$$word", "P4$$word"),
                Arguments.of("John", "A".repeat(65), "john@example.com", "P4$$word", "P4$$word"),
                Arguments.of("John", "Doe", "john@example.a", "P4$$word", "P4$$word"),
                Arguments.of("John", "Doe!", "a@a@.com", "P4$$word", "P4$$word"),
                Arguments.of("John", "Doe", "jane@example.com", "P4$$word", "P4$$word"),
                Arguments.of("John", "Doe", "john@example.com", "password", "password"),
                Arguments.of("John", "Doe", "john@example.com", "Password", "Password"),
                Arguments.of("John", "Doe", "john@example.com", "P4ssword", "P4ssword"),
                Arguments.of("John", "Doe", "john@example.com", "Pa$$word", "Pa$$word"),
                Arguments.of("John", "Doe", "john@example.com", "p4$$word", "p4$$word"),
                Arguments.of("John", "Doe", "john@example.com", "P4$$WORD", "P4$$WORD"),
                Arguments.of("John", "Doe", "john@example.com", "", ""),
                Arguments.of("John", "Doe", "john@example.com", "P4$$word", "password")
        );
    }
    @ParameterizedTest
    @MethodSource("invalidUserDetails")
    public void validateUser_invalidUserWithPasswordAndEmailUniqueDetails_ExceptionThrown(
            String firstName, String lastName, String email, String password, String secondPassword) {

        User duplicateUser = new User("Jane", "Doe", "jane@example.com", "P4$$word",
                null, null);
        duplicateUser.setId(1L);
        Mockito.when(userRepository.findByEmailContainingIgnoreCase("jane@example.com"))
                .thenReturn(duplicateUser);

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                userService.validateUserWithPasswordAndEmailUnique(firstName, lastName, email, password, secondPassword,
                        List.of("Street Address", "Suburb", "City", "Postcode", "Country"), null,true));
    }

    @Test
    public void validateLocation_validLocationInfo_ExceptionNotThrown() {
        Assertions.assertDoesNotThrow(() ->
                userService.validateLocation(Arrays.asList("Street Address", "Suburb", "City", "Postcode", "Country")));
    }

    @Test
    public void validateLocation_InvalidLocationInfo_ExceptionThrown() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                userService.validateLocation(Arrays.asList(null, "Suburb", "City", "Postcode", "Country")));
    }
}
