package nz.ac.canterbury.seng302.homehelper.controller.account;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.JobRepository;
import nz.ac.canterbury.seng302.homehelper.repository.QuoteRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.*;
import nz.ac.canterbury.seng302.homehelper.entity.dto.MapPosition;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class UserProfileController {
    private final UserRepository userRepository;
    private final QuoteRepository quoteRepository;
    private final JobRepository jobRepository;
    Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    private final UserService userService;
    private final RenovationRecordService renovationRecordService;
    private final JobService jobService;
    private final ImageService imageService;

    @Autowired
    public UserProfileController(UserService userService, RenovationRecordService renovationRecordService, UserRepository userRepository,
                                 QuoteRepository quoteRepository, JobRepository jobRepository, JobService jobService, ImageService imageService) {
        this.userService = userService;
        this.renovationRecordService = renovationRecordService;
        this.userRepository = userRepository;
        this.quoteRepository = quoteRepository;
        this.jobRepository = jobRepository;
        this.jobService = jobService;
        this.imageService = imageService;
    }

    /**
     * Gets the thymeleaf page for the /profile page of the website.
     * @param principal the currently authenticated user.
     * @param model the representation of data to be used in thymeleaf display
     * @return the userProfile html.
     */
    @GetMapping(value = "/profile")
    public String profile(@RequestParam(name = "userId", required = false) Long userId,
                          @RequestParam(name = "completedPage", defaultValue = "1") Integer completedPage,
                          @RequestParam(name = "portfolioPage", defaultValue = "1") Integer portfolioPage,
                          Principal principal, Model model) {
        logger.info("GET /profile");

        String email = principal.getName();
        User viewingUser = userService.getUser(email);
        User profileUser = userService.getUserById(userId);
        jobService.setNumberOfJobs(9);

        if (profileUser == viewingUser) {
            model.addAttribute("ownProfile", true);
        } else {
            model.addAttribute("ownProfile", false);
        }
        model.addAttribute("profileUser", profileUser);
        if (profileUser.getProfilePicture() != null) {
            model.addAttribute("profileUserImage", "/profileImages/" + profileUser.getProfilePicture());
        }
        
        List<Job> completedJobs = userService.getCompletedJobsUserHasWorkedOn(userId);
        List<Job> portfolioJobs = userService.getPortfolioJobs(profileUser);

        List<Job> completedJobsPage = PaginationUtil.getPage(completedJobs, completedPage, 9);
        List<Integer> completedJobsPageNums = PaginationUtil.getPageNumbers(completedJobs.size(), 9);

        List<Job> portfolioJobsPage = PaginationUtil.getPage(portfolioJobs, portfolioPage, 9);
        List<Integer> portfolioJobsPageNums = PaginationUtil.getPageNumbers(portfolioJobs.size(), 9);

        model.addAttribute("loggedIn", principal != null);
        model.addAttribute("userId", userId);
        model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(email));
        model.addAttribute("user", viewingUser);
        model.addAttribute("portfolioJobs", portfolioJobs);
        model.addAttribute("portfolioJobsPage", portfolioJobsPage);
        model.addAttribute("completedJobs", completedJobsPage);
        model.addAttribute("currentCompletedJobPage", completedPage);
        model.addAttribute("currentPortfolioJobPage", portfolioPage);
        model.addAttribute("portfolioJobsPageNums", portfolioJobsPageNums);
        model.addAttribute("completedJobsPageNums", completedJobsPageNums);
        model.addAttribute("lastPage", jobService.getNumPages(completedJobs));
        model.addAttribute("lastPortfolioJobsPage", PaginationUtil.getLastPageNumber(portfolioJobs.size(), 9));
        if (viewingUser.getProfilePicture() != null) {
            model.addAttribute("profileImage", "/profileImages/" + viewingUser.getProfilePicture());
        }

        return "userProfileTemplate";
    }

    /**
     * handles GET requests to the map and displays the map page
     * @param model data used to verify user and send them back to profile when 'back' button pressed
     * @return the map html
     */
    @GetMapping("/map")
    public String showMap(@RequestParam(name = "userId", required = false) Long userId,
                          Principal principal,
                          Model model) {
        logger.info("GET /map");

        User viewingUser = userService.getUser(principal.getName());

        model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
        model.addAttribute("userId", userId);
        model.addAttribute("loggedIn", true);
        model.addAttribute("user", viewingUser);
        if (viewingUser.getProfilePicture() != null) {
            model.addAttribute("profileImage", "/profileImages/" + viewingUser.getProfilePicture());
        }

        return "mapTemplate";
    }

    /**
     * handles POST requests to addMarker to receive the coordinates of a marker on the map
     * @param pos latitude longitude DTO object used to find a street address
     * @return the latitude and longitude
     */
    @PostMapping("/addMarker")
    @ResponseBody
    public String addMarker(@RequestBody MapPosition pos) {
        logger.info("POST /addMarker");
        double lat = pos.lat;
        double lng = pos.lng;
        return String.format("Clicked position: lat=%f, lng=%f", lat, lng);
    }

    /**
     * After checking/unchecking a checkbox on a job on the completed jobs tab, the job will be added to a user's list
     * of portfolio jobs if it does not already exist in it, or removed from the portfolio jobs list if it already does
     * @param userId ID of the user
     * @param jobId ID of the job being added/removed from the user's portfolio jobs list
     * @param request link requester to find the prev link
     * @return redirect to the profile page
     */
    @PostMapping(value = "/add-to-profile")
    public String addCompletedJobToProfile(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "jobId") Long jobId,
            HttpServletRequest request) {
        logger.info("POST /add-to-profile");

        Job job = jobService.getJobById(jobId);
        User user = userService.getUserById(userId);
        if (user.getPortfolioJobs().contains(job)) {
            job.removePortfolioUser(user);
            user.removePortfolioJob(job);
        } else {
            job.addPortfolioUser(user);
            user.addPortfolioJob(job);
        }
        jobService.addJob(job);
        userService.addUser(user);

        return "redirect:" + request.getHeader("Referer");
    }

    @PostMapping(value = "/profile")
    public String postCompletedJobPageNumber(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "completedPage", defaultValue = "1") Integer completedPage,
            @RequestParam(name = "newCompletedPage", required = false) Integer newCompletedPage,
            @RequestParam(name = "portfolioPage", defaultValue = "1") Integer portfolioPage,
            @RequestParam(name = "newPortfolioPage", required = false) Integer newPortfolioPage)
    {
        logger.info("POST /profile");
        try {
            if (newPortfolioPage == null) {
                return "redirect:/profile?userId=" + userId + "&completedPage=" + newCompletedPage + "&portfolioPage=" + portfolioPage;
            }
            else {
                return "redirect:/profile?userId=" + userId + "&completedPage=" + completedPage + "&portfolioPage=" + newPortfolioPage;
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            return "error";
        }

    }

    /**
     * get mapping that responds with a list of map marker dtos. These have jobs and their locations.
     * @param userId tradie that has portfolio jobs to show on map.
     * @return a response entity containing map marker dtos.
     */
    @GetMapping("/profile/map/jobs")
    public ResponseEntity<?> getTradiesPortfolioJobMarkers(
            @RequestParam(name = "userId") Long userId
    ) {
        logger.info("GET /profile/map/jobs?userId={}", userId);
        try {
            return ResponseEntity.of(Optional.of(userService.getPortfolioMapMarkers(userId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Adds an image to a job
     * @param jobId The id of the job the image is being added to.
     * @param submittedFile The file which the user has submitted
     * @return The profile page
     */
    @PostMapping("/add-job-image")
    public String addJobImage(
            @RequestParam(name = "jobId") Long jobId,
            @RequestParam(name = "submittedFile") MultipartFile submittedFile,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        logger.info("POST /add-job-image");

        try {
            imageService.validateImage(submittedFile);
            Job job = jobService.getJobById(jobId);
            imageService.checkJobImageCount(job);
            imageService.saveJobImage(submittedFile, job);
        } catch (IOException | IllegalArgumentException e) {
            logger.error(e.getMessage());
            redirectAttributes.addFlashAttribute("fileErrorMessage", e.getMessage());
        }
        redirectAttributes.addFlashAttribute("jobId", jobId);
        return "redirect:" + request.getHeader("Referer");
    }

    /**
     * Remove an image from a job
     * @param jobId the id of the job to remove an image from.
     * @param filename the name of the image to remove.
     * @return the template that the user is on.
     */
    @PostMapping("/remove-job-image")
    public String removeJobImage(
            @RequestParam(name = "jobId") Long jobId,
            @RequestParam(name = "filename") String filename,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        logger.info("POST /remove-job-image");

        try {
            Job job = jobService.getJobById(jobId);
            imageService.removeJobImage(filename, job);
        } catch (IOException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("fileErrorMessage", e.getMessage());
        }
        redirectAttributes.addFlashAttribute("jobId", jobId);
        return "redirect:" + request.getHeader("Referer");
    }
}
