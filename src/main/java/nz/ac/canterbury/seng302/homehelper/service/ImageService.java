package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class ImageService {
    Logger logger = LoggerFactory.getLogger(RenovationRecordService.class);
    UserService userService;
    ValidationService validationService;
    private final JobService jobService;

    @Autowired
    public ImageService(ValidationService validationService, UserService userService, JobService jobService) {
        this.validationService = validationService;
        this.userService = userService;
        this.jobService = jobService;
    }

    /**
     * Validates a image submitted by a user and returns a unique error message for each type of error
     * @param file a file submitted by the user
     */
    public void validateImage(MultipartFile file) {
        if (!validationService.checkImageType(file)) {
            throw new IllegalArgumentException("Image must be of type png, jpg or svg");
        }
        if (!validationService.checkImageSize(file)) {
            throw new IllegalArgumentException("Image must be less than 10MB");
        }
    }

    /**
     * Checks to confirm a job has a valid number of images
     * @param job the job to check
     */
    public void checkJobImageCount(Job job) {
        if (!(job.getImageFilenames().size() < 5)) {
            throw new IllegalArgumentException("You cannot have more than 5 images per job");
        }
    }

    /**
     * Saves the image the user uploaded to server side storage and saves the name of the file to the users profile
     * @param file Img file which the user has uploaded
     * @param user The logged-in user who is changing their profile picture
     * @throws IOException Error which can be thrown due to creating new directory
     */
    public void saveImage(MultipartFile file, User user) throws IOException {
        String uniqueFileName = "user" + user.getId().toString() + file.getOriginalFilename();

        Path uploadPath = Paths.get("uploads/images");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Saving image at: {}", filePath.toAbsolutePath());
        userService.updateDetails(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), uniqueFileName);
    }

    /**
     * Saves the image that a user has uploaded for a job
     * Saves the file to the storage while linking the image filename with the job it is set for.
     * @param file The image to save
     * @param job The job to save the image to.
     * @throws IOException In the case that there is an error with retrieving or saving the file
     */
    public void saveJobImage(MultipartFile file, Job job) throws IOException {
        String uniqueFileName = "job" + job.getId().toString() + file.getOriginalFilename();

        Path uploadPath = Paths.get("uploads/images");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Saving image at: {}", filePath.toAbsolutePath());
        job.addImageFilename(uniqueFileName);
        jobService.addJob(job);
    }

    /**
     * Removes an image from a given job.
     * Deletes the job from storage and unlinks the job and image filename.
     * @param filename The name of the image to remove.
     * @param job The job the image is being removed from.
     * @throws IOException In the case that there is an error with retrieving or saving the image file.
     */
    public void removeJobImage(String filename, Job job) throws IOException {
        Path uploadPath = Paths.get("uploads/images");
        Path filePath = uploadPath.resolve(filename);
        Files.deleteIfExists(filePath);
        job.removeImageFilename(filename);
        jobService.addJob(job);
    }

    /**
     * Saves the image that a user has uploaded for a job
     * @param file The image to save
     * @param room The room to save the image to
     * @throws IOException In the case that there is an error with retrieving or saving the file
     */
    public void saveRoomImage(MultipartFile file, Room room) throws IOException {
        String uniqueFileName = "room" + room.getId().toString() + file.getOriginalFilename();

        Path uploadPath = Paths.get("uploads/images");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        logger.info("Saving image at: {}", filePath.toAbsolutePath());
        room.setImageFilename(uniqueFileName);
    }
}
