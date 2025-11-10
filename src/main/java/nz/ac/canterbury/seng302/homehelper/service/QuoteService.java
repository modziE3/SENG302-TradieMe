package nz.ac.canterbury.seng302.homehelper.service;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.Quote;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.QuoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Service for helping with quotes
 */
@Service
public class QuoteService {
    Logger logger = LoggerFactory.getLogger(QuoteService.class);

    private final QuoteRepository quoteRepository;
    private final ValidationService validationService;
    private final TaskScheduler taskScheduler;
    private final EmailService emailService;
    private final UserService userService;

    public static final String QUOTE_PRICE_EMPTY = "Quote price cannot be empty";
    public static final String QUOTE_PRICE_INVALID = "Quote price must only include positive numbers, including decimal point numbers";
    public static final String QUOTE_PRICE_TOO_LONG = "Quote price must be 15 characters or less";
    public static final String QUOTE_TIME_NON_NUMERIC = "Estimated time must not be empty and must be numeric";
    public static final String QUOTE_DESCRIPTION_OVER_512_CHARS = "Quote description must be 512 characters or less";
    public static final String CONTACT_DETAILS_EMPTY = "Contact details cannot both be empty";
    public static final String EMAIL_INVALID = "Email address must be in the form 'jane@doe.nz'";
    public static final String PHONE_NUMBER_INVALID = "Phone number must be a valid phone number";
    public static final String QUOTE_TIME_TOO_LONG = "Estimated work time must be 5 characters or less";

    public static final Integer maxWorkTimeLength = 5;


    @Autowired
    public QuoteService(
            QuoteRepository quoteRepository,
            ValidationService validationService,
            TaskScheduler taskScheduler,
            EmailService emailService, UserService userService
    ) {
        this.quoteRepository = quoteRepository;
        this.validationService = validationService;
        this.taskScheduler = taskScheduler;
        this.emailService = emailService;
        this.userService = userService;
    }

    /**
     * Gets all quotes in storage
     * @return List of all quotes in storage
     */
    public List<Quote> getAllQuotes() {
        return quoteRepository.findAll();
    }

    /**
     * Gets all quotes sent by the user with a certain ID
     * @param userId ID of the user
     * @return List of all quotes by the user with a certain ID
     */
    public List<Quote> getQuotesByUserId(Long userId) {
        return quoteRepository.findAllByUserId(userId);
    }

    /**
     * Gets all quotes that have been sent for a certain job
     * @param jobId ID of the job
     * @return List of all quotes for all jobs in the job list
     */
    public List<Quote> getQuotesByJobId(Long jobId) {
        return quoteRepository.findAllByJobId(jobId);
    }

    /**
     *
     * @param jobId the job id used to get quotes
     * @param status the status used to filter jobs
     * @return a list of jobs retrieved by jobId and filtered by status
     */
    public List<Quote> getQuotesByJobIdFilteredStatus(Long jobId, String status) {
        return quoteRepository.findAllByJobIdAndStatus(jobId, status);
    }

    /**
     * Gets the quotes sent by a user, filtered with a given status. If status is null then the
     * filter is ignored.
     * @param userEmail the email of the user that sent the quotes.
     * @param status the status used to filter the quotes
     * @return a list of quotes sent by a user filtered with a given status.
     */
    public List<Quote> getSentQuotes(String userEmail, String status) {
        return quoteRepository.findSentQuotes(userEmail, status);
    }

    /**
     * Gets the received quotes of jobs owned by a given user. The quotes are filtered to a
     * status. If the status is null then the filter is ignored.
     * @param userEmail the email of the owner of the jobs for which quotes have been sent to.
     * @param status the status used to filter the quotes.
     * @return a list of quotes received by a user filtered with a given status.
     */
    public List<Quote> getReceivedQuotes(String userEmail, String status) {
        return quoteRepository.findReceivedQuotes(userEmail, status);
    }

    /**
     * Saves the quote to the repository
     * @param quote A quote
     */
    public void addQuote(Quote quote) {
        quoteRepository.save(quote);
    }

