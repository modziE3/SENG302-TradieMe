package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.controller.account.EditProfileController;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.ImageService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class UpdateProfileImageIntegrationTest {

    @Autowired
    private EditProfileController editProfileController;

    private MockMvc mockMvc;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @PostConstruct
    public void setup() {mockMvc = MockMvcBuilders.standaloneSetup(editProfileController).build(); }


    @Test
    void addProfileImage_InvalidImageType_ReturnsError() throws Exception {
        when(userRepository.findUserById((long) 1)).thenReturn(new User("F", "O", "fo@gmail.com", "Ab123!", null, null));

        MockMultipartFile file = new MockMultipartFile("submittedFile", "test.gif", "image/gif", "test".getBytes());

        mockMvc.perform(multipart("/update-image")
                    .file(file)
                    .param("userId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("fileErrorMessage", "Image must be of type png, jpg or svg"));
    }

    @Test
    void addProfileImage_ValidImagePng_UploadsImage() throws Exception {
        User user = new User("F", "O", "fo@gmail.com", "Ab123!", null, null);
        user.setId((long) 1);
        when(userRepository.findUserById((long) 1)).thenReturn(user);

        MockMultipartFile file = new MockMultipartFile("submittedFile", "test.png", "image/png", "test".getBytes());

        mockMvc.perform(multipart("/update-image")
                .file(file)
                .param("userId", "1"));

        user.setProfilePicture("user1test.png");
        Mockito.verify(userRepository).updateDetails(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getProfilePicture());
    }

    @Test
    void addProfileImage_ValidImageJpg_UploadsImage() throws Exception {
        User user = new User("F", "O", "fo@gmail.com", "Ab123!", null, null);
        user.setId((long) 1);
        when(userRepository.findUserById((long) 1)).thenReturn(user);

        MockMultipartFile file = new MockMultipartFile("submittedFile", "test.jpg", "image/jpeg", "test".getBytes());

        mockMvc.perform(multipart("/update-image")
                .file(file)
                .param("userId", "1"));

        user.setProfilePicture("user1test.jpg");
        Mockito.verify(userRepository).updateDetails(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getProfilePicture());
    }

    @Test
    void addProfileImage_ValidImageSvg_UploadsImage() throws Exception {
        User user = new User("F", "O", "fo@gmail.com", "Ab123!", null, null);
        user.setId((long) 1);
        when(userRepository.findUserById((long) 1)).thenReturn(user);

        MockMultipartFile file = new MockMultipartFile("submittedFile", "test.svg", "image/svg+xml", "test".getBytes());

        mockMvc.perform(multipart("/update-image")
                .file(file)
                .param("userId", "1"));

        user.setProfilePicture("user1test.svg");
        Mockito.verify(userRepository).updateDetails(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getProfilePicture());
    }

    @Test
    void addProfileImage_UserAlreadyHasProfile_UploadsImage() throws Exception {
        User user = new User("F", "O", "fo@gmail.com", "Ab123!", "user1CoolPhoto.png", null);
        user.setId((long) 1);
        when(userRepository.findUserById((long) 1)).thenReturn(user);

        MockMultipartFile file = new MockMultipartFile("submittedFile", "test.svg", "image/svg+xml", "test".getBytes());

        mockMvc.perform(multipart("/update-image")
                .file(file)
                .param("userId", "1"));

        user.setProfilePicture("user1test.svg");
        Mockito.verify(userRepository).updateDetails(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getProfilePicture());
    }
}
