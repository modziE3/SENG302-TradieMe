package nz.ac.canterbury.seng302.homehelper.controller.renovations;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.service.*;
import nz.ac.canterbury.seng302.homehelper.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.*;

/**
 * Controller for the renovation record details page
 */
@Controller
public class RenovationDetailsController {
    Logger logger = LoggerFactory.getLogger(RenovationDetailsController.class);

    private final RenovationRecordService renovationRecordService;
    private final JobService jobService;
    private final UserService userService;
    private final TagService tagService;
    private final ExpenseService expenseService;
    private final ImageService imageService;

    @Autowired
    public RenovationDetailsController(RenovationRecordService renovationRecordService, UserService userService,
                                       JobService jobService, TagService tagService, ExpenseService expenseService,
                                       ImageService imageService) {
        this.renovationRecordService = renovationRecordService;
        this.jobService = jobService;
        this.userService = userService;
        this.tagService = tagService;
        this.expenseService = expenseService;
        this.imageService = imageService;
    }

    /**
     * Gets the details of the renovation record with the associated ID and name and
     * displays the record's details on the page
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param principal the currently authenticated user.
     * @param recordId ID of the renovation record
     * @param jobPage Current job page the user is on
     * @return thymeleaf renovationDetailsTemplate
     */
    @GetMapping("/my-renovations/details")
    public String getRenovationDetails(
            @RequestParam(name = "recordId") long recordId,
            @RequestParam(name = "job-page", defaultValue = "1") Integer jobPage,
            @RequestParam(name = "expensesPage", defaultValue = "1") Integer expensePage,
            @RequestParam(name = "search", defaultValue = "false-0--") String search,
            @RequestParam(name = "filter", defaultValue = "false", required = false) String filter,
            Model model,
            Principal principal) {
        logger.info("GET /my-renovations/details");

        User user = userService.getUser(principal.getName());
        try {
            boolean publicUser = false;
            RenovationRecord record = renovationRecordService.getRecordById(recordId);
            record = (Objects.equals(record.getUserEmail(), principal.getName()) || record.getIsPublic()) ? record : null;
            if (record != null && record.getIsPublic() && !record.getUserEmail().equals(principal.getName())) {
                publicUser = true;
            }


            userService.updateRecentRenovations(user, record);
            Slice<Job> jobSlice = jobService.getJobPages(jobPage, record, filter);
            List<Integer> jobPages = jobService.getPageList(jobPage, record, filter);

            if ((jobPages.isEmpty() && jobPage > jobPages.size() + 1) ||
                    (!jobPages.isEmpty() && jobPage > jobPages.get(jobPages.size() - 1))) {
                throw new NullPointerException("Page number is above the range of available pages");
            }
            List<Expense> expenses = new ArrayList<>();
            record.getJobs().forEach(job -> expenses.addAll(job.getExpenses()));
            double totalCost = expenses.stream().mapToDouble(e -> Double.parseDouble(e.getCost()))
                    .sum();

            Slice<Expense> expenseSlice = expenseService.getExpensePagesFromRecord(expensePage, record);
            List<Integer> expensePages = expenseService.getPageList(expensePage, expenses);

            if ((expensePages.isEmpty() && expensePage > expensePages.size() + 1) ||
                    (!expensePages.isEmpty() && expensePage > expensePages.get(expensePages.size() - 1))) {
                throw new NullPointerException("Page number is above the range of available pages");
            }

            // back button
            model.addAttribute("publicUser", publicUser);
            model.addAttribute("searched", search.split("-", 4)[0]);
            model.addAttribute("searchPage", search.split("-", 4)[1]);
            model.addAttribute("search", ((Objects.equals(search.split("-", 4)[2], "null"))) ? "" : search.split("-", 4)[2]);
            model.addAttribute("searchTags", search.split("-", 4)[3]);

            // reno info
            model.addAttribute("record", record);
            model.addAttribute("recordId", record.getId());
            model.addAttribute("renoDescription", record.getDescription());
            model.addAttribute("isPublic", record.getIsPublic());
            model.addAttribute("rooms", record.getRooms());
            model.addAttribute("tags", record.getTags());
            model.addAttribute("tagNames", "["+String.join("`", tagService.getAllTags().stream().map(Tag::getName).toList())+"]");
            model.addAttribute("userIsOwner", record.getUserEmail().equals(principal.getName()));
            model.addAttribute("loggedIn", principal != null);

            // job info
            model.addAttribute("jobs", jobSlice.getContent());
            model.addAttribute("jobPages", jobPages);
            model.addAttribute("jobPage", jobPage);
            model.addAttribute("lastJobPage", jobService.getNumPages(record, filter));
            model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
            model.addAttribute("icons", JobService.ICON_LIST);
            model.addAttribute("filter", filter);

            // expenses info
            model.addAttribute("totalExpenses", expenses.size());
            model.addAttribute("totalCost", totalCost);
            model.addAttribute("expenses", expenseSlice);
            model.addAttribute("expensePages", expensePages);
            model.addAttribute("expensePage", expensePage);
            model.addAttribute("lastExpensePage", expenseService.getNumPages(expenses));

            // calendar info
            List<Job> jobs = record.getJobs();
            model.addAttribute("jobIds", jobs.stream().map(Job::getId).toList());
            model.addAttribute("jobNames", jobs.stream().map(Job::getName).toList());
            model.addAttribute("jobStartDates", jobService.convertJobStartDatesForCalendar(jobs));
            model.addAttribute("jobDueDates", jobService.convertJobDueDatesForCalendar(jobs));
            model.addAttribute("jobStatuses", jobs.stream().map(Job::getStatus).toList());
            model.addAttribute("jobWasModified", jobService.jobsWereModified(jobs));
            model.addAttribute("isQuotedJob", jobs.stream().map(j -> false).toList());

            model.addAttribute("user", user);
            if (user.getProfilePicture() != null) {
                model.addAttribute("profileImage", "/profileImages/" + user.getProfilePicture());
            }

            return "renovationDetailsTemplate";
        } catch (IllegalArgumentException | NullPointerException e) {
            logger.warn(e.getMessage());
            model.addAttribute("loggedIn", principal != null);
            model.addAttribute("user", user);
            model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
            return "error";
        }
    }

