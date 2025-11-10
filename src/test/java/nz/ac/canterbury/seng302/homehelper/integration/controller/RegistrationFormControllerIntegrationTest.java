package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.controller.account.RegistrationFormController;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.service.ValidationService;
import org.springframework.security.core.Authentication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@SpringBootTest
@ActiveProfiles("test")
public class RegistrationFormControllerIntegrationTest {
    @Autowired
    private RegistrationFormController registrationFormController;

    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private TaskScheduler taskScheduler;

    @PostConstruct
    public void setup() {mockMvc = MockMvcBuilders.standaloneSetup(registrationFormController).build(); }

    @Test
    public void registrationFormTest() throws Exception {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.isAuthenticated()).thenReturn(true);

        Mockito.when(authenticationManager.authenticate(Mockito.any(Authentication.class))).thenReturn(auth);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(post("/registration-form")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "john@example.com")
                .param("password", "P4$$word")
                .param("secondPassword", "P4$$word")
                .param("streetAddress", "123 ABC Street")
                .param("suburb", "ABC")
                .param("city", "ABC")
                .param("postcode", "123")
                .param("country", "ABC")
                .param("latitude", String.valueOf(0))
                .param("longitude", String.valueOf(0)));

        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository).save(argument.capture());
        User user = argument.getValue();
        Assertions.assertEquals("John", user.getFirstName());
        Assertions.assertEquals("Doe", user.getLastName());
        Assertions.assertEquals("john@example.com", user.getEmail());
        Assertions.assertEquals("123 ABC Street", user.getStreetAddress());
        Assertions.assertEquals("ABC", user.getSuburb());
        Assertions.assertEquals("ABC", user.getCity());
        Assertions.assertEquals("123", user.getPostcode());
        Assertions.assertEquals("ABC", user.getCountry());
    }

    @Test
    public void registrationFormTestWithNoLocation() throws Exception {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.isAuthenticated()).thenReturn(true);

        Mockito.when(authenticationManager.authenticate(Mockito.any(Authentication.class))).thenReturn(auth);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(post("/registration-form")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "john@example.com")
                .param("password", "P4$$word")
                .param("secondPassword", "P4$$word")
                .param("streetAddress", "")
                .param("suburb", "")
                .param("city", "")
                .param("postcode", "")
                .param("country", "")
                .param("latitude", String.valueOf(0))
                .param("longitude", String.valueOf(0)));

        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository).save(argument.capture());
        User user = argument.getValue();
        Assertions.assertEquals("John", user.getFirstName());
        Assertions.assertEquals("Doe", user.getLastName());
        Assertions.assertEquals("john@example.com", user.getEmail());
        Assertions.assertNull(user.getStreetAddress());
        Assertions.assertNull(user.getSuburb());
        Assertions.assertNull(user.getCity());
        Assertions.assertNull(user.getPostcode());
        Assertions.assertNull(user.getCountry());
    }

    @Test
    public void registrationFormTestWithNoStreetAddress() throws Exception {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.isAuthenticated()).thenReturn(true);

        Mockito.when(authenticationManager.authenticate(Mockito.any(Authentication.class))).thenReturn(auth);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(post("/registration-form")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "john@example.com")
                .param("password", "P4$$word")
                .param("secondPassword", "P4$$word")
                .param("streetAddress", "")
                .param("suburb", "sub")
                .param("city", "city")
                .param("postcode", "post")
                .param("country", "country")
                .param("latitude", String.valueOf(0))
                .param("longitude", String.valueOf(0)))
                .andExpect(model().attribute("addressErrorMessage", LocationService.STREET_ADDRESS_EMPTY));

        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, Mockito.never()).save(argument.capture());
    }

    @Test
    public void registrationFormTestWithOnlyStreetAddress() throws Exception {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.isAuthenticated()).thenReturn(true);

        Mockito.when(authenticationManager.authenticate(Mockito.any(Authentication.class))).thenReturn(auth);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(post("/registration-form")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "john@example.com")
                .param("password", "P4$$word")
                .param("secondPassword", "P4$$word")
                .param("streetAddress", "street")
                .param("suburb", "")
                .param("city", "postcode")
                .param("postcode", "postcode")
                .param("country", "postcode")
                .param("latitude", String.valueOf(0))
                .param("longitude", String.valueOf(0)));

        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository).save(argument.capture());
        User user = argument.getValue();

        Assertions.assertEquals("John", user.getFirstName());
        Assertions.assertEquals("Doe", user.getLastName());
        Assertions.assertEquals("john@example.com", user.getEmail());
        Assertions.assertEquals("street", user.getStreetAddress());
        Assertions.assertNull(user.getSuburb());
        Assertions.assertEquals("postcode", user.getCity());
        Assertions.assertEquals("postcode", user.getPostcode());
        Assertions.assertEquals("postcode", user.getCountry());
    }

    @Test
    public void registrationFormTestWithStreetAddressAndCity() throws Exception {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.isAuthenticated()).thenReturn(true);

        Mockito.when(authenticationManager.authenticate(Mockito.any(Authentication.class))).thenReturn(auth);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(post("/registration-form")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "john@example.com")
                .param("password", "P4$$word")
                .param("secondPassword", "P4$$word")
                .param("streetAddress", "street")
                .param("suburb", "")
                .param("city", "city")
                .param("postcode", "postcode")
                .param("country", "country")
                .param("latitude", String.valueOf(0))
                .param("longitude", String.valueOf(0)));

        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository).save(argument.capture());
        User user = argument.getValue();

        Assertions.assertEquals("John", user.getFirstName());
        Assertions.assertEquals("Doe", user.getLastName());
        Assertions.assertEquals("john@example.com", user.getEmail());
        Assertions.assertEquals("street", user.getStreetAddress());
        Assertions.assertNull(user.getSuburb());
        Assertions.assertEquals("city", user.getCity());
        Assertions.assertEquals("postcode", user.getPostcode());
        Assertions.assertEquals("country", user.getCountry());
    }

    @Test
    public void registrationFormTestWithInvalidStreetAddress() throws Exception {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.isAuthenticated()).thenReturn(true);

        Mockito.when(authenticationManager.authenticate(Mockito.any(Authentication.class))).thenReturn(auth);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(post("/registration-form")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "john@example.com")
                .param("password", "P4$$word")
                .param("secondPassword", "P4$$word")
                .param("streetAddress", "###")
                .param("suburb", "")
                .param("city", "")
                .param("postcode", "")
                .param("country", "")
                .param("latitude", String.valueOf(0))
                .param("longitude", String.valueOf(0)))
                .andExpect(model().attribute("addressErrorMessage", LocationService.STREET_ADDRESS_INVALID));

        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, Mockito.never()).save(argument.capture());

    }

    @Test
    public void registrationFormTestWithAllInvalidLocation() throws Exception {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.isAuthenticated()).thenReturn(true);

        Mockito.when(authenticationManager.authenticate(Mockito.any(Authentication.class))).thenReturn(auth);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(post("/registration-form")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@example.com")
                        .param("password", "P4$$word")
                        .param("secondPassword", "P4$$word")
                        .param("streetAddress", "###")
                        .param("suburb", "###")
                        .param("city", "###")
                        .param("postcode", "###")
                        .param("country", "###")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)))
                .andExpect(model().attribute("addressErrorMessage", LocationService.STREET_ADDRESS_INVALID))
                .andExpect(model().attribute("suburbErrorMessage", LocationService.SUBURB_INVALID))
                .andExpect(model().attribute("cityErrorMessage", LocationService.CITY_INVALID))
                .andExpect(model().attribute("postcodeErrorMessage", LocationService.POSTCODE_INVALID))
                .andExpect(model().attribute("countryErrorMessage", LocationService.COUNTRY_INVALID));

        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, Mockito.never()).save(argument.capture());

    }

    @Test
    public void registrationFormTestWithNoStreetAddressAndInvalidCity() throws Exception {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.isAuthenticated()).thenReturn(true);

        Mockito.when(authenticationManager.authenticate(Mockito.any(Authentication.class))).thenReturn(auth);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(post("/registration-form")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@example.com")
                        .param("password", "P4$$word")
                        .param("secondPassword", "P4$$word")
                        .param("streetAddress", "")
                        .param("suburb", "")
                        .param("city", "###")
                        .param("postcode", "")
                        .param("country", "")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)))
                .andExpect(model().attribute("addressErrorMessage", LocationService.STREET_ADDRESS_EMPTY))
                .andExpect(model().attributeDoesNotExist("suburbErrorMessage"))
                .andExpect(model().attribute("cityErrorMessage", LocationService.CITY_INVALID))
                .andExpect(model().attributeDoesNotExist("postcodeErrorMessage"))
                .andExpect(model().attributeDoesNotExist("countryErrorMessage"));

        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, Mockito.never()).save(argument.capture());

    }

    @Test
    public void registrationFormTestWithInvalidSuburb() throws Exception {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.isAuthenticated()).thenReturn(true);

        Mockito.when(authenticationManager.authenticate(Mockito.any(Authentication.class))).thenReturn(auth);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(post("/registration-form")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@example.com")
                        .param("password", "P4$$word")
                        .param("secondPassword", "P4$$word")
                        .param("streetAddress", "street")
                        .param("suburb", "###")
                        .param("city", "city")
                        .param("postcode", "postcode")
                        .param("country", "country")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)))
                .andExpect(model().attributeDoesNotExist("addressErrorMessage"))
                .andExpect(model().attribute("suburbErrorMessage", LocationService.SUBURB_INVALID))
                .andExpect(model().attributeDoesNotExist("cityErrorMessage"))
                .andExpect(model().attributeDoesNotExist("postcodeErrorMessage"))
                .andExpect(model().attributeDoesNotExist("countryErrorMessage"));

        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, Mockito.never()).save(argument.capture());

    }

    @Test
    public void registrationFormTestWithInvalidPostCode() throws Exception {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.isAuthenticated()).thenReturn(true);

        Mockito.when(authenticationManager.authenticate(Mockito.any(Authentication.class))).thenReturn(auth);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(post("/registration-form")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@example.com")
                        .param("password", "P4$$word")
                        .param("secondPassword", "P4$$word")
                        .param("streetAddress", "street")
                        .param("suburb", "")
                        .param("city", "city")
                        .param("postcode", "###")
                        .param("country", "Country")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)))
                .andExpect(model().attributeDoesNotExist("addressErrorMessage"))
                .andExpect(model().attributeDoesNotExist("suburbErrorMessage"))
                .andExpect(model().attributeDoesNotExist("cityErrorMessage"))
                .andExpect(model().attribute("postcodeErrorMessage", LocationService.POSTCODE_INVALID))
                .andExpect(model().attributeDoesNotExist("countryErrorMessage"));

        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, Mockito.never()).save(argument.capture());

    }

    @Test
    public void registrationFormTestWithInvalidCountry() throws Exception {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.isAuthenticated()).thenReturn(true);

        Mockito.when(authenticationManager.authenticate(Mockito.any(Authentication.class))).thenReturn(auth);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(post("/registration-form")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@example.com")
                        .param("password", "P4$$word")
                        .param("secondPassword", "P4$$word")
                        .param("streetAddress", "street")
                        .param("suburb", "")
                        .param("city", "city")
                        .param("postcode", "city")
                        .param("country", "###")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)))
                .andExpect(model().attributeDoesNotExist("addressErrorMessage"))
                .andExpect(model().attributeDoesNotExist("suburbErrorMessage"))
                .andExpect(model().attributeDoesNotExist("cityErrorMessage"))
                .andExpect(model().attributeDoesNotExist("postcodeErrorMessage"))
                .andExpect(model().attribute("countryErrorMessage", LocationService.COUNTRY_INVALID));

        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, Mockito.never()).save(argument.capture());

    }

}
