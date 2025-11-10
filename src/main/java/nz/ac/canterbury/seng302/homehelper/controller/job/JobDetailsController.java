package nz.ac.canterbury.seng302.homehelper.controller.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.controller.renovations.RenovationDetailsController;
import nz.ac.canterbury.seng302.homehelper.entity.*;
import nz.ac.canterbury.seng302.homehelper.entity.dto.TradieRating;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.Instant;
import java.util.*;

@Controller
public class JobDetailsController {
    private final ExpenseService expenseService;
    private final ValidationService validationService;
    Logger logger = LoggerFactory.getLogger(RenovationDetailsController.class);

    private final JobService jobService;
    private final RenovationRecordService renovationRecordService;
    private final UserService userService;
    private final QuoteService quoteService;
    private final EmailService emailService;
    private final TaskScheduler taskScheduler;
    private final RatingService ratingService;

    @Autowired
    public JobDetailsController(JobService jobService, RenovationRecordService renovationRecordService, UserService userService,
                                ExpenseService expenseService, QuoteService quoteService, EmailService emailService, TaskScheduler taskScheduler,
                                ValidationService validationService, RatingService ratingService) {
        this.jobService = jobService;
        this.renovationRecordService = renovationRecordService;
        this.userService = userService;
        this.expenseService = expenseService;
        this.quoteService = quoteService;
        this.emailService = emailService;
        this.taskScheduler = taskScheduler;
        this.validationService = validationService;
        this.ratingService = ratingService;
    }

    /**
     * Gets the details about a renovation record job and displays the job's details
     * @param jobId ID of the job
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param principal the currently authenticated user
     * @return template for job details page
     */
    @GetMapping("/my-renovations/job-details")
    public String getJobDetailsPage(
            @RequestParam(name = "jobId") long jobId,
            @RequestParam(name = "expensesPage", defaultValue = "1") Integer expensePage,
            @RequestParam(name = "quotesPage", defaultValue = "1") Integer quotesPage,
            @RequestParam(name = "fromSearch", required = false) boolean fromSearch,
            @RequestParam(name = "fromCalendar", required = false) boolean fromCalendar,
            @RequestParam(name = "fromWidget", required = false) boolean fromWidget,
            Model model,
            Principal principal) {
        logger.info("GET /my-renovations/job-details");

        String userEmail = principal.getName();
        User user = userService.getUser(userEmail);
        try {
            boolean publicUser = false;
            Job job = jobService.getJobById(jobId);
            List<Expense> allExpenses = job.getExpenses();
            double totalCost = allExpenses.stream().mapToDouble(e -> Double.parseDouble(e.getCost()))
                            .sum();
            if (!job.getRenovationRecord().getUserEmail().equals(principal.getName())) {
                publicUser = true;
            }

            if (quoteService.checkIfAlreadyQuoted(user, jobService.getJobById(jobId))) {
                model.addAttribute("quoted", true);
            } else {
                model.addAttribute("quoted", false);
            }
            userService.addJobToUsersMostRecent(user, job);
            model.addAttribute("job", job);
            model.addAttribute("jobId", job.getId());
            model.addAttribute("renovationRecord", job.getRenovationRecord());
            model.addAttribute("publicUser", publicUser);
            model.addAttribute("icons", JobService.ICON_LIST);
            model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
            model.addAttribute("totalExpenses", allExpenses.size());
            model.addAttribute("totalCost", totalCost);

            expensePage = expensePage == null ? 1 : expensePage;
            model.addAttribute("expensesResults", PaginationUtil.getPageNumbers(expenseService.getExpensesByJobId(job.getId()).size(), 9));
            model.addAttribute("expensesPage", expensePage);
            model.addAttribute("lastPageExpenses", PaginationUtil.getLastPageNumber(expenseService.getExpensesByJobId(job.getId()).size(), 9));
            model.addAttribute("expenses", PaginationUtil.getPage(expenseService.getExpensesByJobId(job.getId()), expensePage, 9));

            quotesPage = quotesPage == null ? 1 : quotesPage;
            model.addAttribute("quotesResults", PaginationUtil.getPageNumbers(quoteService.getQuotesByJobId(job.getId()).size(), 12));
            model.addAttribute("quotesPage", quotesPage);
            model.addAttribute("lastPageQuotes", PaginationUtil.getLastPageNumber(quoteService.getQuotesByJobId(job.getId()).size(), 12));
            model.addAttribute("receivedQuotes", PaginationUtil.getPage(quoteService.getQuotesByJobId(job.getId()), quotesPage, 12));

            model.addAttribute("loggedIn", principal != null);
            model.addAttribute("fromSearch", fromSearch);
            model.addAttribute("fromCalendar", fromCalendar);
            model.addAttribute("fromWidget", fromWidget);
            model.addAttribute("user", user);
            if (user.getProfilePicture() != null) {
                model.addAttribute("profileImage", "/profileImages/" + user.getProfilePicture());
            }
            return "jobDetailsTemplate";
        } catch (NullPointerException e) {
            logger.warn(e.getMessage());
            model.addAttribute("loggedIn", principal != null);
            model.addAttribute("user", user);
            model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
            return "error";
        }
    }