    /**
     * Posts the value of the job state filter that will be applied to the jobs listed in the jobs tab
     * @param recordId ID of the renovation record
     * @param search whether the user came to this page from the search page
     * @param currentPageNumber Current pagination page number on the jobs tab
     * @param filter job state the list of jobs will be filtered by
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param principal The currently authenticated user
     * @return redirect to the jobs tab with the job state filter applied
     */
    @PostMapping("/my-renovations/details/filter-state")
    public String postFilterJobs(
            @RequestParam(name = "recordId") long recordId,
            @RequestParam(name = "search", defaultValue = "false-0--") String search,
            @RequestParam(name = "job-page", defaultValue = "1") Integer currentPageNumber,
            @RequestParam(name = "filter") String filter,
            Model model,
            Principal principal) {
        User user = userService.getUser(principal.getName());
        try {
            RenovationRecord record = renovationRecordService.getRecordById(recordId);
            return "redirect:/my-renovations/details?recordId="+recordId
                    +"&job-page=1"
                    +"&search="+search
                    +"&filter="+filter;
        } catch (Exception e) {
            logger.warn(e.getMessage());
            model.addAttribute("loggedIn", principal != null);
            model.addAttribute("user", user);
            model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
            return "error";
        }

    }

    /**
     * Posts the job page number the user has inputted, validates the number
     * then redirects to the corresponding job page
     * @param recordId ID of the renovation record
     * @param currentJobPageNumber Current job page the user is on
     * @param newJobPageNumber Job page number the user wants to go to
     * @param currentExpensePageNumber Current expense page the user is on
     * @param newExpensePageNumber expense page number the user wants to go to
     * @param search whether the user came to this page from the search page
     * @param filter any filters the user has on while looking at the jobs
     * @param principal The currently authenticated user
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @return thymeleaf renovationDetailsTemplate
     */
    @PostMapping("/my-renovations/details")
    public String postJobPageNumber(
            @RequestParam(name = "recordId") long recordId,
            @RequestParam(name = "job-page", defaultValue = "1") Integer currentJobPageNumber,
            @RequestParam(name = "jobPageNumber") Integer newJobPageNumber,
            @RequestParam(name = "expensesPage", defaultValue = "1") Integer currentExpensePageNumber,
            @RequestParam(name = "expensePageNumber") Integer newExpensePageNumber,
            @RequestParam(name = "search", defaultValue = "false-0--") String search,
            @RequestParam(name = "filter", defaultValue = "false", required = false) String filter,
            Model model,
            Principal principal) {
        logger.info("POST /my-renovations/details");

        User user = userService.getUser(principal.getName());
        try {
            RenovationRecord record = renovationRecordService.getRecordById(recordId);
            List<Integer> pages = jobService.getPageList(currentJobPageNumber, record, filter);

            if (jobService.pageNumberIsInJobPageList(pages, newJobPageNumber)) {
                currentJobPageNumber = newJobPageNumber;
            }

            List<Expense> expenses = new ArrayList<>();
            record.getJobs().forEach(job -> expenses.addAll(job.getExpenses()));
            List<Integer> expensePages = expenseService.getPageList(currentExpensePageNumber, expenses);

            if (expenseService.pageNumberIsInExpensePageList(expensePages, newExpensePageNumber)) {
                currentExpensePageNumber = newExpensePageNumber;
            }


            return "redirect:/my-renovations/details?recordId="+record.getId()
                    +"&job-page="+currentJobPageNumber
                    +"&expensesPage="+currentExpensePageNumber
                    +"&search="+search;
        } catch (Exception e) {
            logger.warn(e.getMessage());
            model.addAttribute("loggedIn", principal != null);
            model.addAttribute("user", user);
            model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
            return "error";
        }
    }

