package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.controller.quote.MyQuotesController;
import nz.ac.canterbury.seng302.homehelper.entity.*;
import nz.ac.canterbury.seng302.homehelper.repository.*;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.QuoteService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Assertions;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class MyQuotesControllerIntegrationTest {
    @Autowired
    private MyQuotesController myQuotesController;
    @Autowired
    private QuoteService quoteService;
    @Autowired
    private JobService jobService;
    @Autowired
    private RenovationRecordService renovationRecordService;
    @Autowired
    private UserService userService;
    @MockBean
    private QuoteRepository quoteRepository;
    @MockBean
    private RenovationRecordRepository renovationRecordRepository;
    @MockBean
    private JobRepository jobRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private Principal principalJohn;
    @MockBean
    private EmailService emailService;
    @MockBean
    private ExpenseRepository expenseRepository;

    private MockMvc mockMvc;

    private List<Quote> quotes;
    private Quote johnQuote;
    private List<RenovationRecord> records;
    private List<Job> jobs;
    private User user;

    @PostConstruct
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(myQuotesController).build();
        User jane = new User("jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        User john = new User("john", "Doe", "john@doe.nz", "P4$$word", null, null);
        Job jobJane = new Job("Example Job Jane", "Example Job Jane", "13/10/2025", "13/10/2025");
        Job jobJohn = new Job("Example Job John", "Example Job John", "13/10/2025", "13/10/2025");
        jobJane.setType("Carpentry");
        jobJohn.setType("Carpentry");
        RenovationRecord recordJane = new RenovationRecord("janeRecord", "desc", List.of(), "jane@doe.nz");
        RenovationRecord recordJohn = new RenovationRecord("johnRecord", "desc", List.of(), "john@doe.nz");
        renovationRecordService.addRenovationRecord(recordJane);
        renovationRecordService.addRenovationRecord(recordJohn);
        jobJane.setRenovationRecord(recordJane);
        jobJohn.setRenovationRecord(recordJohn);
        jobJane.setIsPosted(true);
        jobJohn.setIsPosted(true);
        jobService.addJob(jobJane);
        jobService.addJob(jobJohn);

        principalJohn = Mockito.mock(Principal.class);
        when(principalJohn.getName()).thenReturn(john.getEmail());

        when(userRepository.findByEmailContainingIgnoreCase(john.getEmail())).thenReturn(john);
        when(userRepository.findByEmailContainingIgnoreCase(jane.getEmail())).thenReturn(jane);

        quotes = new ArrayList<>();

        when(quoteRepository.findSentQuotes(anyString(), anyString()))
                .then(i -> quotes.stream()
                        .filter(q -> i.getArgument(0) != null && q.getUser().getEmail().equalsIgnoreCase(i.getArgument(0)) && q.getStatus().equalsIgnoreCase(i.getArgument(1)))
                        .toList());
        when(quoteRepository.findReceivedQuotes(anyString(), anyString()))
                .then(i -> quotes.stream()
                        .filter(q -> i.getArgument(0) != null && q.getJob().getRenovationRecord().getUserEmail().equalsIgnoreCase(i.getArgument(0)) && q.getStatus().equalsIgnoreCase(i.getArgument(1)))
                        .toList());

        Random rand = new Random();
        for (int i = 1; i <= 200; i++) {
            try {
                Quote quoteJohn = new Quote("" + rand.nextInt(1, 100), "" + rand.nextInt(1, 24), john.getEmail(), "123456", "Quote");
                quoteJohn.setJob(jobJane);
                quoteJohn.setUser(john);
                quoteJohn.setStatus(List.of("Pending", "Accepted", "Rejected").get(rand.nextInt(0,3)));
                quotes.add(quoteJohn);

                Quote quoteJane = new Quote("" + rand.nextInt(1, 100), "" + rand.nextInt(1, 24), jane.getEmail(), "123456", "Quote");
                quoteJane.setJob(jobJohn);
                quoteJane.setUser(jane);
                quoteJane.setStatus(List.of("Pending", "Accepted", "Rejected").get(rand.nextInt(0,3)));
                quotes.add(quoteJane);
            } catch (Exception e) {}
        }
        johnQuote = new Quote("50", "12" , jane.getEmail(), "123456", "Quote");
        johnQuote.setJob(jobJohn);
        johnQuote.setUser(jane);
        johnQuote.setStatus("Pending");
        quotes.add(johnQuote);

    }

    @Test
    public void getMyQuotesPage_SentAndReceivedQuotesExist_QuotesShownOnPage() throws Exception {
        mockMvc.perform(get("/my-quotes")
                    .principal(principalJohn))
                .andExpect(status().isOk())
                .andExpect(view().name("myQuotesTemplate"))
                .andExpect(model().attributeExists("sentQuotes"))
                .andExpect(model().attributeExists("receivedQuotes"));
    }

    @ParameterizedTest
    @CsvSource({
            "Pending",
            "Accepted",
            "Rejected"
    })
    public void getMyQuotesPage_FilterByStatus_AllQuotesHaveGivenStatus(String status) throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/my-quotes")
                        .param("status", status)
                        .principal(principalJohn))
                .andExpect(status().isOk())
                .andExpect(view().name("myQuotesTemplate"))
                .andReturn();

        List<Quote> quotes = (List<Quote>) mvcResult.getModelAndView().getModel().get("sentQuotes");
        Assertions.assertFalse(quotes.isEmpty());
        Assertions.assertNotNull(quotes);
        for (Quote quote : quotes) {
            Assertions.assertEquals(status.toLowerCase(), quote.getStatus().toLowerCase());
        }
    }

    @ParameterizedTest
    @CsvSource({
            "Pending",
            "Accepted",
            "Rejected"
    })
    public void paginationTest(String status) throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/my-quotes")
                        .param("status", status)
                        .param("sentPage", "1")
                        .param("receivedPage", "1")
                        .principal(principalJohn))
                .andExpect(status().isOk())
                .andExpect(view().name("myQuotesTemplate"))
                .andReturn();

        List<Quote> sentQuotes = (List<Quote>) mvcResult.getModelAndView().getModel().get("sentQuotes");
        Assertions.assertFalse(sentQuotes.isEmpty());
        Assertions.assertEquals(15, sentQuotes.size());
        Assertions.assertNotNull(sentQuotes);
        for (Quote quote : sentQuotes) {
            Assertions.assertEquals(status.toLowerCase(), quote.getStatus().toLowerCase());
        }
    }

    @Test
    public void rejectQuotesTest() throws Exception {

        when(quoteRepository.findById(1L)).thenReturn(Optional.ofNullable(johnQuote));
        MvcResult mvcResult = mockMvc.perform(post("/reject-quote")
                        .param("quoteId", "1")
                        .principal(principalJohn))
                .andExpect(view().name("redirect:/my-quotes"))
                .andReturn();

        ArgumentCaptor<Quote> quoteCaptor = ArgumentCaptor.forClass(Quote.class);
        Mockito.verify(quoteRepository).save(quoteCaptor.capture());

        Quote quote = quoteCaptor.getValue();
        Assertions.assertEquals("rejected", quote.getStatus().toLowerCase());
    }

    @Test
    public void acceptQuote_WithRetract_RedirectedToMyQuotesPage() throws Exception {
        when(principalJohn.getName()).thenReturn("john@doe.nz");
        when(quoteRepository.findById(1L)).thenReturn(Optional.ofNullable(johnQuote));

        mockMvc.perform(post("/accept-quote")
                .param("quoteId", "1")
                .param("retract", "true")
                .principal(principalJohn))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<Quote> quoteCaptor = ArgumentCaptor.forClass(Quote.class);
        Mockito.verify(quoteRepository).save(quoteCaptor.capture());
        Quote capturedQuote = quoteCaptor.getValue();
        Assertions.assertFalse(capturedQuote.getJob().getIsPosted());
    }

    @Test
    public void acceptQuote_WithoutRetract_RedirectedToMyQuotesPage() throws Exception {
        when(principalJohn.getName()).thenReturn("john@doe.nz");
        when(quoteRepository.findById(1L)).thenReturn(Optional.ofNullable(johnQuote));

        mockMvc.perform(post("/accept-quote")
                        .param("quoteId", "1")
                        .param("retract", "false")
                        .principal(principalJohn))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<Quote> quoteCaptor = ArgumentCaptor.forClass(Quote.class);
        Mockito.verify(quoteRepository).save(quoteCaptor.capture());
        Quote capturedQuote = quoteCaptor.getValue();
        Assertions.assertTrue(capturedQuote.getJob().getIsPosted());
    }

    @Test
    public void acceptQuote_WithoutRetractWithExpenseCreation_RedirectedToMyQuotesPageExpenseSaved() throws Exception {
        when(principalJohn.getName()).thenReturn("john@doe.nz");
        when(quoteRepository.findById(1L)).thenReturn(Optional.ofNullable(johnQuote));

        mockMvc.perform(post("/accept-quote")
                        .param("quoteId", "1")
                        .param("retract", "false")
                        .param("transfer", "true")
                        .principal(principalJohn))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<Expense> expenseArgumentCaptor = ArgumentCaptor.forClass(Expense.class);
        Mockito.verify(expenseRepository, Mockito.times(1)).save(expenseArgumentCaptor.capture());
        Expense expense = expenseArgumentCaptor.getValue();
        Assertions.assertEquals(johnQuote.getPrice(), expense.getCost());
        Assertions.assertEquals("Quote", expense.getCategory());
        Assertions.assertEquals("Quote from jane Doe", expense.getDescription());
    }

