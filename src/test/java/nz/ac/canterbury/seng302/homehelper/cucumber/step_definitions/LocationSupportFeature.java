package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.homehelper.controller.account.EditProfileController;
import nz.ac.canterbury.seng302.homehelper.controller.account.RegistrationFormController;
import nz.ac.canterbury.seng302.homehelper.controller.renovations.RenovationFormController;
import nz.ac.canterbury.seng302.homehelper.entity.RecentRenovation;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.*;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@SpringBootTest
public class LocationSupportFeature {
    private static String viewName;
    private static Model model;
    private static MockMvc mockMvc;
    private static UserRepository userRepository;
    private static UserService userService;
    private static ValidationService validationService;
    private static EmailService emailService;
    private static TaskScheduler taskScheduler;
    private static RenovationRecordService renovationRecordService;
    private static ImageService imageService;
    private static Principal principal;
    private static HttpServletRequest request;
    private static User user;
    private static RenovationRecord renovationRecord;
    private static LocationService locationService;
    private static PasswordEncoder passwordEncoder;
    private static AuthenticationManager authenticationManager;
    private static LocationQueryService locationQueryService;
    private static HttpSession session;

    private static ArgumentCaptor<User> userCaptor;
    private static RecentRenovationRepository recentRenovationRepository;
    private static RenovationRecordRepository renovationRecordRepository;
    private static QuoteRepository quoteRepository;

