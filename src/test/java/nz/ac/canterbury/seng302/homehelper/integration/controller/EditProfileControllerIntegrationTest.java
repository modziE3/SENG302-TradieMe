package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.controller.account.EditProfileController;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class EditProfileControllerIntegrationTest {

    @Autowired
    private UserService userService;
    @Autowired
    private EditProfileController editProfileController;
    @MockBean
    private UserRepository userRepository;

    private MockMvc mockMvc;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final User testUser = new User("John", "Doe", "john@example.com", passwordEncoder.encode("P4$$word"), null, null);
    private final User updatedUser = new User("John", "Cena", "john@example.com", passwordEncoder.encode("P4$$word"), null, null);
    private Principal authentication;


    @PostConstruct
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(editProfileController).build();
        updatedUser.grantAuthority("ROLE_USER");
        testUser.grantAuthority("ROLE_USER");
        testUser.setId(9999L);
        updatedUser.setId(9999L);
        authentication = new UsernamePasswordAuthenticationToken(testUser.getEmail(), null, testUser.getAuthorities());
    }

    @BeforeEach
    public void before() {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("john@example.com");
    }

    @Test
    public void testGetEditProfile_DefaultValues() throws Exception {
        when(userRepository.findByEmailContainingIgnoreCase(authentication.getName())).thenReturn(testUser);

        mockMvc.perform(get("/edit-profile")
                        .sessionAttr(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, authentication)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("editProfileTemplate"))
                .andExpect(model().attribute("firstName", testUser.getFirstName()))
                .andExpect(model().attribute("lastName", testUser.getLastName()))
                .andExpect(model().attribute("email", testUser.getEmail()));
    }

    @Test
    public void testEditProfileForm_WithValidValues() throws Exception {
        when(userRepository.findByEmailContainingIgnoreCase(authentication.getName())).thenReturn(testUser);
        when(userRepository.findUserById(testUser.getId())).thenReturn(updatedUser);

        mockMvc.perform(post("/edit-profile")
                        .param("userId", testUser.getId().toString())
                        .param("firstName", updatedUser.getFirstName())
                        .param("lastName", updatedUser.getLastName())
                        .param("email", updatedUser.getEmail())
                        .param("streetAddress", "")
                        .param("suburb","")
                        .param("city", "")
                        .param("postcode", "")
                        .param("country", "")
                        .principal(authentication))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:./profile?userId="+testUser.getId()));

        verify(userRepository).updateDetails(testUser.getId(), updatedUser.getEmail(), updatedUser.getFirstName(), updatedUser.getLastName(), updatedUser.getProfilePicture());
    }

    @Test
    void testEditProfileForm_WithBlankFirstName() throws Exception {
        when(userRepository.findByEmailContainingIgnoreCase(authentication.getName())).thenReturn(testUser);
        when(userRepository.findUserById(testUser.getId())).thenReturn(updatedUser);

        mockMvc.perform(post("/edit-profile")
                        .param("userId", testUser.getId().toString())
                        .param("email", updatedUser.getEmail())
                        .param("firstName", "")
                        .param("lastName", updatedUser.getLastName())
                        .param("streetAddress", "")
                        .param("suburb","")
                        .param("city", "")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0))
                        .param("postcode", "")
                        .param("country", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("editProfileTemplate"))
                .andExpect(model().attribute("email", updatedUser.getEmail()))
                .andExpect(model().attribute("firstName", ""))
                .andExpect(model().attribute("lastName", updatedUser.getLastName()))
                .andExpect(model().attribute("firstNameErrorMessage", "First name cannot be empty"));

        verify(userRepository, never()).updateDetails(testUser.getId(), updatedUser.getEmail(), "", updatedUser.getLastName(), updatedUser.getProfilePicture());
    }

    @Test
    void testEditProfileForm_WithBlankLastName() throws Exception {
        when(userRepository.findByEmailContainingIgnoreCase(authentication.getName())).thenReturn(testUser);
        when(userRepository.findUserById(testUser.getId())).thenReturn(updatedUser);

        mockMvc.perform(post("/edit-profile")
                        .param("userId", testUser.getId().toString())
                        .param("email", updatedUser.getEmail())
                        .param("firstName", updatedUser.getFirstName())
                        .param("lastName", "")
                        .param("streetAddress", "")
                        .param("suburb","")
                        .param("city", "")
                        .param("postcode", "")
                        .param("country", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:./profile?userId="+testUser.getId()));

        verify(userRepository).updateDetails(testUser.getId(), updatedUser.getEmail(), updatedUser.getFirstName(), "", updatedUser.getProfilePicture());
    }

    @Test
    void testEditProfileForm_InvalidCharacterFirstName() throws Exception {
        when(userRepository.findByEmailContainingIgnoreCase(authentication.getName())).thenReturn(testUser);
        when(userRepository.findUserById(testUser.getId())).thenReturn(updatedUser);

        mockMvc.perform(post("/edit-profile")
                        .param("userId", testUser.getId().toString())
                        .param("email", updatedUser.getEmail())
                        .param("firstName", "John!")
                        .param("lastName", updatedUser.getLastName())
                        .param("streetAddress", "")
                        .param("suburb","")
                        .param("city", "")
                        .param("postcode", "")
                        .param("country", "")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)))
                .andExpect(status().isOk())
                .andExpect(view().name("editProfileTemplate"))
                .andExpect(model().attribute("email", updatedUser.getEmail()))
                .andExpect(model().attribute("firstName", "John!"))
                .andExpect(model().attribute("lastName", updatedUser.getLastName()))
                .andExpect(model().attribute("firstNameErrorMessage", "First name must only include letters, spaces, hyphens or apostrophes"));


        verify(userRepository, never()).updateDetails(testUser.getId(), updatedUser.getEmail(), "John!", updatedUser.getLastName(), updatedUser.getProfilePicture());
    }

    @Test
    void testEditProfileForm_InvalidCharacterLastName() throws Exception {
        when(userRepository.findByEmailContainingIgnoreCase(authentication.getName())).thenReturn(testUser);
        when(userRepository.findUserById(testUser.getId())).thenReturn(updatedUser);


        mockMvc.perform(post("/edit-profile")
                        .param("userId", testUser.getId().toString())
                        .param("email", updatedUser.getEmail())
                        .param("firstName", updatedUser.getFirstName())
                        .param("lastName", "Cena$")
                        .param("streetAddress", "")
                        .param("suburb","")
                        .param("city", "")
                        .param("postcode", "")
                        .param("country", "")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)))
                .andExpect(status().isOk())
                .andExpect(view().name("editProfileTemplate"))
                .andExpect(model().attribute("email", updatedUser.getEmail()))
                .andExpect(model().attribute("firstName", updatedUser.getFirstName()))
                .andExpect(model().attribute("lastName", "Cena$"))
                .andExpect(model().attribute("lastNameErrorMessage", "Last name must only include letters, spaces, hyphens or apostrophes"));


        verify(userRepository, never()).updateDetails(testUser.getId(), updatedUser.getEmail(), updatedUser.getFirstName(), "Cena$", updatedUser.getProfilePicture());
    }

    @Test
    void testEditProfileForm_FirstNameTooLong() throws Exception {
        when(userRepository.findByEmailContainingIgnoreCase(authentication.getName())).thenReturn(testUser);
        when(userRepository.findUserById(testUser.getId())).thenReturn(updatedUser);

        mockMvc.perform(post("/edit-profile")
                        .param("userId", testUser.getId().toString())
                        .param("email", updatedUser.getEmail())
                        .param("firstName", "A" .repeat(100))
                        .param("lastName", updatedUser.getLastName())
                        .param("streetAddress", "")
                        .param("suburb","")
                        .param("city", "")
                        .param("postcode", "")
                        .param("country", "")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)))
                .andExpect(status().isOk())
                .andExpect(view().name("editProfileTemplate"))
                .andExpect(model().attribute("email", updatedUser.getEmail()))
                .andExpect(model().attribute("firstName", "A" .repeat(100)))
                .andExpect(model().attribute("lastName", updatedUser.getLastName()))
                .andExpect(model().attribute("firstNameErrorMessage", "First name must be 64 characters long or less"));

        verify(userRepository, never()).updateDetails(testUser.getId(), updatedUser.getEmail(), "A" .repeat(100), updatedUser.getLastName(), updatedUser.getProfilePicture());
    }

    @Test
    void testEditProfileForm_LastNameTooLong() throws Exception {
        when(userRepository.findByEmailContainingIgnoreCase(authentication.getName())).thenReturn(testUser);
        when(userRepository.findUserById(testUser.getId())).thenReturn(updatedUser);

        mockMvc.perform(post("/edit-profile")
                        .param("userId", testUser.getId().toString())
                        .param("email", updatedUser.getEmail())
                        .param("firstName", updatedUser.getFirstName())
                        .param("lastName", "B" .repeat(100))
                        .param("streetAddress", "")
                        .param("suburb","")
                        .param("city", "")
                        .param("postcode", "")
                        .param("country", "")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)))
                .andExpect(status().isOk())
                .andExpect(view().name("editProfileTemplate"))
                .andExpect(model().attribute("email", updatedUser.getEmail()))
                .andExpect(model().attribute("firstName", updatedUser.getFirstName()))
                .andExpect(model().attribute("lastName", "B" .repeat(100)))
                .andExpect(model().attribute("lastNameErrorMessage", "Last name must be 64 characters long or less"));

        verify(userRepository, never()).updateDetails(testUser.getId(), updatedUser.getEmail(), updatedUser.getFirstName(), "B".repeat(100), updatedUser.getProfilePicture());
    }

    @Test
    void testEditProfileForm_InvalidEmail() throws Exception {
        when(userRepository.findByEmailContainingIgnoreCase(authentication.getName())).thenReturn(testUser);
        when(userRepository.findUserById(testUser.getId())).thenReturn(updatedUser);

        mockMvc.perform(post("/edit-profile")
                        .param("userId", testUser.getId().toString())
                        .param("email", "testemailtest.com")
                        .param("firstName", updatedUser.getFirstName())
                        .param("lastName", updatedUser.getLastName())
                        .param("streetAddress", "")
                        .param("suburb","")
                        .param("city", "")
                        .param("postcode", "")
                        .param("country", "")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)))
                .andExpect(view().name("editProfileTemplate"))
                .andExpect(model().attribute("email", "testemailtest.com"))
                .andExpect(model().attribute("firstName", updatedUser.getFirstName()))
                .andExpect(model().attribute("lastName", updatedUser.getLastName()))
                .andExpect(model().attribute("emailErrorMessage", "Email address must be in the form 'jane@doe.nz'"));

        verify(userRepository, never()).updateDetails(testUser.getId(), "testemailtest.com", updatedUser.getFirstName(), updatedUser.getLastName(), updatedUser.getProfilePicture());
    }


    @Test
    public void testEditProfileForm_NotUniqueEmail() throws Exception {
        User secondUser = new User("Not", "Unique", "email@email.com", "P4$$word", null, null);
        secondUser.setId(8888L);

        when(userRepository.findByEmailContainingIgnoreCase(authentication.getName())).thenReturn(testUser);
        when(userRepository.findByEmailContainingIgnoreCase("email@email.com")).thenReturn(secondUser);

        mockMvc.perform(post("/edit-profile")
                        .param("userId", testUser.getId().toString())
                        .param("email", "email@email.com")
                        .param("firstName", updatedUser.getFirstName())
                        .param("lastName", updatedUser.getLastName())
                        .param("streetAddress", "")
                        .param("suburb","")
                        .param("city", "")
                        .param("postcode", "")
                        .param("country", "")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)))
                .andExpect(status().isOk())
                .andExpect(view().name("editProfileTemplate"))
                .andExpect(model().attribute("email", "email@email.com"))
                .andExpect(model().attribute("firstName", updatedUser.getFirstName()))
                .andExpect(model().attribute("lastName", updatedUser.getLastName()))
                .andExpect(model().attribute("emailErrorMessage", "This email address is already in use"));

        verify(userRepository, never()).updateDetails(testUser.getId(), "email@email.com", updatedUser.getFirstName(), updatedUser.getLastName(), updatedUser.getProfilePicture());
    }

    @Test
    void testEditProfileForm_InvalidLocation_NoStreetAddress() throws Exception {
        when(userRepository.findByEmailContainingIgnoreCase(authentication.getName())).thenReturn(testUser);
        when(userRepository.findUserById(testUser.getId())).thenReturn(updatedUser);

        mockMvc.perform(post("/edit-profile")
                        .param("userId", testUser.getId().toString())
                        .param("email", updatedUser.getEmail())
                        .param("firstName", updatedUser.getFirstName())
                        .param("lastName", updatedUser.getLastName())
                        .param("streetAddress", "")
                        .param("suburb","suburb")
                        .param("city", "city")
                        .param("postcode", "postcode")
                        .param("country", "country")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)))
                .andExpect(status().isOk())
                .andExpect(view().name("editProfileTemplate"))
                .andExpect(model().attribute("email", updatedUser.getEmail()))
                .andExpect(model().attribute("firstName", updatedUser.getFirstName()))
                .andExpect(model().attribute("lastName", updatedUser.getLastName()))
                .andExpect(model().attribute("addressErrorMessage", LocationService.STREET_ADDRESS_EMPTY));

        verify(userRepository, never()).updateDetails(testUser.getId(), updatedUser.getEmail(), updatedUser.getFirstName(), "B".repeat(100), updatedUser.getProfilePicture());
    }

    @Test
    void testEditProfileForm_InvalidLocation_InvalidStreetAddress() throws Exception {
        when(userRepository.findByEmailContainingIgnoreCase(authentication.getName())).thenReturn(testUser);
        when(userRepository.findUserById(testUser.getId())).thenReturn(updatedUser);

        mockMvc.perform(post("/edit-profile")
                        .param("userId", testUser.getId().toString())
                        .param("email", updatedUser.getEmail())
                        .param("firstName", updatedUser.getFirstName())
                        .param("lastName", updatedUser.getLastName())
                        .param("streetAddress", "###")
                        .param("suburb","suburb")
                        .param("city", "city")
                        .param("postcode", "postcode")
                        .param("country", "country")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)))
                .andExpect(status().isOk())
                .andExpect(view().name("editProfileTemplate"))
                .andExpect(model().attribute("email", updatedUser.getEmail()))
                .andExpect(model().attribute("firstName", updatedUser.getFirstName()))
                .andExpect(model().attribute("lastName", updatedUser.getLastName()))
                .andExpect(model().attribute("addressErrorMessage", LocationService.STREET_ADDRESS_INVALID))
                .andExpect(model().attributeDoesNotExist("suburbErrorMessage"))
                .andExpect(model().attributeDoesNotExist("cityErrorMessage"))
                .andExpect(model().attributeDoesNotExist("postcodeErrorMessage"))
                .andExpect(model().attributeDoesNotExist("countryErrorMessage"));

        verify(userRepository, never()).updateDetails(testUser.getId(), updatedUser.getEmail(), updatedUser.getFirstName(), "B".repeat(100), updatedUser.getProfilePicture());
    }

    @Test
    void testEditProfileForm_InvalidLocation_InvalidSuburb() throws Exception {
        when(userRepository.findByEmailContainingIgnoreCase(authentication.getName())).thenReturn(testUser);
        when(userRepository.findUserById(testUser.getId())).thenReturn(updatedUser);

        mockMvc.perform(post("/edit-profile")
                        .param("userId", testUser.getId().toString())
                        .param("email", updatedUser.getEmail())
                        .param("firstName", updatedUser.getFirstName())
                        .param("lastName", updatedUser.getLastName())
                        .param("streetAddress", "street")
                        .param("suburb","###")
                        .param("city", "city")
                        .param("postcode", "postcode")
                        .param("country", "country")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)))
                .andExpect(status().isOk())
                .andExpect(view().name("editProfileTemplate"))
                .andExpect(model().attribute("email", updatedUser.getEmail()))
                .andExpect(model().attribute("firstName", updatedUser.getFirstName()))
                .andExpect(model().attribute("lastName", updatedUser.getLastName()))
                .andExpect(model().attributeDoesNotExist("addressErrorMessage"))
                .andExpect(model().attribute("suburbErrorMessage", LocationService.SUBURB_INVALID))
                .andExpect(model().attributeDoesNotExist("cityErrorMessage"))
                .andExpect(model().attributeDoesNotExist("postcodeErrorMessage"))
                .andExpect(model().attributeDoesNotExist("countryErrorMessage"));

        verify(userRepository, never()).updateDetails(testUser.getId(), updatedUser.getEmail(), updatedUser.getFirstName(), "B".repeat(100), updatedUser.getProfilePicture());
    }

    @Test
    void testEditProfileForm_InvalidLocation_InvalidCity() throws Exception {
        when(userRepository.findByEmailContainingIgnoreCase(authentication.getName())).thenReturn(testUser);
        when(userRepository.findUserById(testUser.getId())).thenReturn(updatedUser);

        mockMvc.perform(post("/edit-profile")
                        .param("userId", testUser.getId().toString())
                        .param("email", updatedUser.getEmail())
                        .param("firstName", updatedUser.getFirstName())
                        .param("lastName", updatedUser.getLastName())
                        .param("streetAddress", "street")
                        .param("suburb","suburb")
                        .param("city", "###")
                        .param("postcode", "postcode")
                        .param("country", "country")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)))
                .andExpect(status().isOk())
                .andExpect(view().name("editProfileTemplate"))
                .andExpect(model().attribute("email", updatedUser.getEmail()))
                .andExpect(model().attribute("firstName", updatedUser.getFirstName()))
                .andExpect(model().attribute("lastName", updatedUser.getLastName()))
                .andExpect(model().attributeDoesNotExist("addressErrorMessage"))
                .andExpect(model().attributeDoesNotExist("suburbErrorMessage"))
                .andExpect(model().attribute("cityErrorMessage", LocationService.CITY_INVALID))
                .andExpect(model().attributeDoesNotExist("postcodeErrorMessage"))
                .andExpect(model().attributeDoesNotExist("countryErrorMessage"));

        verify(userRepository, never()).updateDetails(testUser.getId(), updatedUser.getEmail(), updatedUser.getFirstName(), "B".repeat(100), updatedUser.getProfilePicture());
    }

    @Test
    void testEditProfileForm_InvalidLocation_InvalidPostcode() throws Exception {
        when(userRepository.findByEmailContainingIgnoreCase(authentication.getName())).thenReturn(testUser);
        when(userRepository.findUserById(testUser.getId())).thenReturn(updatedUser);

        mockMvc.perform(post("/edit-profile")
                        .param("userId", testUser.getId().toString())
                        .param("email", updatedUser.getEmail())
                        .param("firstName", updatedUser.getFirstName())
                        .param("lastName", updatedUser.getLastName())
                        .param("streetAddress", "street")
                        .param("suburb","suburb")
                        .param("city", "city")
                        .param("postcode", "###")
                        .param("country", "country")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)))
                .andExpect(status().isOk())
                .andExpect(view().name("editProfileTemplate"))
                .andExpect(model().attribute("email", updatedUser.getEmail()))
                .andExpect(model().attribute("firstName", updatedUser.getFirstName()))
                .andExpect(model().attribute("lastName", updatedUser.getLastName()))
                .andExpect(model().attributeDoesNotExist("addressErrorMessage"))
                .andExpect(model().attributeDoesNotExist("suburbErrorMessage"))
                .andExpect(model().attributeDoesNotExist("cityErrorMessage"))
                .andExpect(model().attribute("postcodeErrorMessage", LocationService.POSTCODE_INVALID))
                .andExpect(model().attributeDoesNotExist("countryErrorMessage"));

        verify(userRepository, never()).updateDetails(testUser.getId(), updatedUser.getEmail(), updatedUser.getFirstName(), "B".repeat(100), updatedUser.getProfilePicture());
    }

    @Test
    void testEditProfileForm_InvalidLocation_InvalidCountry() throws Exception {
        when(userRepository.findByEmailContainingIgnoreCase(authentication.getName())).thenReturn(testUser);
        when(userRepository.findUserById(testUser.getId())).thenReturn(updatedUser);

        mockMvc.perform(post("/edit-profile")
                        .param("userId", testUser.getId().toString())
                        .param("email", updatedUser.getEmail())
                        .param("firstName", updatedUser.getFirstName())
                        .param("lastName", updatedUser.getLastName())
                        .param("streetAddress", "street")
                        .param("suburb","suburb")
                        .param("city", "city")
                        .param("postcode", "postcode")
                        .param("country", "###")
                        .param("latitude", String.valueOf(0))
                        .param("longitude", String.valueOf(0)))
                .andExpect(status().isOk())
                .andExpect(view().name("editProfileTemplate"))
                .andExpect(model().attribute("email", updatedUser.getEmail()))
                .andExpect(model().attribute("firstName", updatedUser.getFirstName()))
                .andExpect(model().attribute("lastName", updatedUser.getLastName()))
                .andExpect(model().attributeDoesNotExist("addressErrorMessage"))
                .andExpect(model().attributeDoesNotExist("suburbErrorMessage"))
                .andExpect(model().attributeDoesNotExist("cityErrorMessage"))
                .andExpect(model().attributeDoesNotExist("postcodeErrorMessage"))
                .andExpect(model().attribute("countryErrorMessage", LocationService.COUNTRY_INVALID));

        verify(userRepository, never()).updateDetails(testUser.getId(), updatedUser.getEmail(), updatedUser.getFirstName(), "B".repeat(100), updatedUser.getProfilePicture());
    }
}
