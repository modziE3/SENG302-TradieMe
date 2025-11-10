package nz.ac.canterbury.seng302.homehelper.controller.job;

import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.dto.JobFilter;
import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Controller that manages the job listing page and job listings queries eg. filters and pagination for jobs
 */
@Controller
public class JobListingController {
    Logger logger = LoggerFactory.getLogger(JobListingController.class);

    private final JobService jobService;
    private final RenovationRecordService renovationRecordService;
    private final UserService userService;
    private final LocationService locationService;

    /**
     * Constructor for the Job listings controller. This sets up the services used for
     * fetching and displaying users posted jobs.
     *
     * @param jobService              fetches jobs from the job repo
     * @param renovationRecordService is used to find the locations of jobs.
     * @param userService             used to find info about the users that own/post jobs
     */
    @Autowired
    public JobListingController(
            JobService jobService,
            RenovationRecordService renovationRecordService,
            UserService userService, LocationService locationService) {
        this.jobService = jobService;
        this.renovationRecordService = renovationRecordService;
        this.userService = userService;
        this.locationService = locationService;
    }

    /**
     * This provides the details that are to be displayed on the job listings page.
     *
     * @param principal the logged-in users info
     * @param model     the model which is passed to the front end to extract info
     * @return the job listing page template.
     */
    @GetMapping("/job-listings")
    public String getJobListing(
            @RequestParam(name = "job-page", defaultValue = "1") Integer jobPage,
            @RequestParam(name = "keywords-filter", required = false) String keywordsFilter,
            @RequestParam(name = "jobStartDate", required = false) String jobStartDate,
            @RequestParam(name = "jobDueDate", required = false) String jobDueDate,
            @RequestParam(name = "type-filter", required = false) String typeFilter,
            @RequestParam(name = "city-filter", required = false) String cityFilter,
            @RequestParam(name = "suburb-filter", required = false) String suburbFilter,
            Principal principal,
            HttpSession session,
            Model model) {
        logger.info("GET /job-listings?job-page={}&keywords-filter={}", jobPage, keywordsFilter);
        if (Objects.equals(typeFilter, "") || Objects.equals(typeFilter, "null")) {
            typeFilter = null;
        }

        JobFilter filter = new JobFilter(keywordsFilter, typeFilter, cityFilter, suburbFilter, jobStartDate, jobDueDate);
        Map<String, List<String>> citySuburbs = renovationRecordService.getCityAndSuburb();
        try {
            jobService.validateJobFilter(filter);
        } catch (IllegalArgumentException e) {
            filter = new JobFilter(null, null, null, null, null, null);
            setErrorMessages(model, e.getMessage());
        }

        User user = userService.getUser(principal.getName());
        jobService.setNumberOfJobs(9);
        List<Job> filteredJobs = jobService.getFilteredJobs(filter);
        List<Integer> pages = jobService.getPageListPosted(jobPage, filteredJobs);
        List<Job> paginatedJobs = jobService.getPostedJobPages(jobPage, filteredJobs);

        model.addAttribute("user", user);
        if (user != null && user.getProfilePicture() != null) {
            model.addAttribute("profileImage", "/profileImages/" + user.getProfilePicture());
        }
        model.addAttribute("loggedIn", true);
        model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
        model.addAttribute("jobs", jobService.getJobCardsPosted(userService, paginatedJobs));
        model.addAttribute("pages", pages);
        model.addAttribute("jobPage", jobPage);
        model.addAttribute("lastPage", jobService.getNumPages(filteredJobs));
        model.addAttribute("keywordsFilter", keywordsFilter != null ? keywordsFilter : "");
        model.addAttribute("typeFilter",  typeFilter != null ? typeFilter : "");
        model.addAttribute("jobStartDate", jobStartDate != null ? jobStartDate : "");
        model.addAttribute("jobDueDate", jobDueDate != null ? jobDueDate : "");
        model.addAttribute("cityFilter", cityFilter != null ? cityFilter : "");
        model.addAttribute("suburbFilter", suburbFilter != null ? suburbFilter : "");
        model.addAttribute("citySuburb", citySuburbs);

        model.addAttribute("user", user);
        if (user != null && user.getProfilePicture() != null) {
            model.addAttribute("profileImage", "/profileImages/" + user.getProfilePicture());
        }
        return "jobListingTemplate";
    }

    /**
     * Posts the job page number the user has inputted, validates the number
     * then redirects to the corresponding job page
     * @param currentPageNumber Current job page the user is on
     * @param newPageNumber Job page number the user wants to go to
     * @return thymeleaf renovationDetailsTemplate
     */
    @PostMapping("/job-listings")
    public String postJobPageNumber(
            @RequestParam(name = "job-page", defaultValue = "1") Integer currentPageNumber,
            @RequestParam(name = "pageNumber") Integer newPageNumber,
            @RequestParam(name = "keywords-filter", required = false) String keywordsFilter,
            @RequestParam(name = "jobStartDate", required = false) String jobStartDate,
            @RequestParam(name = "jobDueDate", required = false) String jobDueDate,
            @RequestParam(name = "type-filter", required = false) String typeFilter,
            @RequestParam(name = "city-filter", required = false) String cityFilter,
            @RequestParam(name = "suburb-filter", required = false) String suburbFilter) {
        logger.info("POST /job-listings-page-number");
        JobFilter filter = new JobFilter(
                keywordsFilter,
                typeFilter,
                cityFilter,
                suburbFilter,
                jobStartDate,
                jobDueDate
        );

        try {
            List<Job> filteredJobs = jobService.getFilteredJobs(filter);
            List<Integer> pages = jobService.getPageListPosted(currentPageNumber, filteredJobs);
            if (jobService.pageNumberIsInJobPageList(pages, newPageNumber)) {
                currentPageNumber = newPageNumber;
            }
            return String.format("redirect:/job-listings" +
                    "?job-page=%s" +
                    "&keywords-filter=%s" +
                    "&jobStartDate=%s" +
                    "&jobDueDate=%s" +
                    "&type-filter=%s" +
                    "&city-filter=%s" +
                    "&suburb-filter=%s",
                    currentPageNumber,
                    keywordsFilter != null ? keywordsFilter : "",
                    jobStartDate,
                    jobDueDate,
                    typeFilter,
                    cityFilter,
                    suburbFilter
            );
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return "error";
        }
    }

    /**
     * Sets error messages for the add/edit job form
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param allErrorMessages String of all error messages wanting to be displayed
     */
    private void setErrorMessages(Model model, String allErrorMessages) {
        List<String> jobErrorMessages = jobService.getErrorMessages(allErrorMessages);
        List<String> locationErrorMessages = locationService.getErrorMessages(allErrorMessages);
        model.addAttribute("cityErrorMessage", locationErrorMessages.get(2));
        model.addAttribute("suburbErrorMessage", locationErrorMessages.get(1));
        model.addAttribute("jobDueDateErrorMessage", jobErrorMessages.get(3));
        model.addAttribute("jobStartDateErrorMessage", jobErrorMessages.get(4));
    }
}
