package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.Quote;
import nz.ac.canterbury.seng302.homehelper.entity.Rating;
import nz.ac.canterbury.seng302.homehelper.entity.dto.TradieRating;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.QuoteRepository;
import nz.ac.canterbury.seng302.homehelper.service.QuoteService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import nz.ac.canterbury.seng302.homehelper.service.ValidationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class QuoteServiceTest {

    @Mock
    public QuoteRepository quoteRepository;
    @Mock
    public ValidationService validationService;
    @Mock
    public UserService userService;

    @InjectMocks
    QuoteService quoteService;

    public static final String QUOTE_PRICE_EMPTY = "Quote price cannot be empty";
    public static final String QUOTE_PRICE_INVALID = "Quote price must only include positive numbers, including decimal point numbers";
    public static final String QUOTE_PRICE_TOO_LONG = "Quote price must be 15 characters or less";
    public static final String QUOTE_TIME_NON_NUMERIC = "Estimated time must not be empty and must be numeric";
    public static final String QUOTE_DESCRIPTION_OVER_512_CHARS = "Quote description must be 512 characters or less";
    public static final String CONTACT_DETAILS_EMPTY = "Contact details cannot both be empty";
    public static final String EMAIL_INVALID = "Email address must be in the form 'jane@doe.nz'";
    public static final String PHONE_NUMBER_INVALID = "Phone number must be a valid phone number";

    @Test
    public void checkIfAlreadyQuoted_HasQuoted_ReturnTrue() {
        Quote quote = new Quote("250", "2d", "jane@example.com", "0123456789", "Desc");
        User user = new User("Jane", "Doe", "jane@example.com", "Pass", null, null);
        Job job = new Job("Example", "Desc", "11/10/25", "12/10/25");
        job.setId(1L);
        quote.setUser(user);
        quote.setJob(job);
        List<Quote> quotes = new ArrayList<>();
        quotes.add(quote);
        when(quoteRepository.findAllByUser(any())).thenReturn(quotes);
        Assertions.assertTrue(quoteService.checkIfAlreadyQuoted(user, job));
    }

    @Test
    public void checkIfAlreadyQuoted_HasNotQuotes_ReturnFalse() {
        User user = new User("Jane", "Doe", "jane@example.com", "Pass", null, null);
        Job job = new Job("Example", "Desc", "11/10/25", "12/10/25");
        when(quoteRepository.findAllByUser(any())).thenReturn(new ArrayList<Quote>());
        Assertions.assertFalse(quoteService.checkIfAlreadyQuoted(user, job));
    }

    @Test
    public void getErrorMessages_AllEmptyErrorsMessages_ReturnsTrue() {
        String testAllErrorMessages = QUOTE_PRICE_EMPTY + QUOTE_TIME_NON_NUMERIC + QUOTE_DESCRIPTION_OVER_512_CHARS + CONTACT_DETAILS_EMPTY;
        Assertions.assertEquals(List.of(QUOTE_PRICE_EMPTY, QUOTE_TIME_NON_NUMERIC, QUOTE_DESCRIPTION_OVER_512_CHARS,
                CONTACT_DETAILS_EMPTY, ""), quoteService.getErrorMessages(testAllErrorMessages));
    }

    @Test
    public void getErrorMessages_AllInvalidErrorMessages_ReturnsTrue() {
        String testAllErrorMessages = QUOTE_PRICE_INVALID + QUOTE_TIME_NON_NUMERIC + QUOTE_DESCRIPTION_OVER_512_CHARS + EMAIL_INVALID + PHONE_NUMBER_INVALID;
        Assertions.assertEquals(List.of(QUOTE_PRICE_INVALID, QUOTE_TIME_NON_NUMERIC, QUOTE_DESCRIPTION_OVER_512_CHARS,
                EMAIL_INVALID, PHONE_NUMBER_INVALID), quoteService.getErrorMessages(testAllErrorMessages));
    }

    @Test
    public void getErrorMessages_OnlyQuotePriceTooLong_ReturnsTrue() {
        String testAllErrorMessages = QUOTE_PRICE_TOO_LONG;
        Assertions.assertEquals(List.of(QUOTE_PRICE_TOO_LONG, "", "", "", ""), quoteService.getErrorMessages(testAllErrorMessages));
    }

    @Test
    public void retractQuote_ShouldRemoveQuoteByIdWithCorrectId() {
        Quote quote = new Quote();
        quote.setId(1L);
        quoteService.retractQuote(quote);
        verify(quoteRepository).deleteQuoteById(1L);
    }

    @Test
    public void setQuoteAsRated() {
        Quote quote = new Quote("10", "12", "email@example.com", "1234567890", "Quote");
        User user = new User("Jane", "Doe", "jane@example.com", "Pass", null, null);
        user.setId(1L);
        quote.setUser(user);

        when(quoteService.getAcceptedQuotes(1L)).thenReturn(List.of(quote));
        when(quoteRepository.save(quote)).thenReturn(quote);

        quoteService.setQuoteAsRated(1L, 1L);
        Assertions.assertTrue(quote.getRated());
    }

    @Test
    public void compareQuote_ShouldReturnAllTrue() {
        when(userService.getCompletedJobsUserHasWorkedOn(1L)).thenReturn(List.of(new Job(), new Job()));
        when(userService.getCompletedJobsUserHasWorkedOn(2L)).thenReturn(List.of(new Job()));

        when(userService.getUsersWorkEfficiency(1L)).thenReturn(75.0);
        when(userService.getUsersWorkEfficiency(2L)).thenReturn(85.0);

        Quote quote = new Quote("10", "15", "email@example.com", "1234567890", "Quote");
        Quote quote2 = new Quote("25", "20", "email@example.com", "1234567890", "Quote");

        User user = new User("Jane", "Doe", "jane@example.com", "Pass", null, null);
        user.setId(1L);
        User user2 = new User("John", "Doe", "john@example.com", "Pass", null, null);
        user2.setId(2L);

        Rating rating = new Rating(5,user, user2);
        List<Rating> ratings = new ArrayList<>();
        ratings.add(rating);
        user.setReceivedRatings(ratings);

        quote.setUser(user);
        quote2.setUser(user2);

        List<Boolean> trueList = List.of(true, true, true, true, true);
        Assertions.assertEquals(trueList, quoteService.compareQuotes(quote, quote2));
    }

    @Test
    public void compareQuote_ShouldReturnAllFalse() {
        when(userService.getCompletedJobsUserHasWorkedOn(1L)).thenReturn(List.of(new Job(), new Job()));
        when(userService.getCompletedJobsUserHasWorkedOn(2L)).thenReturn(List.of(new Job()));

        when(userService.getUsersWorkEfficiency(1L)).thenReturn(75.0);
        when(userService.getUsersWorkEfficiency(2L)).thenReturn(85.0);

        Quote quote = new Quote("10", "15", "email@example.com", "1234567890", "Quote");
        Quote quote2 = new Quote("25", "20", "email@example.com", "1234567890", "Quote");

        User user = new User("Jane", "Doe", "jane@example.com", "Pass", null, null);
        user.setId(1L);
        User user2 = new User("John", "Doe", "john@example.com", "Pass", null, null);
        user2.setId(2L);

        Rating rating = new Rating(5,user, user2);
        List<Rating> ratings = new ArrayList<>();
        ratings.add(rating);
        user.setReceivedRatings(ratings);

        quote.setUser(user);
        quote2.setUser(user2);

        List<Boolean> trueList = List.of(false, false, false, false, false);
        Assertions.assertEquals(trueList, quoteService.compareQuotes(quote2, quote));
    }

}

