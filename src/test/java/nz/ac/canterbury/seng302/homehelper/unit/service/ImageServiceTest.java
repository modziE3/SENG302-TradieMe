package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.ImageService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class ImageServiceTest {
    @Autowired
    UserService userService;
    @Autowired
    private ImageService imageService;
    @MockBean
    private UserRepository userRepository;

    @Test
    void imageValidation_InValidType_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.gif", "image/gif", "test".getBytes());
        assertThrows(IllegalArgumentException.class, () -> {
            imageService.validateImage(file);
        }, "Image must be of type png, jpg or svg");
    }

    @Test
    void saveImage_ValidImageGiven_ImageSavedAsUserProfilePicture() throws IOException {
        User user = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        user.setId(1L);
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "test".getBytes());
        imageService.saveImage(file, user);

        String uniqueFileName = "user" + user.getId().toString() + file.getOriginalFilename();
        Path uploadPath = Paths.get("uploads/images/");
        Path filePath = uploadPath.resolve(uniqueFileName);
        assertTrue(Files.exists(filePath));
        verify(userRepository).updateDetails(any(), any(), any(), any(), eq(uniqueFileName));
    }

    @Test
    void saveJobImage_ValidJobImageGiven_ImageSavedInJobImages() throws IOException {
        Job job = new Job("Job", "Job", null, null);
        job.setId(1L);
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "test".getBytes());
        imageService.saveJobImage(file, job);

        String uniqueFileName = "job" + job.getId().toString() + file.getOriginalFilename();
        Path uploadPath = Paths.get("uploads/images/");
        Path filePath = uploadPath.resolve(uniqueFileName);
        assertTrue(Files.exists(filePath));
        assertTrue(job.getImageFilenames().contains(uniqueFileName));
    }

    @Test
    void removeJobImage_ValidJobImageGiven_ImageSavedInJobImages() throws IOException {
        Job job = new Job("Job", "Job", null, null);
        job.setId(1L);
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "test".getBytes());
        imageService.saveJobImage(file, job);

        String uniqueFileName = "job" + job.getId().toString() + file.getOriginalFilename();
        imageService.removeJobImage(uniqueFileName, job);

        Path uploadPath = Paths.get("uploads/images/");
        Path filePath = uploadPath.resolve(uniqueFileName);
        assertFalse(Files.exists(filePath));
        assertFalse(job.getImageFilenames().contains(uniqueFileName));
    }

    @Test
    void saveRoomImage_ValidRoomImageGiven_ImageSavedAsRoomImage() throws IOException {
        Room room = new Room("Room");
        room.setId(1L);
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", "test".getBytes());
        imageService.saveRoomImage(file, room);

        String uniqueFileName = "room" + room.getId().toString() + file.getOriginalFilename();
        Path uploadPath = Paths.get("uploads/images/");
        Path filePath = uploadPath.resolve(uniqueFileName);
        assertTrue(Files.exists(filePath));
        assertEquals(room.getImageFilename(), uniqueFileName);
    }

    @Test
    void addImage_5ImagesExist_ExceptionThrown() {
        Job job = new Job("Job", "Job", null, null);
        job.setId(1L);
        List<String> filenames = Arrays.asList("image1", "image2", "image3", "image4", "image5");
        job.setImageFilenames(filenames);

        assertThrows(IllegalArgumentException.class, () -> {
            imageService.checkJobImageCount(job);
        }, "You cannot have more than 5 images per job");

    }
}