    /**
     * Posts the value of the public status checkbox in the renovation details page
     * and sets this value to the public status attribute of the renovation record
     * @param recordId ID of the renovation record that will have its public status changed
     * @param isPublic Boolean value that the record's public status will be set to
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param principal the users authentication
     * @param request link requester to find the prev link
     * @return thymeleaf template
     */
    @PostMapping("/my-renovations/details/update-public-status")
    public String updatePublicStatus(
            @RequestParam(name = "recordId") long recordId,
            @RequestParam(name = "public") boolean isPublic,
            Model model,
            Principal principal,
            HttpServletRequest request) {
        logger.info("POST /my-renovations/details/update-public-status");

        User user = userService.getUser(principal.getName());
        try {
            RenovationRecord record = renovationRecordService.getRecordById(recordId);
            record = Objects.equals(record.getUserEmail(), principal.getName()) ? record : null;

            renovationRecordService.editRenovationRecordPublicStatus(record, isPublic);
            return "redirect:" + request.getHeader("Referer");
        } catch (NullPointerException e) {
            logger.warn(e.getMessage());
            model.addAttribute("loggedIn", principal != null);
            model.addAttribute("user", user);
            model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
            return "error";
        }
    }

    /**
     * This post is used to add a tag to a renovation record.
     * If the tag name is invalid, an error message is displayed when the page is redirected.
     * Otherwise, the tag gets added to the record
     * @param recordId the renovation that has the job
     * @param tagName the name of the tag
     * @param jobPage the job page the user is currently on
     * @param search search parameters
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param principal the users authentication
     * @param request link requester to find the prev link
     * @param redirectAttributes attributes that are set after the page is redirected
     * @return redirect to details page of renovation record
     */
    @PostMapping("/my-renovations/details/add-tag")
    public String addTag(@RequestParam(name = "recordId") long recordId,
                         @RequestParam(name = "newTag") String tagName,
                         @RequestParam(name = "job-page", defaultValue = "1") Integer jobPage,
                         @RequestParam(name = "search", required = false) String search,
                         Model model,
                         Principal principal,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        logger.info("POST /my-renovations/details/add-tag");

        User user = userService.getUser(principal.getName());
        try {
            RenovationRecord record = renovationRecordService.getRecordById(recordId);
            if (!record.getUserEmail().equals(principal.getName())) {
                return "redirect:" + request.getHeader("Referer");
            }

            tagName = tagName.toLowerCase();
            String tagError = tagService.validateTag(record, tagName);
            if (!tagError.isEmpty()) {
                redirectAttributes.addFlashAttribute("tagErrorMessage", tagError);
                redirectAttributes.addFlashAttribute("tagName", tagName);
                redirectAttributes.addFlashAttribute("showAddTagField", true);
            } else {
                Tag oldTag = tagService.getTagByName(tagName);
                if (oldTag != null) {
                    oldTag.addRenovationRecord(record);
                    tagService.addTag(oldTag);
                    renovationRecordService.editRenovationRecordTags(record, oldTag);
                } else {
                    Tag tag = new Tag(tagName);
                    record.addTag(tag);
                    tag.addRenovationRecord(record);
                    tagService.addTag(tag);
                }
            }

            return "redirect:/my-renovations/details?recordId="+record.getId()
                    +"&job-page="+jobPage
                    +"&search="+search;
        } catch (Exception e) {
            logger.warn(e.getMessage());
            model.addAttribute("loggedIn", principal != null);
            model.addAttribute("user", user);
            model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
            return "error";
        }
    }

