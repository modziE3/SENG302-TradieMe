package nz.ac.canterbury.seng302.homehelper.controller.job;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import org.apache.commons.text.StringEscapeUtils;
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
import java.util.*;

/**
 * Controller for the job form
 * This class links automatically with @link{JobService} and @link{RenovationRecordService}
 */
@Controller
public class JobFormController {
    Logger logger = LoggerFactory.getLogger(JobFormController.class);

    private final RenovationRecordService renovationRecordService;
    private final JobService jobService;

    @Autowired
    public JobFormController(RenovationRecordService renovationRecordService, JobService jobService) {
        this.renovationRecordService = renovationRecordService;
        this.jobService = jobService;
    }

    /**
     * Gets the form for creating a new job for a renovation record
     * @param recordId ID of the renovation record that the job will belong to
     * @param search Renovation record search information
     * @param fromCalendar Variable for if the form was accessed from the job calendar
     * @param fromJob Variable for if the form was accessed from the job details page
     * @param principal the currently authenticated user
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @return thymeleaf jobFormTemplate
     */
    @GetMapping("/my-renovations/create-job")
    public String getCreateJobForm(
            @RequestParam(name = "recordId") long recordId,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "fromCalendar", required = false, defaultValue = "false") boolean fromCalendar,
            @RequestParam(name = "fromJob", required = false, defaultValue = "false") boolean fromJob,
            Principal principal,
            Model model) {
        logger.info("GET /my-renovations/create-job");

        try {
            RenovationRecord record = renovationRecordService.getAndAuthenticateRecord(recordId, principal.getName());
            model.addAttribute("record", record);
            model.addAttribute("renoRooms", "[" + String.join("`", record.getRooms().stream().map(Room::getName).toList()) + "]");
            model.addAttribute("descriptionLength", "0/512");
            model.addAttribute("search", search);
            model.addAttribute("fromCalendar", fromCalendar);
            model.addAttribute("fromJob", fromJob);
            return "jobFormTemplate";
        } catch (NullPointerException e) {
            logger.warn(e.getMessage());
            model.addAttribute("loggedIn", principal != null);
            model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
            return "error";
        }
    }

    /**
     * Posts the name, description, due date and selected renovation record rooms entered into the form
     * and creates a new job using them
     * @param recordId ID of the renovation record the job will belong to
     * @param jobName Job name entered into the form
     * @param jobDescription Job description entered into the form
     * @param jobType Job type entered into the form
     * @param jobDueDate Job due date entered into the form
     * @param jobStartDate Job start date entered into the form
     * @param selectedRooms Room names selected in the form
     * @param search Renovation record search information
     * @param fromCalendar Variable for if the form was accessed from the job calendar
     * @param fromJob Variable for if the form was accessed from the job details page
     * @param principal the currently authenticated user
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @return thymeleaf jobFormTemplate
     */
    @PostMapping("/my-renovations/create-job")
    public String postNewJob(
            @RequestParam(name = "recordId") long recordId,
            @RequestParam(name = "newJobName") String jobName,
            @RequestParam(name = "jobDescription") String jobDescription,
            @RequestParam(name = "jobType") String jobType,
            @RequestParam(name = "jobDueDate") String jobDueDate,
            @RequestParam(name = "jobStartDate") String jobStartDate,
            @RequestParam(name = "selectedRooms", required = false) String selectedRooms,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "fromCalendar", required = false, defaultValue = "false") boolean fromCalendar,
            @RequestParam(name = "fromJob", required = false, defaultValue = "false") boolean fromJob,
            Principal principal,
            Model model) {
        logger.info("POST /my-renovations/create-job");

        RenovationRecord record = renovationRecordService.getAndAuthenticateRecord(recordId, principal.getName());
        try {
            Job job = new Job(jobName, jobDescription, jobDueDate, jobStartDate);
            job.setType(jobType);
            jobService.validateJob(job);

            if (selectedRooms != null && !selectedRooms.isEmpty()) {
                List<String> jobRoomNamesList = splitRoomNamesString(selectedRooms);
                jobService.addJobRooms(record, job, jobRoomNamesList);
            }

            job.setRenovationRecord(record);
            job = jobService.addJob(job);

            if (fromCalendar) {
                return "redirect:/my-renovations/details?recordId="+record.getId() +"&search="+search;
            } else {
                return "redirect:/my-renovations/job-details?jobId="+job.getId()+"&search="+search;
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("record", record);
            model.addAttribute("renoRooms", "[" + String.join("`",
                    record.getRooms().stream().map(Room::getName).toList()) + "]");
            model.addAttribute("newJobName", jobName);
            model.addAttribute("jobDescription", jobDescription);
            model.addAttribute("descriptionLength", jobDescription.codePointCount(0, jobDescription.length())  + "/512");
            model.addAttribute("jobType", jobType);
            model.addAttribute("jobDueDate", jobDueDate);
            model.addAttribute("jobStartDate", jobStartDate);
            model.addAttribute("jobRooms", selectedRooms);
            model.addAttribute("search", search);
            model.addAttribute("fromCalendar", fromCalendar);
            model.addAttribute("fromJob", fromJob);

            setErrorMessages(model, e.getMessage());

            return "jobFormTemplate";
        } catch (NullPointerException e) {
            logger.warn(e.getMessage());
            model.addAttribute("loggedIn", principal != null);
            model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
            return "error";
        }
    }

    /**
     * Gets the details of a renovation record job and displays them in the input fields of an edit form
     * @param recordId ID of the renovation record
     * @param jobId ID of the job
     * @param search Renovation record search information
     * @param fromCalendar Variable for if the form was accessed from the job calendar
     * @param fromJob Variable for if the form was accessed from the job details page
     * @param fromWidget Variable for if the form was accessed from the home page calendar widget
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param principal the currently authenticated user
     * @return thymeleaf jobFormTemplate
     */
    @GetMapping("/my-renovations/edit-job")
    public String getEditJobForm(
            @RequestParam(name = "recordId") long recordId,
            @RequestParam(name = "jobId") long jobId,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "fromCalendar", required = false, defaultValue = "false") boolean fromCalendar,
            @RequestParam(name = "fromJob", required = false, defaultValue = "false") boolean fromJob,
            @RequestParam(name = "fromWidget", required = false, defaultValue = "false") boolean fromWidget,
            Model model,
            Principal principal) {
        logger.info("GET /my-renovations/edit-job");

        try {
            RenovationRecord record = renovationRecordService.getAndAuthenticateRecord(recordId, principal.getName());
            Job job = jobService.getAndAuthenticateJob(jobId, record);
            model.addAttribute("record", record);
            model.addAttribute("job", job);
            model.addAttribute("jobIcon", job.getIcon());
            model.addAttribute("newJobName", job.getName());
            model.addAttribute("jobDescription", job.getDescription());
            model.addAttribute("descriptionLength", job.getDescription().codePointCount(0, job.getDescription().length()) + "/512");
            model.addAttribute("jobType", job.getType());
            model.addAttribute("jobDueDate", job.getDueDate());
            model.addAttribute("jobStartDate", job.getStartDate());
            model.addAttribute("renoRooms", "[" + String.join("`", record.getRooms().stream().map(Room::getName).toList()) + "]");
            model.addAttribute("jobRooms", job.getRooms().stream().map(Room::getName).toList());
            model.addAttribute("mode", "edit");
            model.addAttribute("search", search);
            model.addAttribute("fromCalendar", fromCalendar);
            model.addAttribute("fromWidget", fromWidget);
            model.addAttribute("fromJob", fromJob);

            return "jobFormTemplate";
        } catch (NullPointerException e) {
            logger.warn(e.getMessage());
            model.addAttribute("loggedIn", principal != null);
            model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
            return "error";
        }
    }

    /**
     * Posts the name, description, due date, and selected rooms entered into the edit form
     * and changes the details of the renovation record job
     * @param recordId ID of the renovation record
     * @param jobId ID of the job
     * @param newJobName Name of the job after being updated
     * @param jobDescription Job description entered into the form
     * @param jobType Job type entered into the form
     * @param jobDueDate Job due date entered into the form
     * @param jobStartDate Job start date entered into the form
     * @param selectedRooms Room names selected in the form
     * @param search Renovation record search information
     * @param fromCalendar Variable for if the form was accessed from the job calendar
     * @param fromJob Variable for if the form was accessed from the job details page
     * @param fromWidget Variable for if the form was accessed from the home page calendar widget
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param principal the currently authenticated user
     * @return thymeleaf jobFormTemplate
     */
    @PostMapping("/my-renovations/edit-job")
    public String postEditJob(
            @RequestParam(name = "recordId") long recordId,
            @RequestParam(name = "jobId") long jobId,
            @RequestParam(name = "newJobName") String newJobName,
            @RequestParam(name = "jobDescription") String jobDescription,
            @RequestParam(name = "jobType") String jobType,
            @RequestParam(name = "jobDueDate") String jobDueDate,
            @RequestParam(name = "jobStartDate") String jobStartDate,
            @RequestParam(name = "selectedRooms", required = false) String selectedRooms,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "fromCalendar", required = false, defaultValue = "false") boolean fromCalendar,
            @RequestParam(name = "fromJob", required = false, defaultValue = "false") boolean fromJob,
            @RequestParam(name = "fromWidget", required = false, defaultValue = "false") boolean fromWidget,
            Model model,
            Principal principal) {
        logger.info("POST /my-renovations/edit-job");
        RenovationRecord record = null;
        Job job = null;
        try {
            record = renovationRecordService.getAndAuthenticateRecord(recordId, principal.getName());
            job = jobService.getAndAuthenticateJob(jobId, record);

            jobService.editJob(job, newJobName, jobDescription, jobType, jobDueDate, jobStartDate);

            if (selectedRooms != null && !selectedRooms.isEmpty()) {
                List<String> jobRoomNamesList = splitRoomNamesString(selectedRooms);
                jobService.editJobRooms(record, job, jobRoomNamesList);
            }

            if (fromJob) {
                return "redirect:/my-renovations/job-details?jobId="+jobId+"&search="+search;
            }
            else if (fromWidget) {
                return "redirect:/home";
            }
            else {
                return "redirect:/my-renovations/details?recordId="+record.getId()
                        +"&search="+search;
            }
        } catch (IllegalArgumentException | ParseException e) {
            model.addAttribute("record", record);
            model.addAttribute("job", job);
            model.addAttribute("jobIcon", job.getIcon());
            model.addAttribute("newJobName", newJobName);
            model.addAttribute("jobDescription", jobDescription);
            model.addAttribute("descriptionLength", jobDescription.codePointCount(0, jobDescription.length()) + "/512");
            model.addAttribute("jobType", jobType);
            model.addAttribute("jobDueDate", jobDueDate);
            model.addAttribute("jobStartDate", jobStartDate);
            model.addAttribute("renoRooms", "[" + String.join("`",
                    record.getRooms().stream().map(Room::getName).toList()) + "]");
            model.addAttribute("jobRooms", selectedRooms);
            model.addAttribute("mode", "edit");
            model.addAttribute("search", search);
            model.addAttribute("fromCalendar", fromCalendar);
            model.addAttribute("fromJob", fromJob);
            model.addAttribute("fromWidget", fromWidget);

            setErrorMessages(model, e.getMessage());

            return "jobFormTemplate";
        } catch (NullPointerException e) {
            logger.warn(e.getMessage());
            model.addAttribute("loggedIn", principal != null);
            model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
            return "error";
        }
    }

    /**
     * Splits string of room names into a list
     * @param rooms string of room names
     * @return list of room name strings
     */
    private List<String> splitRoomNamesString(String rooms) {
        String roomsDecoded = StringEscapeUtils.unescapeHtml4(rooms); //ChatGpt code
        String roomsString = roomsDecoded.replaceAll("^\"|\"$", "").trim(); //Chatgpt code
        List<String> roomNamesList = Arrays.stream(roomsString.split("`"))
                .filter(room -> !room.isBlank())
                .toList();
        return roomNamesList;
    }

    /**
     * Sets error messages for the add/edit job form
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param allErrorMessages String of all error messages wanting to be displayed
     */
    private void setErrorMessages(Model model, String allErrorMessages) {
        List<String> errorMessages = jobService.getErrorMessages(allErrorMessages);
        model.addAttribute("newJobNameErrorMessage", errorMessages.get(0));
        model.addAttribute("jobDescriptionErrorMessage", errorMessages.get(1));
        model.addAttribute("jobTypeErrorMessage", errorMessages.get(2));
        model.addAttribute("jobDueDateErrorMessage", errorMessages.get(3));
        model.addAttribute("jobStartDateErrorMessage", errorMessages.get(4));
    }
}
