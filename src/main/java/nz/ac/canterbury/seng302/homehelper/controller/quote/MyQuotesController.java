package nz.ac.canterbury.seng302.homehelper.controller.quote;

import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.Quote;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Controller for My-Quotes page.
 */
@Controller
public class MyQuotesController {
    private final ExpenseService expenseService;
    Logger logger = LoggerFactory.getLogger(MyQuotesController.class);
    private QuoteService quoteService;
    private RenovationRecordService renovationRecordService;
    private JobService jobService;
    private UserService userService;
    private final TaskScheduler taskScheduler;
    private final EmailService emailService;


    private final List<String> QUOTES_STATES = List.of("pending", "accepted", "rejected");

    @Autowired
    public MyQuotesController(QuoteService quoteService, RenovationRecordService renovationRecordService,
                              JobService jobService, UserService userService, TaskScheduler taskScheduler, EmailService emailService, ExpenseService expenseService) {
        this.quoteService = quoteService;
        this.renovationRecordService = renovationRecordService;
        this.jobService = jobService;
        this.userService = userService;
        this.taskScheduler = taskScheduler;
        this.emailService = emailService;
        this.expenseService = expenseService;
    }

    /**
     * Gets the My Quotes page template and fills in the quotes (sent and received).
     * The number of quotes for each tab is different because the received quotes card
     * is larger and takes up more space.
     * @param status a status filter is applied to the list of quotes. If the status
     *               is null or doesn't match a real status then the filter is ignored.
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param principal the currently authenticated user
     * @return thymeleaf template for the My Quotes page
     */
    @GetMapping("/my-quotes")
    public String getMyQuotesPage(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "sentPage", required = false) Integer sentPage,
            @RequestParam(name = "receivedPage", required = false) Integer receivedPage,
            Model model,
            Principal principal) {
        logger.info("GET /my-quotes");
        if (status != null && !QUOTES_STATES.contains(status.toLowerCase())) {
            status = null;
        } else if (status != null) {
            status = status.toLowerCase();
        }

        User user = userService.getUser(principal.getName());
        List<Quote> sentQuotes = quoteService.getQuotesByUserId(user.getId());
        List<RenovationRecord> records = renovationRecordService.getRenovationRecordsByOwner(principal.getName());
        List<Job> jobs = new ArrayList<>();
        for (RenovationRecord record : records) {
            jobs.addAll(record.getJobs());
        }
        List<Quote> receivedQuotes = quoteService.getQuotesByJobs(jobs);
        model.addAttribute("loggedIn", true);
        model.addAttribute("user", user);
        model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
        if (user.getProfilePicture() != null) {
            model.addAttribute("profileImage", "/profileImages/" + user.getProfilePicture());
        }
        model.addAttribute("status", status == null ? "Filter Status" : status);

        // pagination received quotes
        receivedPage = receivedPage == null ? 1 : receivedPage;
        model.addAttribute("receivedResults", PaginationUtil.getPageNumbers(quoteService.getReceivedQuotes(user.getEmail(), status).size(), 12));
        model.addAttribute("receivedPage", receivedPage);
        model.addAttribute("lastPageReceived", PaginationUtil.getLastPageNumber(quoteService.getReceivedQuotes(user.getEmail(), status).size(), 12));
        model.addAttribute("receivedQuotes", PaginationUtil.getPage(quoteService.getReceivedQuotes(user.getEmail(), status), receivedPage, 12));
        // pagination sent quotes
        sentPage = sentPage == null ? 1 : sentPage;
        model.addAttribute("sentResults", PaginationUtil.getPageNumbers(quoteService.getSentQuotes(user.getEmail(), status).size(), 15));
        model.addAttribute("sentPage", sentPage);
        model.addAttribute("lastPageSent", PaginationUtil.getLastPageNumber(quoteService.getSentQuotes(user.getEmail(), status).size(), 15));
        model.addAttribute("sentQuotes", PaginationUtil.getPage(quoteService.getSentQuotes(user.getEmail(), status), sentPage, 15));

        return "myQuotesTemplate";
    }

    /**
     * Post mapping for accepting a quote
     * @param quoteId The id of the quote which is getting accepted
     * @param retract A boolean for if a quotes job should be retracted or not
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param principal the currently authenticated user
     * @return Redirects user back to the my-quotes page
     */
    @PostMapping("/accept-quote")
    public String acceptQuote(@RequestParam(name = "quoteId") long quoteId,
                              @RequestParam(name = "retract", defaultValue = "false") Boolean retract,
                              @RequestParam(name = "transfer", defaultValue = "false") Boolean transfer,
                              Model model, Principal principal) {
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

        return "redirect:/my-quotes";
    }


    /**
     * This functions current purpose is for the pagination input field. This simply redirects the user to the page
     * number they have given. Since this page has two paginations there is an if to check which page to send the
     * user to.
     * @param status status filter
     * @param sentPage this is the UN-CHANGED page number for the sent-quotes tab.
     * @param receivedPage this is the UN-CHANGED page number for the received-quotes tab.
     * @param sentPageNumber this is the CHANGED page number for the sent-quotes tab.
     * @param receivedPageNumber this is the CHANGED page number for the received-quotes tab.
     * @return a redirect to the updated my quotes page.
     */
    @PostMapping("/my-quotes")
    public String submitPageNumberSearch(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "sentPage", required = false) Integer sentPage,
            @RequestParam(name = "receivedPage", required = false) Integer receivedPage,
            @RequestParam(name = "sentPageNumber", required = false) Integer sentPageNumber,
            @RequestParam(name = "receivedPageNumber", required = false) Integer receivedPageNumber) {
        if (sentPageNumber != null) {
            return "redirect:/my-quotes?sentPage=" + sentPageNumber + "&receivedPage=" + receivedPage + "&status=" + status;
        } else {
            return "redirect:/my-quotes?sentPage=" + sentPage + "&receivedPage=" + receivedPageNumber + "&status=" + status;
        }
    }

    /**
     * Post mapping for rejecting a quote
     * @param quoteId The id of the quote which is getting rejected
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param principal the currently authenticated user
     * @return Redirects user back to the my-quotes page
     */
    @PostMapping("/reject-quote")
    public String rejectQuote(@RequestParam(name = "quoteId") long quoteId,
                              Model model, Principal principal) {
        Quote quote = quoteService.findQuoteById(quoteId);
        if (quote.getJob().getRenovationRecord().getUserEmail().equals(principal.getName())) {
            quoteService.rejectQuote(quote);
        }
        return "redirect:/my-quotes";
    }


    /**
     * Used to retract quotes I have sent and sends retraction email to job owner
     * @param quoteId the id of the quote to be deleted
     * @param status the status of the page
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param principal the currently authenticated user
     * @return thymeleaf template for my quotes page with retracted quote removed
     */
    @PostMapping(value="/my-quotes", params="quoteId")
    public String postEditMyQuotesPage(
            @RequestParam(name = "quoteId", required = false) Long quoteId,
            @RequestParam(name = "status", required = false) String status,
            Model model,
            Principal principal) {
        logger.info("POST /my-quotes");

        if (status != null && !QUOTES_STATES.contains(status.toLowerCase())) {
            status = null;
        } else if (status != null) {
            status = status.toLowerCase();
        }
        User user = userService.getUser(principal.getName());
        String email = user.getEmail();
        Quote quote = quoteService.getQuote(email, status, quoteId);
        quoteService.retractQuote(quote);
        Job job = quote.getJob();
        model.addAttribute("sentQuotes", quoteService.getSentQuotes(user.getEmail(), status));
        model.addAttribute("receivedQuotes", quoteService.getReceivedQuotes(user.getEmail(), status));
        model.addAttribute("status", status == null ? "Filter Status" : status);
        if (status != null && !status.isBlank()) {
            taskScheduler.schedule(
                    () -> emailService.sendQuoteRetractedEmail(job, user),
                    Instant.now()
            );
            return "redirect:/my-quotes?status=" + URLEncoder.encode(status, StandardCharsets.UTF_8);
        }
        taskScheduler.schedule(
                () -> emailService.sendQuoteRetractedEmail(job, user),
                Instant.now()
        );
        return "redirect:/my-quotes";
    }


}
