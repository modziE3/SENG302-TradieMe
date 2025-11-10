package nz.ac.canterbury.seng302.homehelper.controller.quote;

import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.Quote;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.QuoteService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.*;

/**
 * Controller for the Compare Tradies page
 */
@Controller
public class CompareTradieController {
    Logger logger = LoggerFactory.getLogger(CompareTradieController.class);
    private final JobService jobService;
    private final RenovationRecordService renovationRecordService;
    private final UserService userService;
    private final QuoteService quoteService;

    @Autowired
    public CompareTradieController(
            JobService jobService,
            RenovationRecordService renovationRecordService,
            UserService userService, QuoteService quoteService) {
        this.jobService = jobService;
        this.renovationRecordService = renovationRecordService;
        this.userService = userService;
        this.quoteService = quoteService;
    }

    /**
     * Gets the compare tradies page that displays two tradies who have sent currently pending quotes to a user's renovation job
     * @param jobId ID of the job the tradies have sent quotes to
     * @param principal the currently authenticated user
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @return thymeleaf template for the compare tradies page
     */
    @GetMapping("/my-renovations/job-details/compare-tradies")
    public String getCompareTradiesPage(
            @RequestParam(name = "jobId") Long jobId,
            Principal principal,
            Model model) {
        logger.info("GET /my-renovations/job-details/compare-tradies");

        User user = userService.getUser(principal.getName());
        try {
            Job job = jobService.getJobById(jobId);
            List<RenovationRecord> headerRecords = renovationRecordService.getRenovationRecordsByOwner(principal.getName());
            if (!job.getRenovationRecord().getUserEmail().equals(user.getEmail())) {
                return getErrorPage(principal, model);
            }

            List<Quote> quotes = job.getQuotes();
            List<User> quoteSenders = new ArrayList<>();
            for (Quote quote : quotes) {
                if (!quoteSenders.contains(quote.getUser()) && Objects.equals(quote.getStatus(), "Pending")) {
                    quoteSenders.add(quote.getUser());
                }
            }
            User tradie1 = quoteSenders.get(0);
            User tradie2 = quoteSenders.get(1);
            Quote quote1 = null, quote2 = null;
            for (Quote quote : quotes) {
                if (quote.getUser() == tradie1) {
                    quote1 = quote;
                } else if (quote.getUser() == tradie2) {
                    quote2 = quote;
                }
            }

            quoteSenders.remove(tradie1);
            quoteSenders.remove(tradie2);
            quotes.remove(quote1);
            quotes.remove(quote2);

            List<Long> tradieIds = new ArrayList<>();
            for (User tradie : quoteSenders) {
                tradieIds.add(tradie.getId());
            }

            List<Long> quoteIds = new ArrayList<>();
            for (Quote quote : quotes) {
                quoteIds.add(quote.getId());
            }

            model.addAttribute("quote1Stats", quoteService.compareQuotes(quote1, quote2));
            model.addAttribute("quote2Stats", quoteService.compareQuotes(quote2, quote1));
            model.addAttribute("user", user);
            model.addAttribute("loggedIn", true);
            model.addAttribute("renovationRecords", headerRecords);
            model.addAttribute("job", job);
            model.addAttribute("tradie1", tradie1);
            model.addAttribute("quote1", quote1);
            model.addAttribute("tradie2", tradie2);
            model.addAttribute("quote2", quote2);
            model.addAttribute("userService", userService);
            model.addAttribute("tradieIds", tradieIds);
            model.addAttribute("quoteIds", quoteIds);
            if (user.getProfilePicture() != null) {
                model.addAttribute("profileImage", "/profileImages/" + user.getProfilePicture());
            }
            return "compareTradiesTemplate";
        } catch (NullPointerException e) {
            logger.warn(e.getMessage());
            return getErrorPage(principal, model);
        }
    }

    /**
     * Gets the error page if invalid job ID found when going to the compare tradies page
     * @param principal the currently authenticated user
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @return error thymeleaf template
     */
    public String getErrorPage(Principal principal, Model model) {
        logger.info("GET error");
        User user = userService.getUser(principal.getName());
        model.addAttribute("loggedIn", principal != null);
        model.addAttribute("user", user);
        model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
        return "error";
    }