    @BeforeAll
    public static void beforeAll() {
        model = Mockito.mock(Model.class);
        userRepository = Mockito.mock(UserRepository.class);
        validationService = new ValidationService();
        taskScheduler = Mockito.mock(TaskScheduler.class);
        locationService = new LocationService(validationService);
        userService = new UserService(userRepository, validationService, taskScheduler, locationService, renovationRecordService, renovationRecordRepository,recentRenovationRepository, quoteRepository );
        emailService = Mockito.mock(EmailService.class);
        taskScheduler = Mockito.mock(TaskScheduler.class);
        renovationRecordService = Mockito.mock(RenovationRecordService.class);
        imageService = Mockito.mock(ImageService.class);
        principal = Mockito.mock(Principal.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        authenticationManager = Mockito.mock(AuthenticationManager.class);
        locationQueryService = Mockito.mock(LocationQueryService.class);
        session = Mockito.mock(HttpSession.class);
        request = Mockito.mock(HttpServletRequest.class);
    }

    @Given("I register to the system on the registration page")
    public void i_register_to_the_system_on_the_registration_page() {
        RegistrationFormController registrationFormController = new RegistrationFormController(userService, emailService,
                taskScheduler, locationService, passwordEncoder, authenticationManager, locationQueryService);
        model = Mockito.mock(Model.class);
        viewName = registrationFormController.registration(model, request, Mockito.mock(HttpSession.class));
        mockMvc = MockMvcBuilders.standaloneSetup(registrationFormController).build();
        Assertions.assertEquals("registrationFormTemplate", viewName);
    }

    // AC2
    @Given("I edit my profile on the edit profile page")
    public void i_edit_my_profile_on_the_edit_profile_page() {
        user = new User("John", "Doe", "john@example.com", "P4$$word",
                null, null);
        user.setId(1L);
        user.setStreetAddress("");
        user.setSuburb("");
        user.setCity("");
        user.setPostcode("");
        user.setCountry("");

        Mockito.when(principal.getName()).thenReturn("john@example.com");
        Mockito.when(userService.getUser(Mockito.anyString())).thenReturn(user);

        EditProfileController editProfileController = new EditProfileController(userService, renovationRecordService, imageService, locationService, locationQueryService, userRepository);
        model = Mockito.mock(Model.class);
        viewName = editProfileController.editProfile(model, principal, request, Mockito.mock(HttpSession.class));
        mockMvc = MockMvcBuilders.standaloneSetup(editProfileController).build();

        Assertions.assertEquals("editProfileTemplate", viewName);
    }

    // AC3
    @Given("I create a renovation record on the add renovation record page")
    public void i_create_a_renovation_record_on_the_add_renovation_record_page() {
        RenovationFormController renovationFormController = new RenovationFormController(renovationRecordService, locationService, locationQueryService, renovationRecordRepository);
        model = Mockito.mock(Model.class);
        request = Mockito.mock(HttpServletRequest.class);
        viewName = renovationFormController.getCreateRenovationRecordForm("", model, request, Mockito.mock(HttpSession.class));
        mockMvc = MockMvcBuilders.standaloneSetup(renovationFormController).build();
        Assertions.assertEquals("renovationFormTemplate", viewName);
    }

    // AC4
    @Given("I edit a renovation record on the edit renovation record page")
    public void i_edit_a_renovation_record_on_the_edit_renovation_record_page() {
        renovationRecord = new RenovationRecord("Title", "Description", List.of(), "john@example.com");
        renovationRecord.setId(1L);
        renovationRecord.setStreetAddress("");
        renovationRecord.setSuburb("");
        renovationRecord.setCity("");
        renovationRecord.setPostcode("");
        renovationRecord.setCountry("");

        Mockito.when(principal.getName()).thenReturn("john@example.com");
        Mockito.when(renovationRecordService.getAndAuthenticateRecord(Mockito.anyLong(), Mockito.anyString()))
                .thenReturn(renovationRecord);

        RenovationFormController renovationFormController = new RenovationFormController(renovationRecordService, locationService, locationQueryService, renovationRecordRepository);
        model = Mockito.mock(Model.class);
        request = Mockito.mock(HttpServletRequest.class);
        viewName = renovationFormController.getEditRenovationRecordForm(renovationRecord.getId(),
                "false-", principal, model, request, Mockito.mock(HttpSession.class));
        mockMvc = MockMvcBuilders.standaloneSetup(renovationFormController).build();

        Assertions.assertEquals("renovationFormTemplate", viewName);
    }

    // AC11
    @Given("I supply a fully compliant address")
    public void i_supply_a_fully_compliant_address() {
        Mockito.when(userRepository.findByEmailContainingIgnoreCase(Mockito.anyString())).thenReturn(null);
        List<String> locationInfo = List.of("123 ABC Street", "ABC", "ABC", "123", "ABC");
        Assertions.assertDoesNotThrow(() ->
                userService.validateUserWithPasswordAndEmailUnique(
                        "John", "Doe", "john@example.com", "P4$$word",
                        "P4$$word", locationInfo, null, true));
    }

    // AC1
    @When("I am asked to supply my details on the registration page")
    public void i_am_asked_to_supply_my_details_on_the_registration_page() throws Exception {
        mockMvc.perform(get("/registration-form"))
                .andExpect(model().attribute("firstName", ""))
                .andExpect(model().attribute("lastName", ""))
                .andExpect(model().attribute("email", ""))
                .andExpect(model().attribute("password", ""))
                .andExpect(model().attribute("secondPassword", ""));
    }

    // AC2
    @When("I am asked to supply my location details on the edit profile page")
    public void i_am_asked_to_supply_my_location_details_on_the_edit_profile_page() throws Exception {
        mockMvc.perform(get("/edit-profile")
                        .principal(principal))
                .andExpect(model().attribute("firstName", user.getFirstName()))
                .andExpect(model().attribute("lastName", user.getLastName()))
                .andExpect(model().attribute("email", user.getEmail()));
    }

    // AC3
    @When("I am asked to supply the renovation details on the add renovation record page")
    public void i_am_asked_to_supply_the_renovation_details_on_the_add_renovation_record_page() throws Exception {
        mockMvc.perform(get("/my-renovations/create-renovation"))
                .andExpect(model().attribute("renoName", ""))
                .andExpect(model().attribute("renoDescription", ""))
                .andExpect(model().attribute("descLen", "0/512"));
    }

    // AC4
    @When("I am asked to supply the renovation details on the edit renovation record page")
    public void i_am_asked_to_supply_the_renovation_details_on_the_edit_renovation_record_page() throws Exception {
        mockMvc.perform(get("/my-renovations/edit-renovation")
                        .principal(principal)
                        .param("recordId", renovationRecord.getId().toString())
                        .param("search", "false-"))
                .andExpect(model().attribute("renoName", renovationRecord.getName()))
                .andExpect(model().attribute("renoDescription", renovationRecord.getDescription()))
                .andExpect(model().attribute("descLen", renovationRecord.getDescription().length()+"/512"))
                .andExpect(model().attribute("renoRooms",
                        "["+String.join("`", renovationRecord.getRooms().stream().map(Room::getName).toList())+"]"));
    }

    // AC11
    @When("I submit the form")
    public void i_submit_the_form() throws Exception {
        Mockito.when(userRepository.findByEmailContainingIgnoreCase(Mockito.anyString())).thenReturn(null);

        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.isAuthenticated()).thenReturn(true);
        Mockito.when(authenticationManager.authenticate(Mockito.any(Authentication.class))).thenReturn(auth);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        RegistrationFormController registrationFormController = new RegistrationFormController(userService, emailService,
                taskScheduler, locationService, passwordEncoder, authenticationManager, locationQueryService);
        viewName = registrationFormController.registration(model, request, Mockito.mock(HttpSession.class));
        mockMvc = MockMvcBuilders.standaloneSetup(registrationFormController).build();
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
                .param("country", "ABC"));

        userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository).save(userCaptor.capture());
    }

    // AC1
    @Then("I can optionally supply my location on the registration page")
    public void i_can_optionally_supply_my_location_on_the_registration_page() throws Exception {
        mockMvc.perform(get("/registration-form"))
                .andExpect(model().attribute("streetAddress", ""))
                .andExpect(model().attribute("suburb", ""))
                .andExpect(model().attribute("city", ""))
                .andExpect(model().attribute("postcode", ""))
                .andExpect(model().attribute("country", ""));
    }

    // AC2
    @Then("I can optionally supply my location on the edit profile page")
    public void i_can_optionally_supply_my_location_on_the_edit_profile_page() throws Exception {
        mockMvc.perform(get("/edit-profile")
                        .principal(principal))
                .andExpect(model().attribute("streetAddress", ""))
                .andExpect(model().attribute("suburb", ""))
                .andExpect(model().attribute("city", ""))
                .andExpect(model().attribute("postcode", ""))
                .andExpect(model().attribute("country", ""));
    }

    // AC3
    @Then("I can optionally supply a location for that renovation on the add renovation record page")
    public void i_can_optionally_supply_a_location_for_that_renovation_on_the_add_renovation_record_page() throws Exception {
        mockMvc.perform(get("/my-renovations/create-renovation"))
                .andExpect(model().attribute("streetAddress", ""))
                .andExpect(model().attribute("suburb", ""))
                .andExpect(model().attribute("city", ""))
                .andExpect(model().attribute("postcode", ""))
                .andExpect(model().attribute("country", ""));
    }

    // AC4
    @Then("I can optionally supply a location for that renovation on the edit renovation record page")
    public void i_can_optionally_supply_a_location_for_that_renovation_on_the_edit_renovation_record_page() throws Exception {
        mockMvc.perform(get("/my-renovations/edit-renovation")
                        .principal(principal)
                        .param("recordId", renovationRecord.getId().toString())
                        .param("search", "false-"))
                .andExpect(model().attribute("streetAddress", ""))
                .andExpect(model().attribute("suburb", ""))
                .andExpect(model().attribute("city", ""))
                .andExpect(model().attribute("postcode", ""))
                .andExpect(model().attribute("country", ""));
    }

    // AC11
    @Then("The form is saved with the address I supplied")
    public void the_form_is_saved_with_the_address_i_supplied() {
        User user = userCaptor.getValue();
        Assertions.assertEquals("123 ABC Street", user.getStreetAddress());
        Assertions.assertEquals("ABC", user.getSuburb());
        Assertions.assertEquals("ABC", user.getCity());
        Assertions.assertEquals("123", user.getPostcode());
        Assertions.assertEquals("ABC", user.getCountry());
    }
}
