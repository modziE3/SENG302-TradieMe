package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.controller.quote.SubmitQuoteController;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.Quote;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.JobRepository;
import nz.ac.canterbury.seng302.homehelper.repository.QuoteRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.QuoteService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@ActiveProfiles("test")
public class SubmitQuoteControllerIntegrationTest {
    @Autowired
    private SubmitQuoteController submitQuoteController;
    @Autowired
    private QuoteService quoteService;
    @Autowired
    private UserService userService;
    @Autowired
    private JobService jobService;
    @MockBean
    private QuoteRepository quoteRepository;
    @MockBean
    private JobRepository jobRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private Principal principal;
    @MockBean
    private EmailService emailService;

    private MockMvc mockMvc;
    private Job job;
    private User user;

    @PostConstruct
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(submitQuoteController).build();
        job = new Job("Job", "Job", null, null);
        job.setId(1L);
        user = new User("jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        when(jobRepository.findById(Mockito.anyLong())).thenReturn(Optional.ofNullable(job));
        when(userRepository.findByEmailContainingIgnoreCase(Mockito.anyString())).thenReturn(user);
        when(principal.getName()).thenReturn("jane@doe.nz");
    }

    @Test
    public void postSubmitQuote_ValidDetailsEntered_QuoteIsCreated() throws Exception {
        mockMvc.perform(post("/submit-quote")
                .param("jobId", job.getId().toString())
                .param("price", "2")
                .param("workTime", "10")
                .param("email", "jane@doe.nz")
                .param("phoneNumber", "123456789")
                .param("description", "Hi")
                .principal(principal))
                .andExpect(view().name("redirect:/my-renovations/job-details?jobId="+job.getId()+"&fromSearch=true"));

        ArgumentCaptor<Quote> quoteCaptor = ArgumentCaptor.forClass(Quote.class);
        Mockito.verify(quoteRepository).save(quoteCaptor.capture());
        Quote capturedQuote = quoteCaptor.getValue();
        Assertions.assertEquals("2", capturedQuote.getPrice());
        Assertions.assertEquals("10", capturedQuote.getWorkTime());
        Assertions.assertEquals("jane@doe.nz", capturedQuote.getEmail());
        Assertions.assertEquals("123456789", capturedQuote.getPhoneNumber());
        Assertions.assertEquals("Hi", capturedQuote.getDescription());
    }

    @Test
    public void postSubmitQuote_InvalidPriceEntered_ErrorIsShown() throws Exception {
        mockMvc.perform(post("/submit-quote")
                        .param("jobId", job.getId().toString())
                        .param("price", "hello")
                        .param("workTime", "10")
                        .param("email", "jane@doe.nz")
                        .param("phoneNumber", "123456789")
                        .param("description", "Hi")
                        .principal(principal))
                .andExpect(model().attribute("priceErrorMessage", "Quote price must only include positive numbers, including decimal point numbers"));
    }

    @Test
    public void postSubmitQuote_PriceTooLong_ErrorIsShown() throws Exception {
        mockMvc.perform(post("/submit-quote")
                        .param("jobId", job.getId().toString())
                        .param("price", "1000000000000000000000000000000000000000000000000000000000000")
                        .param("workTime", "10")
                        .param("email", "jane@doe.nz")
                        .param("phoneNumber", "123456789")
                        .param("description", "Hi")
                        .principal(principal))
                .andExpect(model().attribute("priceErrorMessage", "Quote price must be 15 characters or less"));
    }

    @Test
    public void postSubmitQuote_WorkTimeNonNumeric_ErrorIsShown() throws Exception {
        mockMvc.perform(post("/submit-quote")
                        .param("jobId", job.getId().toString())
                        .param("price", "2")
                        .param("workTime", "hello there")
                        .param("email", "jane@doe.nz")
                        .param("phoneNumber", "123456789")
                        .param("description", "Hi")
                        .principal(principal))
                .andExpect(model().attribute("workTimeErrorMessage", "Estimated time must not be empty and must be numeric"));
    }

    @Test
    public void postSubmitQuote_DescriptionEmpty_ErrorIsShown() throws Exception {
        mockMvc.perform(post("/submit-quote")
                        .param("jobId", job.getId().toString())
                        .param("price", "2")
                        .param("workTime", "10")
                        .param("email", "jane@doe.nz")
                        .param("phoneNumber", "123456789")
                        .param("description", "lldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjk" +
                                "ncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvlll" +
                                "ncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvlll" +
                                "ncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvlll" +
                                "ncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvlll" +
                                "ncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvlll" +
                                "ncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvlll" +
                                "ncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvlll" +
                                "ncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvlll" +
                                "ncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvlll" +
                                "ncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvlll" +
                                "ncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvlll" +
                                "ncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvlll" +
                                "ncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvllldjkncvlll")
                        .principal(principal))
                .andExpect(model().attribute("descriptionErrorMessage", "Quote description must be 512 characters or less"));
    }

    @Test
    public void postSubmitQuote_NoContactDetails_ErrorIsShown() throws Exception {
        mockMvc.perform(post("/submit-quote")
                        .param("jobId", job.getId().toString())
                        .param("price", "2")
                        .param("workTime", "10")
                        .param("email", "")
                        .param("phoneNumber", "")
                        .param("description", "Hi")
                        .principal(principal))
                .andExpect(model().attribute("emailErrorMessage", "Contact details cannot both be empty"));
    }

    @Test
    public void postSubmitQuote_InvalidEmail_ErrorIsShown() throws Exception {
        mockMvc.perform(post("/submit-quote")
                        .param("jobId", job.getId().toString())
                        .param("price", "2")
                        .param("workTime", "10")
                        .param("email", "123")
                        .param("phoneNumber", "123456789")
                        .param("description", "Hi")
                        .principal(principal))
                .andExpect(model().attribute("emailErrorMessage", "Email address must be in the form 'jane@doe.nz'"));
    }

    @Test
    public void postSubmitQuote_InvalidPhoneNumber_ErrorIsShown() throws Exception {
        mockMvc.perform(post("/submit-quote")
                        .param("jobId", job.getId().toString())
                        .param("price", "2")
                        .param("workTime", "10")
                        .param("email", "jane@doe.nz")
                        .param("phoneNumber", "hello there")
                        .param("description", "Hi")
                        .principal(principal))
                .andExpect(model().attribute("phoneNumberErrorMessage", "Phone number must be a valid phone number"));
    }

    @Test
    public void PostSubmitQuote_AlreadyQuoted_QuotedErrorIsShown() throws Exception {
        Quote quote = new Quote("13", "3", "jane@doe.nz", "123456789", "hello there");
        quote.setJob(job);
        List<Quote> quotes = new ArrayList<>();
        quotes.add(quote);
        when(quoteRepository.findAllByUser(user)).thenReturn(quotes);
        mockMvc.perform(post("/submit-quote")
                        .param("jobId", job.getId().toString())
                        .param("price", "2")
                        .param("workTime", "10")
                        .param("email", "jane@doe.nz")
                        .param("phoneNumber", "123456789")
                        .param("description", "Hi")
                        .principal(principal))
                .andExpect(model().attribute("quotedErrorMessage", "You have already submitted a quote for this job"));
    }

}
