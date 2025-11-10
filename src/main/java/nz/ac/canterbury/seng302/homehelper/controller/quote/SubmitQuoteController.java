package nz.ac.canterbury.seng302.homehelper.controller.quote;

import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.Quote;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.QuoteService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

/**
 * Controller for the submit quote form
 */
@Controller
public class SubmitQuoteController {
    Logger logger = LoggerFactory.getLogger(SubmitQuoteController.class);

    private final QuoteService quoteService;
    private final JobService jobService;
    private final UserService userService;
    private final EmailService emailService;
    private final TaskScheduler taskScheduler;

    @Autowired
    public SubmitQuoteController(QuoteService quoteService, JobService jobService, UserService userService,
                                 EmailService emailService, TaskScheduler taskScheduler) {
        this.quoteService = quoteService;
        this.jobService = jobService;
        this.userService = userService;
        this.emailService = emailService;
        this.taskScheduler = taskScheduler;
    }

    /**
     * Gets the submit quote form for a particular job
     * @param jobId ID of the job the quote will be for
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @return Returns submit quote template
     */
    @GetMapping("/submit-quote")
    public String getSubmitQuoteForm(
            @RequestParam(name = "jobId") long jobId,
            Model model) {
        model.addAttribute("jobId", jobId);
        return "submitQuoteTemplate";
    }

    /**
     * Gets the values entered into the submit quote form and creates a new quote
     * @param jobId ID of the job the quote will be for
     * @param price Price entered into the form
     * @param workTime Work time entered into the form
     * @param email Email given by the user as their contact details, optional (need email or phone number)
     * @param phoneNumber Phone number given by user as their contact details, optional (need email or phone number)
     * @param description Description of quote given by user
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param principal the currently authenticated user.
     * @return Redirects the user to the job details page of the job they are giving a quote for
     */
    @PostMapping("/submit-quote")
    public String postSubmitQuoteForm(
            @RequestParam(name = "jobId") long jobId,
            @RequestParam(name = "price") String price,
            @RequestParam(name = "workTime") String workTime,
            @RequestParam(name = "email") String email,
            @RequestParam(name = "phoneNumber") String phoneNumber,
            @RequestParam(name = "description") String description,
            Model model,
            Principal principal) {
        logger.info("POST /submit-quote");

        Job job = jobService.getJobById(jobId);
        Quote quote = new Quote(price, workTime, email, phoneNumber, description);
        try {
            String userEmail = principal.getName();
            User user = userService.getUser(userEmail);
            quote.setUser(user);
            if (quoteService.checkIfAlreadyQuoted(user, jobService.getJobById(jobId))) {
                model.addAttribute("quotedErrorMessage", "You have already submitted a quote for this job");
                model.addAttribute("jobId", jobId);
                model.addAttribute("price", price);
                model.addAttribute("workTime", workTime);
                model.addAttribute("email", email);
                model.addAttribute("phoneNumber", phoneNumber);
                model.addAttribute("description", description);
                return "submitQuoteTemplate";
            }
            quoteService.validateQuote(quote);
            quote.setJob(job);
            quoteService.addQuote(quote);
            taskScheduler.schedule(
                    () -> emailService.sendQuoteReceivedEmail(job),
                    Instant.now()
            );

            return "redirect:/my-renovations/job-details?jobId="+jobId+"&fromSearch=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("jobId", jobId);
            model.addAttribute("price", price);
            model.addAttribute("workTime", workTime);
            model.addAttribute("email", email);
            model.addAttribute("phoneNumber", phoneNumber);
            model.addAttribute("description", description);
            setErrorMessages(model, e.getMessage());
            return "submitQuoteTemplate";
        }
    }


    /**
     * function to set the error message attributes instead of doing it all in the post function
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param allErrorMessages a string that contains all the error messages found in the posted quote
     */
    private void setErrorMessages(Model model, String allErrorMessages) {
        List<String> errorMessages = quoteService.getErrorMessages(allErrorMessages);
        model.addAttribute("priceErrorMessage", errorMessages.get(0));
        model.addAttribute("workTimeErrorMessage", errorMessages.get(1));
        model.addAttribute("descriptionErrorMessage", errorMessages.get(2));
        model.addAttribute("emailErrorMessage", errorMessages.get(3));
        model.addAttribute("phoneNumberErrorMessage", errorMessages.get(4));
    }

}