    /**
     * This post is used to update an icon of a job. After the icon is updated then
     * the user is sent back to the page they were on.
     * @param recordId the renovation that has the job. the job is not updated if the
     *               job does not belong to this reno.
     * @param jobId the id of the job.
     * @param selectedIcon string representing the html icon class
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param principal the users authentication
     * @param request link requester to find the prev link
     * @return thymeleaf template
     */
    @PostMapping("/my-renovations/update-icon")
    public String updateJobIcon(
            @RequestParam(name = "recordId") long recordId,
            @RequestParam("jobId") Long jobId,
            @RequestParam("selectedIcon") String selectedIcon,
            Principal principal,
            Model model,
            HttpServletRequest request) {
        logger.info("POST /my-renovations/update-icon");

        User user = userService.getUser(principal.getName());
        try {
            RenovationRecord record = renovationRecordService.getRecordById(recordId);
            record = Objects.equals(record.getUserEmail(), principal.getName()) ? record : null;

            if (record.getJobs().stream().filter(t -> Objects.equals(t.getId(), jobId)).findFirst().orElse(null) == null) {
                throw new NullPointerException("No job found with id " + jobId);
            }

            selectedIcon = selectedIcon.isEmpty() ? "" : selectedIcon.substring(1);
            jobService.editIcon(jobId, selectedIcon.isEmpty() ? null : selectedIcon);

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
     * This post is used to update the status of a job
     * @param jobId ID of the job
     * @param newJobStatus new status the job will be set to
     * @param request HttpServletRequest of session
     * @return the redirect back to the users last page
     */
    @PostMapping("/my-renovations/update-job-status")
    public String updateJobStatus(
            @RequestParam(name = "jobId") long jobId,
            @RequestParam(name = "newJobStatus") String newJobStatus,
            Principal principal,
            HttpServletRequest request, RedirectAttributes redirectAttributes) {
        logger.info("POST /my-renovations/update-job-state");

        Job job = jobService.getJobById(jobId);
        jobService.editJobStatus(job, newJobStatus);
        userService.addJobToUsersMostRecent(userService.getUser(principal.getName()), job);

        if (newJobStatus.equals("Completed")) {
            List<Quote> acceptedQuotes = quoteService.getAcceptedQuotes(job.getId());
            Set<User> users = new HashSet<>();
            for (Quote quote : acceptedQuotes) {
                if (!quote.getRated()) {
                    users.add(quote.getUser());
                }
            }

            if (!users.isEmpty()) {
                redirectAttributes.addFlashAttribute("tradies", users);
                redirectAttributes.addFlashAttribute("showCompletedModal", true);
                redirectAttributes.addFlashAttribute("completedJobId", jobId);
            }

        }

        return "redirect:" + request.getHeader("Referer");
    }

    /**
     * Posts the page numbers entered into the pagination input bars for expenses and quotes
     * and redirects to the pagination page of either expenses or quotes depending on which input
     * bar was used to change a pagination page
     * @param jobId ID of the job
     * @param fromSearch Boolean variable for if the user found the job through searching
     * @param expensesPage Current pagination page the expenses tab is on
     * @param quotesPage Current pagination page the quotes tab is on
     * @param expensesPageNumber Number entered into the expenses pagination input bar
     * @param quotesPageNumber Number entered into the quotes pagination input bar
     * @return redirect to the correct pagination pages for expenses and quotes
     */
    @PostMapping("/my-renovations/job-details")
    public String postExpensePageNumber(
            @RequestParam(name = "jobId") long jobId,
            @RequestParam(name = "fromSearch") boolean fromSearch,
            @RequestParam(name = "expensesPage", required = false) Integer expensesPage,
            @RequestParam(name = "quotesPage", required = false) Integer quotesPage,
            @RequestParam(name = "expensesPageNumber", required = false) Integer expensesPageNumber,
            @RequestParam(name = "quotesPageNumber", required = false) Integer quotesPageNumber) {
        logger.info("POST /my-renovations/job-details");

        if (expensesPageNumber != null) {
            return "redirect:/my-renovations/job-details?jobId="
                    +jobId
                    +"&fromSearch=" +fromSearch
                    +"&expensesPage="+expensesPageNumber
                    +"&quotesPage="+quotesPage;
        } else {
            return "redirect:/my-renovations/job-details?jobId="
                    +jobId
                    +"&fromSearch=" +fromSearch
                    +"&expensesPage="+expensesPage
                    +"&quotesPage="+quotesPageNumber;
        }
    }

    /**
     * Post mapping for accepting a quote
     * @param quoteId The id of the quote which is getting accepted
     * @param retract A boolean for if a quotes job should be retracted or not
     * @param jobId the id of the job to which the quote belongs to
     * @param fromSearch boolean stating if the job details page was accessed from the search page
     * @param expensesPage the current page on the expenses tab
     * @param quotesPage the current page on the quotes tab
     * @param principal the currently authenticated user
     * @return Redirects user back to the Job Details page
     */
    @PostMapping("/my-renovations/job-details/accept-quote")
    public String acceptQuoteJobDetails(
            @RequestParam(name = "quoteId") long quoteId,
            @RequestParam(name = "retract", defaultValue = "false") boolean retract,
            @RequestParam(name = "transfer", defaultValue = "false") boolean transfer,
            @RequestParam(name = "jobId") long jobId,
            @RequestParam(name = "fromSearch") boolean fromSearch,
            @RequestParam(name = "expensesPage", required = false) Integer expensesPage,
            @RequestParam(name = "quotesPage", required = false) Integer quotesPage,
            Principal principal) {
        Quote quote = quoteService.findQuoteById(quoteId);
        Job job = quote.getJob();
        if (!Objects.equals(principal.getName(), job.getRenovationRecord().getUserEmail())) {
            return "error";
        }
        if (retract) {
            job.setIsPosted(false);
            jobService.addJob(job);
        }
        quote.setStatus("Accepted");
        quoteService.addQuote(quote);
        User user = quote.getUser();
        String email = user.getEmail();
        taskScheduler.schedule(
                () -> emailService.sendQuoteAcceptedEmail(email, job),
                Instant.now()
        );
        if (transfer) {
            expenseService.transferQuoteToExpense(quote, job);
        }
        return "redirect:/my-renovations/job-details?jobId="
                +jobId
                +"&fromSearch=" +fromSearch
                +"&expensesPage="+expensesPage
                +"&quotesPage="+quotesPage
                +"#nav-quotes";
    }

    /**
     * Post mapping for rejecting a quote on the job details page
     * @param quoteId The id of the quote which is getting rejected
     * @param jobId the id of the job to which the quote belongs to
     * @param fromSearch boolean stating if the job details page was accessed from the search page
     * @param expensesPage the current page on the expenses tab
     * @param quotesPage the current page on the quotes tab
     * @param principal the currently authenticated user
     * @return Redirects user back to the Job Details page
     */
    @PostMapping("/my-renovations/job-details/reject-quote")
    public String rejectQuoteJobDetails(
            @RequestParam(name = "quoteId") long quoteId,
            @RequestParam(name = "jobId") long jobId,
            @RequestParam(name = "fromSearch") boolean fromSearch,
            @RequestParam(name = "expensesPage", required = false) Integer expensesPage,
            @RequestParam(name = "quotesPage", required = false) Integer quotesPage,
            Principal principal) {
        Quote quote = quoteService.findQuoteById(quoteId);
        Job job = quote.getJob();
        if (job.getRenovationRecord().getUserEmail().equals(principal.getName())) {
            quote.setStatus("Rejected");
            quoteService.addQuote(quote);
            User user = quote.getUser();
            String email = user.getEmail();
            taskScheduler.schedule(
                    () -> emailService.sendQuoteRejectedEmail(email, job),
                    Instant.now()
            );
        }
        return "redirect:/my-renovations/job-details?jobId="
                +jobId
                +"&fromSearch=" +fromSearch
                +"&expensesPage="+expensesPage
                +"&quotesPage="+quotesPage
                +"#nav-quotes";
    }

    /**
     * Gets the ratings for the tradies from the user
     * @param ratingsJson The ratings of the tradies from the user in a JSON string in a structure which has the tradie
     * ID and the rating
     * @param principal the currently authenticated user
     * @param request HttpServletRequest of session
     * @return Returns use to the page that they changed the status if the job from
     * @throws JsonProcessingException Needed when using the mapper to parse JSON string
     */
    @PostMapping("/rate-tradie")
    public String rateTradie(
            @RequestParam(name = "ratings") String ratingsJson,
            @RequestParam(name = "jobId") Long jobId,
            Principal principal,
            HttpServletRequest request) throws JsonProcessingException {
        logger.info("POST /rate-tradie");
        ObjectMapper mapper = new ObjectMapper();
        List<TradieRating> tradieRatings = Arrays.asList(mapper.readValue(ratingsJson, TradieRating[].class));
        //Still need to save ratings
        tradieRatings = validationService.checkTradieRatings(tradieRatings);
        for (TradieRating tradieRating : tradieRatings) {
            quoteService.setQuoteAsRated(jobId, tradieRating.getTradieId());
            User tradie = userService.getUserById(tradieRating.getTradieId());
            User sendingUser = userService.getUser(principal.getName());
            Rating rating = new Rating(tradieRating.getRating(), tradie, sendingUser);
            ratingService.addRating(rating);
        }
        return "redirect:" + request.getHeader("Referer");
    }
}
