package nz.ac.canterbury.seng302.homehelper.controller;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the home page
 */
@Controller
public class HomePageController {
    Logger logger = LoggerFactory.getLogger(HomePageController.class);

    private final RenovationRecordService renovationRecordService;
    private final UserService userService;
    private final JobService jobService;
    private final QuoteService quoteService;

    public HomePageController(RenovationRecordService renovationRecordService, UserService userService, JobService jobService, QuoteService quoteService) {
        this.renovationRecordService = renovationRecordService;
        this.userService = userService;
        this.jobService = jobService;
        this.quoteService = quoteService;
    }

    /**
     * Redirects GET default url '/' to '/demo'
     * @return redirect to /demo
     */
    @GetMapping("/")
    public String home(Principal principal) {
        logger.info("GET /");
        if (principal != null) {
            return "redirect:./home";
        } else {
            return "redirect:./login";
        }
    }

    /**
     * Gets the thymeleaf page representing the /demo page (a basic welcome screen with some links)
     * @param name url query parameter of user's name
     * @param principal the currently authenticated user.
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @return thymeleaf homepage
     */
    @GetMapping("/home")
    public String getLoggedInHomePage(
            @RequestParam(name = "name", required = false, defaultValue = "World") String name,
            Principal principal,
            Model model) {
        logger.info("GET /home");
        model.addAttribute("loggedIn", principal != null);
        if (principal != null) {
            User user = userService.getUser(principal.getName());
            if (user != null && user.getVerificationCode() != null) {
                return "redirect:/registration-code";
            }
            List<RenovationRecord> recentRenovations = userService.getRecentRenovations(user);
            List<Job> recommendedPostedJobs = jobService.getRecommendedPostedJobs(user);
            model.addAttribute("name", name);
            model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
            model.addAttribute("recommendedJobs", jobService.getJobCardsPosted(userService,  recommendedPostedJobs));
            model.addAttribute("recentJobs", jobService.getJobCardsAll(userService, user.getRecentJobs().stream().map(jobService::getJobById).collect(Collectors.toList())));

            // calendar info
            List<RenovationRecord> renovationRecords = renovationRecordService.getRenovationRecordsByOwner(principal.getName());
            List<Job> jobs = new ArrayList<>();
            for (RenovationRecord record : renovationRecords) {
                jobs.addAll(record.getJobs());
            }
            List<Quote> quotes = quoteService.getQuotesByUserId(user.getId());
            List<Job> quotedJobs = new ArrayList<>();
            for (Quote quote : quotes) {
                if (quote.getStatus() == "Accepted") {
                    if (quote.getJob() != null) {
                        quotedJobs.add(quote.getJob());
                    }
                }
            }

            if (user.getProfilePicture() != null) {
                model.addAttribute("profileUserImage", "/profileImages/" + user.getProfilePicture());
            }

            List<String> homePageWidgetOrder = user.getHomePageWidgetOrder();
            jobs.addAll(quotedJobs);
            model.addAttribute("hottestTradies", userService.getHottestTradies());
            model.addAttribute("recordIds", jobs.stream().map(j -> j.getRenovationRecord().getId()).toList());
            model.addAttribute("jobIds", jobs.stream().map(Job::getId).toList());
            model.addAttribute("jobNames", jobs.stream().map(Job::getName).toList());
            model.addAttribute("jobStartDates", jobService.convertJobStartDatesForCalendar(jobs));
            model.addAttribute("jobDueDates", jobService.convertJobDueDatesForCalendar(jobs));
            model.addAttribute("jobStatuses", jobs.stream().map(Job::getStatus).toList());
            model.addAttribute("jobWasModified", jobService.jobsWereModified(jobs));
            model.addAttribute("isQuotedJob", jobs.stream().map(quotedJobs::contains).toList());
            model.addAttribute("publicUser", true);
            model.addAttribute("homePageWidgetOrder", homePageWidgetOrder);

            model.addAttribute("recentRenovations", renovationRecordService.getRenovationRecordCards(userService, recentRenovations));
            model.addAttribute("user", user);
            if (user != null && user.getProfilePicture() != null) {
                model.addAttribute("profileImage", "/profileImages/" + user.getProfilePicture());
            }
        } else {
            return "redirect:./login";
        }
        return "homePageTemplate";
    }
}