    /**
     * Used to remove tags from renovation records. this only actually removes the tag if you are the owner.
     * regardless of the owner you are redirected back to the page you came from.
     * @param recordId of the record to be updated.
     * @param tagName name of tag to be removed.
     * @param principal user details of logged-in user.
     * @param request HttpServletRequest of session.
     * @return the redirect back to the users last page.
     */
    @PostMapping("/my-renovations/details/remove-tag")
    public String removeTag(@RequestParam(name = "recordId") long recordId,
                         @RequestParam(name = "tagNameRemove") String tagName,
                         Principal principal,
                         HttpServletRequest request) {
        logger.info("POST /my-renovations/details/remove-tag");
        RenovationRecord record = renovationRecordService.getRecordById(recordId);
        if (record.getUserEmail().equals(principal.getName())) {
            Tag tag = tagService.getTagByName(tagName.toLowerCase());
            if (tag != null) {
                tag.getRenovationRecords().remove(record);
                record.getTags().remove(tag);
                if (tag.getRenovationRecords().isEmpty()) {
                    tagService.deleteTag(tag);
                } else {
                    tagService.addTag(tag);
                }
                renovationRecordService.addRenovationRecord(record);
            }
        }
        return "redirect:" + request.getHeader("Referer");
    }

    /**
     * Adds a submitted image to a renovation record room if it is a valid image
     * @param recordId ID of the renovation record
     * @param roomId ID of the room
     * @param submittedFile The file which the user has submitted
     * @param redirectAttributes Attributes that are set after the page is redirected
     * @param request Link requester to find the prev link
     * @return redirect to the renovation/job details page
     */
    @PostMapping("/add-room-image")
    public String addRoomImage(
            @RequestParam(name = "recordId") Long recordId,
            @RequestParam(name = "roomId") Long roomId,
            @RequestParam(name = "submittedFile") MultipartFile submittedFile,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        logger.info("POST /add-room-image");

        RenovationRecord record = renovationRecordService.getRecordById(recordId);
        Room room = record.getRooms().stream().filter(r -> r.getId().equals(roomId)).findFirst().get();

        try {
            imageService.validateImage(submittedFile);
            imageService.saveRoomImage(submittedFile, room);
            renovationRecordService.addRenovationRecord(record);
        } catch (IOException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("fileErrorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("roomId", roomId);
        }
        return "redirect:" + request.getHeader("Referer");
    }
}