    /**
     * validates that the quote is in the correct format
     * @param quote the quote being checked
     * @throws IllegalArgumentException throws an error containing all the errors found in the quote
     */
    public void validateQuote(Quote quote) throws IllegalArgumentException {
        List<String> errors = new ArrayList<>();

        if (validationService.stringEmpty(quote.getPrice())) {
            errors.add(QUOTE_PRICE_EMPTY);
        } else if (!validationService.correctExpenseCostFormat(quote.getPrice())) {
            errors.add(QUOTE_PRICE_INVALID);
        } else if (!validationService.correctExpenseCostLength(quote.getPrice())) {
            errors.add(QUOTE_PRICE_TOO_LONG);
        }

        if (!validationService.checkPositiveNumber(quote.getWorkTime()) || validationService.stringEmpty(quote.getWorkTime())) {
            errors.add(QUOTE_TIME_NON_NUMERIC);
        } else if (quote.getWorkTime().length() > maxWorkTimeLength) {
            errors.add(QUOTE_TIME_TOO_LONG);
        }

        if (!validationService.correctDescriptionLength(quote.getDescription())) {
            errors.add(QUOTE_DESCRIPTION_OVER_512_CHARS);
        }

        if (validationService.stringEmpty(quote.getEmail()) && validationService.stringEmpty(quote.getPhoneNumber())) {
            errors.add(CONTACT_DETAILS_EMPTY);
        } else {
            if (!validationService.checkEmailForm(quote.getEmail()) && !validationService.stringEmpty(quote.getEmail())) {
                errors.add(EMAIL_INVALID);
            }
            if (!validationService.checkValidPhoneNumber(quote.getPhoneNumber()) && !validationService.stringEmpty(quote.getPhoneNumber())) {
                errors.add(PHONE_NUMBER_INVALID);
            }
            if (!validationService.checkValidPhoneNumber(quote.getPhoneNumber()) && !validationService.checkEmailForm(quote.getEmail())) {
                errors.add(PHONE_NUMBER_INVALID);
                errors.add(EMAIL_INVALID);
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errors));
        }
    }

    /**
     * turns a string with all the error messages in it into a list of all the error messages
     * @param allErrorMessages a string with all the error messages for the quote in a string
     * @return a list of all the error messages seperated so that you can tell which error message is for which field
     */
    public List<String> getErrorMessages(String allErrorMessages) {
        String priceErrorMessage = "";
        if (allErrorMessages.contains(QUOTE_PRICE_EMPTY)) {
            priceErrorMessage = QUOTE_PRICE_EMPTY;
        }
        if (allErrorMessages.contains(QUOTE_PRICE_INVALID)) {
            priceErrorMessage = QUOTE_PRICE_INVALID;
        }
        if (allErrorMessages.contains(QUOTE_PRICE_TOO_LONG)) {
            priceErrorMessage = QUOTE_PRICE_TOO_LONG;
        }

        String workTimeErrorMessage = "";
        if (allErrorMessages.contains(QUOTE_TIME_NON_NUMERIC)) {
            workTimeErrorMessage = QUOTE_TIME_NON_NUMERIC;
        } else if (allErrorMessages.contains(QUOTE_TIME_TOO_LONG)) {
            workTimeErrorMessage = QUOTE_TIME_TOO_LONG;
        }

        String descriptionErrorMessage = "";
        if (allErrorMessages.contains(QUOTE_DESCRIPTION_OVER_512_CHARS)) {
            descriptionErrorMessage = QUOTE_DESCRIPTION_OVER_512_CHARS;
        }

        String emailErrorMessage = "";
        if (allErrorMessages.contains(CONTACT_DETAILS_EMPTY)) {
            emailErrorMessage = CONTACT_DETAILS_EMPTY;
        }
        if (allErrorMessages.contains(EMAIL_INVALID)) {
            emailErrorMessage = EMAIL_INVALID;
        }

        String phoneNumberErrorMessage = "";
        if (allErrorMessages.contains(PHONE_NUMBER_INVALID)) {
            phoneNumberErrorMessage = PHONE_NUMBER_INVALID;
        }
        return List.of(priceErrorMessage, workTimeErrorMessage, descriptionErrorMessage,
                emailErrorMessage, phoneNumberErrorMessage);
    }

    /**
     * Checks if the user has already quoted a job
     * @param user The user who is quoting a job
     * @param job The job the user is quoting
     * @return Returns true if the user has already quoted a job and false if not
     */
    public Boolean checkIfAlreadyQuoted(User user, Job job) {
        List<Quote> quotes = quoteRepository.findAllByUser(user);
        if (quotes.isEmpty()) {
            return false;
        }
        for (Quote quote : quotes) {
            if (quote.getJob().getId().equals(job.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a quote based of a quote id
     * @param id a quote id
     * @return Returns a quote or null if no quote exists with given id
     */
    public Quote findQuoteById(Long id) {
        return quoteRepository.findById(id).orElse(null);
    }

    /**
     * Gets all quotes that have been sent for a certain list of jobs
     * @param jobs List of jobs the quotes have been sent to
     * @return List of all quotes for all jobs in the job list
     */
    public List<Quote> getQuotesByJobs(List<Job> jobs) {
        List<Quote> quotes = new ArrayList<>();
        for (Job job : jobs) {
            quotes.addAll(quoteRepository.findAllByJobId(job.getId()));
        }
        return quotes;
    }

    /**
     * gets quote from repository by its id
     * @param userEmail authenticated user's email
     * @param status status of job
     * @param id job id
     * @return the quote repository
     */
    public Quote getQuote(String userEmail, String status, Long id) {
        return quoteRepository.findQuoteById(userEmail, status, id);
    }

    /**
     * Retracts the quote from the repository
     * @param quote The quote to be retracted
     */
    @Transactional
    public void retractQuote(Quote quote) {
        quoteRepository.deleteQuoteById(quote.getId());
    }

    /**
     * Gets the accepted quotes for a given job
     * @param jobId the job to get the quotes for
     */
    public List<Quote> getAcceptedQuotes(Long jobId) {
        return quoteRepository.findAllByJobIdAndAcceptedStatus(jobId);
    }

    /**
     * Sets the tradies quote for a job as rated so that it cannot be rated again
     * @param jobId the job to rated for
     * @param tradieId the tradie being rated
     */
    public void setQuoteAsRated(Long jobId, Long tradieId) {
        List<Quote> acceptedQuotes = getAcceptedQuotes(jobId);
        for (Quote quote : acceptedQuotes) {
            if (quote.getUser().getId().equals(tradieId)) {
                quote.setRated(true);
                quoteRepository.save(quote);
            }
        }
    }

    /**
     * Rejects a quote and emails the sender to inform them of their tragic loss.
     * @param quote the quote being rejected.
     */
    public Quote rejectQuote(Quote quote) {
        Job job = quote.getJob();
        quote.setStatus("Rejected");
        this.addQuote(quote);
        User user = quote.getUser();
        String email = user.getEmail();
        taskScheduler.schedule(
                () -> emailService.sendQuoteRejectedEmail(email, job),
                Instant.now()
        );
        return quote;
    }

    /**
     * Accepts quote and saves in database
     * @param quote the quote being accepted.
     */
    public void acceptQuote(Quote quote) {
        quote.setStatus("Accepted");
        this.addQuote(quote);
    }

    /**
     * compares two quotes and returns a list of boolean with their stats
     * @param quote1 the quote that the list of booleans will relate to
     * @param quote2 the quote being compared against
     * @return a list of booleans with true if the first quote has a better stat for that one
     */
    public List<Boolean> compareQuotes(Quote quote1, Quote quote2) {
        List<Boolean> quote1stats = new ArrayList<>();
        quote1stats.add(quote1.getUser().getAverageRating() >= quote2.getUser().getAverageRating());
        quote1stats.add(quote1.getPriceAsFloat() <= quote2.getPriceAsFloat());
        quote1stats.add(userService.getCompletedJobsUserHasWorkedOn(quote1.getUser().getId()).size() >= userService.getCompletedJobsUserHasWorkedOn(quote2.getUser().getId()).size());
        quote1stats.add(quote1.getWorkTimeAsLong() <= quote2.getWorkTimeAsLong());
        quote1stats.add(userService.getUsersWorkEfficiency(quote1.getUser().getId()) <= userService.getUsersWorkEfficiency(quote2.getUser().getId()));

        return quote1stats;
    }

}