    @PatchMapping("/my-renovations/job-details/compare-tradies/reject")
    public ResponseEntity<?> rejectTradie(
            Model model,
            Principal principal,
            @RequestParam(name = "side") String side,
            @RequestParam(name = "rejectedQuoteId") Long rejectedQuoteId
    ) {
        try {
            // retrieve data
            User user = userService.getUser(principal.getName());
            Quote quote = quoteService.findQuoteById(rejectedQuoteId);
            Job job = quote.getJob();
            List<String> validSides = List.of("left", "right", "top", "bottom");
            boolean isLeft = side.equals("left");

            // verify before updating data
            if (!Objects.equals(job.getRenovationRecord().getUserEmail(), user.getEmail())) {
                String warning = "Job: "+job.getId()+" does not belong to the user: "+user.getEmail();
                logger.warn(warning);
                return ResponseEntity.badRequest().body("Error: Invalid data in patch request: "+ warning);
            } else if (!validSides.contains(side.toLowerCase())) {
                throw new Exception("Side is not valid (not 'left', 'right', 'top' or 'bottom')");
            }
            // update data
            quoteService.rejectQuote(quote);

            //information used to show modal for accepting last tradie
            int remainingQuotes = quoteService.getQuotesByJobIdFilteredStatus(job.getId(), "Pending").size();
            Map<String, Object> response = new HashMap<>();
            response.put("remainingQuotes", remainingQuotes);
            response.put("quoteId", quote.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: Invalid data in patch request: " + e.getMessage());
        }
    }

    /**
     * Gets the next tradie card fragment for the compare tradies page
     * @param tradieIds All the remaining tradie Ids for a given job
     * @param quoteIds All the remaining quote Ids for a given job
     * @param side The side of the page the quote is for, left or right for desktop, top or bottom for mobile
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @return A tradie card fragment
     */
    @GetMapping("my-renovations/job-details/compare-tradies/get-next-tradie")
    public String getNextTradieCard(@RequestParam List<Long> tradieIds,
                                    @RequestParam List<Long> quoteIds,
                                    @RequestParam Long oldQuoteId,
                                    @RequestParam String side, Model model) {
        List<String> mobileCards = List.of("top", "bottom");
        List<User> tradies = new ArrayList<>();
        for (Long tradieId : tradieIds) {
            User user = userService.getUserById(tradieId);
            tradies.add(user);
        }

        List<Quote> quotes = new ArrayList<>();
        for (Long quoteId : quoteIds) {
            Quote quote = quoteService.findQuoteById(quoteId);
            quotes.add(quote);
        }

        User tradie = tradies.getFirst();
        Quote quote = null;
        for (Quote quote1 : quotes) {
            if (quote1.getUser().equals(tradie)) {
                quote = quote1;
            }
        }

        if (quote != null) {
            model.addAttribute("quoteStats", quoteService.compareQuotes(quote, quoteService.findQuoteById(oldQuoteId)));
            model.addAttribute("quoteStats2", quoteService.compareQuotes(quoteService.findQuoteById(oldQuoteId), quote));
            model.addAttribute("tradie", tradie);
            model.addAttribute("quote", quote);
            model.addAttribute("userService", userService);
            if (!mobileCards.contains(side.toLowerCase())) {
                if (side.equals("left")) {
                    model.addAttribute("isLeft", true);
                } else {
                    model.addAttribute("isLeft", false);
                }
            } else {
                if (side.equals("top")) {
                    model.addAttribute("isTop", true);
                } else {
                    model.addAttribute("isTop", false);
                }
            }
            if (quote != null || tradie != null) {
                if (!mobileCards.contains(side.toLowerCase())) {
                    return "fragments/tradieCard :: tradieCard";
                } else {
                    return "fragments/mobileTradieCard :: mobileTradieCard";
                }
            }
        }

        return "No More Tradies To Compare";
    }

    @GetMapping("my-renovations/job-details/compare-tradies/get-job-modals")
    public ResponseEntity<List<Long>> getFirstTwoJobs(@RequestParam Long tradieId) {
        User tradie = userService.getUserById(tradieId);
        if (tradie == null) {
            return ResponseEntity.notFound().build();
        }
        List<Long> jobs = tradie.getPortfolioJobs().stream()
                .limit(2)
                .map(job -> job.getId())
                .toList();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("my-renovations/job-details/compare-tradies/get-job-modal-fragment")
    public String getJobModalFragment(@RequestParam Long jobId, Model model) {
        Job job = jobService.getJobById(jobId);
        logger.warn(job == null ? "null" : "not null");
        model.addAttribute("job", job);
        return "fragments/imageScrollModal :: jobImageModalFragment";
    }

    /**
     * Prompts user to accept last tradie if all others have been rejected
     * @param jobId The id of the job
     * @return to the job details page
     */
    @PostMapping("my-renovations/job-details/compare-tradies/accept-last")
    public String acceptLastTradie(
            @RequestParam("jobId") Long jobId,
            Model model,
            Principal principal){

        Job job = jobService.getJobById(jobId);
        User user = userService.getUser(principal.getName());
        List<RenovationRecord> headerRecords = renovationRecordService.getRenovationRecordsByOwner(principal.getName());
        List<Quote> quotes = job.getQuotes();
        List<User> quoteSenders = new ArrayList<>();
        for (Quote quote : quotes) {
            if (!quoteSenders.contains(quote.getUser()) && Objects.equals(quote.getStatus(), "Pending")) {
                quoteSenders.add(quote.getUser());
            }
        }
        User tradie1 = quoteSenders.size() > 0 ? quoteSenders.get(0) : null;
        User tradie2 = quoteSenders.size() > 1 ? quoteSenders.get(1) : null;
        Quote quote1 = null, quote2 = null;

        for (Quote quote : quotes) {
            if (quote.getUser().equals(tradie1)) {
                quote1 = quote;
            } else if (quote.getUser().equals(tradie2)) {
                quote2 = quote;
            }
        }

        //this should contain one quote if everything has been rejected already, just easier to retrieve in list form
        List<Quote> finalQuoteList = quoteService.getQuotesByJobIdFilteredStatus(jobId, "Pending");
        Quote finalQuote = null;
        if (!finalQuoteList.isEmpty()) {
            finalQuote = finalQuoteList.get(0);
        }
        if (finalQuote.getJob().getRenovationRecord().getUserEmail().equals(principal.getName())) {
            quoteService.acceptQuote(finalQuote);
        }
        model.addAttribute("finalQuoteList", finalQuoteList);
        model.addAttribute("job", job);
        model.addAttribute("user", user);
        model.addAttribute("loggedIn", true);
        model.addAttribute("renovationRecords", headerRecords);
        model.addAttribute("job", job);
        model.addAttribute("tradie1", tradie1);
        model.addAttribute("quote1", quote1);
        model.addAttribute("tradie2", tradie2);
        model.addAttribute("quote2", quote2);

        String profileImage = (user != null && user.getProfilePicture() != null)
                ? "/profileImages/" + user.getProfilePicture()
                : "/images/DefaultProfileImage.png";
        model.addAttribute("profileImage", profileImage);
        return "redirect:/my-renovations/job-details?jobId="
                +jobId;
    }
}

