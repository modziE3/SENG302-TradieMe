package nz.ac.canterbury.seng302.homehelper.controller.job;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
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
import java.text.ParseException;
import java.util.List;

/**
 * Controller for the post job form
 */
@Controller
public class PostJobFormController {
    Logger logger = LoggerFactory.getLogger(JobFormController.class);

    private final RenovationRecordService renovationRecordService;
    private final JobService jobService;
    private final UserService userService;

    @Autowired
    public PostJobFormController(RenovationRecordService renovationRecordService, JobService jobService, UserService userService) {
        this.renovationRecordService = renovationRecordService;
        this.jobService = jobService;
        this.userService = userService;
    }

    /**
     * Gets the post job form for a job that is wanted to be posted but cannot because of invalid job details
     * @param jobId ID of the job
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param principal the currently authenticated user
     * @return thymeleaf template for the post job form or error page if job not found
     */
    @GetMapping("/my-renovations/post-job")
    public String getPostJobForm(
            @RequestParam(name = "jobId") long jobId,
            Model model,
            Principal principal) {
        logger.info("GET /my-renovations/post-job");

        User user = userService.getUser(principal.getName());
        try {
            Job job = jobService.getJobById(jobId);

            model.addAttribute("jobId", jobId);
            model.addAttribute("jobName", job.getName());
            model.addAttribute("jobDescription", job.getDescription());
            model.addAttribute("descriptionLength", job.getDescription().codePointCount(0, job.getDescription().length()) + "/512");
            model.addAttribute("jobType", job.getType());
            model.addAttribute("jobDueDate", job.getDueDate());
            model.addAttribute("jobStartDate", job.getStartDate());
            model.addAttribute("renovationRecord", job.getRenovationRecord());

            try {
                jobService.validateJobBeforePosting(job);
            } catch (IllegalArgumentException e) {
                setErrorMessages(model, e.getMessage());
            }

            return "postJobTemplate";
        } catch (NullPointerException e) {
            logger.warn(e.getMessage());
            model.addAttribute("loggedIn", principal != null);
            model.addAttribute("user", user);
            model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
            return "error";
        }
    }

    /**
     * Gets the values posted in the post job form and validates them to determine if they meet the criteria for the job
     * to be posted. If so, the job posted status is set to true
     * @param jobId ID of the job
     * @param jobName Job name entered into the form
     * @param jobDescription Job description entered into the form
     * @param jobType Job type entered into the form
     * @param jobDueDate Job due date entered into the form
     * @param jobStartDate Job start date entered into the form
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param principal the currently authenticated user
     * @return redirect to job details page if values are valid, otherwise redirected to the post job form
     */
    @PostMapping("/my-renovations/post-job")
    public String editAndPostJob(
            @RequestParam(name = "jobId") long jobId,
            @RequestParam(name = "jobName") String jobName,
            @RequestParam(name = "jobDescription") String jobDescription,
            @RequestParam(name = "jobType") String jobType,
            @RequestParam(name = "jobDueDate") String jobDueDate,
            @RequestParam(name = "jobStartDate") String jobStartDate,
            Model model,
            Principal principal) {
        logger.info("POST /my-renovations/post-job");

        User user = userService.getUser(principal.getName());
        Job job = null;
        try {
            job = jobService.getJobById(jobId);

            Job validateJob = new Job(jobName, jobDescription, jobDueDate, jobStartDate);
            validateJob.setType(jobType);

            jobService.validateJobBeforePosting(validateJob);

            jobService.editJob(job, jobName, jobDescription, jobType, jobDueDate, jobStartDate);
            jobService.editJobPostStatus(job, true);

            return "redirect:/my-renovations/job-details?jobId=" + jobId;
        } catch (IllegalArgumentException | ParseException e) {
            model.addAttribute("jobId", jobId);
            model.addAttribute("jobName", jobName);
            model.addAttribute("jobDescription", jobDescription);
            model.addAttribute("descriptionLength", jobDescription.codePointCount(0, jobDescription.length()) + "/512");
            model.addAttribute("jobType", jobType);
            model.addAttribute("jobDueDate", jobDueDate);
            model.addAttribute("jobStartDate", jobStartDate);
            model.addAttribute("renovationRecord", job.getRenovationRecord());

            setErrorMessages(model, e.getMessage());

            return "postJobTemplate";
        } catch (NullPointerException e) {
            logger.warn(e.getMessage());
            model.addAttribute("loggedIn", principal != null);
            model.addAttribute("user", user);
            model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
            return "error";
        }
    }

    /**
     * This post is used to update the posted status of a job
     * @param jobId ID of the job
     * @param isPosted Boolean value that the job's posted status will be set to
     * @param request HttpServletRequest of session
     * @return the redirect back to the users last page
     */
    @PostMapping("/my-renovations/update-posted-status")
    public String updateJobPostedStatus(
            @RequestParam(name = "jobId") long jobId,
            @RequestParam(name = "posted") boolean isPosted,
            Model model,
            Principal principal,
            HttpServletRequest request) {
        logger.info("POST /my-renovations/update-posted-status");

        User user = userService.getUser(principal.getName());
        try {
            Job job = jobService.getJobById(jobId);

            if (isPosted) {
                jobService.validateJobBeforePosting(job);
            }

            userService.addJobToUsersMostRecent(userService.getUser(principal.getName()), job);

            jobService.editJobPostStatus(job, isPosted);
            return "redirect:" + request.getHeader("Referer");
        } catch (IllegalArgumentException e) {
            return "redirect:/my-renovations/post-job?jobId=" + jobId;
        } catch (NullPointerException e) {
            logger.warn(e.getMessage());
            model.addAttribute("loggedIn", principal != null);
            model.addAttribute("user", user);
            model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
            return "error";
        }
    }

    /**
     * Sets error messages for the add/edit job form
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param allErrorMessages String of all error messages wanting to be displayed
     */
    private void setErrorMessages(Model model, String allErrorMessages) {
        List<String> errorMessages = jobService.getErrorMessages(allErrorMessages);
        model.addAttribute("jobNameErrorMessage", errorMessages.get(0));
        model.addAttribute("jobDescriptionErrorMessage", errorMessages.get(1));
        model.addAttribute("jobTypeErrorMessage", errorMessages.get(2));
        model.addAttribute("jobDueDateErrorMessage", errorMessages.get(3));
        model.addAttribute("jobStartDateErrorMessage", errorMessages.get(4));
    }
}