//    @Test
//    public void postMyQuotesPage_StatusRemainsAfterQuoteRemoval() throws Exception {
//        List<Quote> quotesList = new ArrayList<>(quotes);
//        Quote quote = quotes.get(3);
//        String status = "pending";
//        when(principalJohn.getName()).thenReturn(user.getEmail());
//        when(userService.getUser(user.getEmail())).thenReturn(user);
//        when(quoteService.getAllQuotes()).thenReturn(quotesList);
//        when(quoteService.getQuote(user.getEmail(), status, quote.getId())).thenReturn(quote);
//        doAnswer(invocation -> {
//            Quote q = invocation.getArgument(0);
//            quotesList.remove(q);
//            return null;
//        }).when(quoteService).retractQuote(any(Quote.class));
//        mockMvc.perform(post("/my-quotes")
//                        .param("quoteId", String.valueOf(quote.getId()))
//                        .param("status", status)
//                        .principal(principalJohn))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(view().name("redirect:/my-quotes?status=" + status));
//
//    }
//
//    @Test
//    public void postMyQuotesPage_RetractedQuotesGetRemoved() throws Exception {
//        List<Quote> quotesList = new ArrayList<>(quotes);
//        Quote quote = quotes.get(1);
//        String status = "accepted";
//        when(principalJohn.getName()).thenReturn(user.getEmail());
//        when(userService.getUser(user.getEmail())).thenReturn(user);
//        when(quoteService.getAllQuotes()).thenReturn(quotesList);
//        when(quoteService.getQuote(user.getEmail(), status, quote.getId())).thenReturn(quote);
//        doAnswer(invocation -> {
//            Quote q = invocation.getArgument(0);
//            quotesList.remove(q);
//            return null;
//        }).when(quoteService).retractQuote(any(Quote.class));
//        mockMvc.perform(post("/my-quotes")
//                        .param("quoteId", String.valueOf(quote.getId()))
//                        .param("status", status)
//                        .principal(principalJohn))
//                .andExpect(status().is3xxRedirection());
//
//        Assertions.assertFalse(quotesList.contains(quote));
//    }


}
