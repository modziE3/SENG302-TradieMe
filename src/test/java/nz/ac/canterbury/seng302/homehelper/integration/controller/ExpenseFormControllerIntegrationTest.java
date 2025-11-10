package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.controller.job.ExpenseFormController;
import nz.ac.canterbury.seng302.homehelper.entity.Expense;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.repository.ExpenseRepository;
import nz.ac.canterbury.seng302.homehelper.repository.JobRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.service.ExpenseService;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ActiveProfiles("test")
@SpringBootTest
public class ExpenseFormControllerIntegrationTest {
    @Autowired
    private ExpenseFormController expenseFormController;
    private MockMvc mockMvc;
    @Autowired
    private ExpenseService expenseService;
    @Autowired
    private JobService jobService;
    @Autowired
    private RenovationRecordService renovationRecordService;
    @MockBean
    private ExpenseRepository expenseRepository;
    @MockBean
    private JobRepository jobRepository;
    @MockBean
    private RenovationRecordRepository renovationRecordRepository;

    private RenovationRecord record;
    private Job job;
    private Principal principal;

    @PostConstruct
    public void setupMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(expenseFormController).build();
    }

    @BeforeEach
    public void setup() {
        principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("john@example.com");

        record = new RenovationRecord("Renovation", "Renovation", new ArrayList<>(),
                "john@example.com");
        record.setId(1L);
        job = new Job("Job", "Job", "No Category", null);
        job.setId(1L);
        job.setRenovationRecord(record);
        record.setJobs(List.of(job));
        Mockito.when(renovationRecordRepository.findById(record.getId())).thenReturn(Optional.of(record));
        Mockito.when(jobRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(job));
    }

    @Test
    public void getExpenseForm() throws Exception {
        mockMvc.perform(get("/my-renovations/add-expense")
                    .param("recordId", Long.toString(record.getId()))
                    .param("jobId", Long.toString(job.getId())))
                .andExpect(view().name("expenseFormTemplate"))
                .andExpect(model().attribute("recordId", record.getId()))
                .andExpect(model().attribute("jobId", job.getId()));
    }

    static Stream<Arguments> validExpenseInputs() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        String todayDateString = dateFormat.format(new Date());
        return Stream.of(
                Arguments.of("1", "A", "No Category", todayDateString),
                Arguments.of("1", "A".repeat(512), "No Category", todayDateString),
                Arguments.of("1.1", "A", "No Category", todayDateString),
                Arguments.of(".1", "A", "No Category", todayDateString),
                Arguments.of("1", "A","No Category", "01/01/2025")
        );
    }
    @ParameterizedTest
    @MethodSource("validExpenseInputs")
    public void postExpenseForm_ValidInputs_RedirectToDetailsPage(
            String cost, String description, String category, String date) throws Exception {
        mockMvc.perform(post("/my-renovations/add-expense")
                        .param("recordId", Long.toString(record.getId()))
                        .param("jobId", Long.toString(job.getId()))
                        .param("expenseCost", cost)
                        .param("description", description)
                        .param("category", category)
                        .param("expenseDate", date)
                        .principal(principal))
                .andExpect(view().name("redirect:/my-renovations/job-details?jobId=" + job.getId()));

        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        Mockito.verify(expenseRepository).save(expenseCaptor.capture());
        Expense capturedExpense = expenseCaptor.getValue();
        Assertions.assertEquals(cost, capturedExpense.getCost());
        Assertions.assertEquals(description, capturedExpense.getDescription());
        Assertions.assertEquals(category, capturedExpense.getCategory());
        Assertions.assertEquals(date, capturedExpense.getDate());
    }

    static Stream<Arguments> invalidCosts() {
        return Stream.of(
                Arguments.of("", ExpenseService.EXPENSE_COST_EMPTY),
                Arguments.of("1.", ExpenseService.EXPENSE_COST_INVALID),
                Arguments.of("1.1.1", ExpenseService.EXPENSE_COST_INVALID)
        );
    }
    @ParameterizedTest
    @MethodSource("invalidCosts")
    public void postExpenseForm_InvalidCost_ErrorMessageShows(String cost, String expectedErrorMessage) throws Exception {
        mockMvc.perform(post("/my-renovations/add-expense")
                        .param("recordId", Long.toString(record.getId()))
                        .param("jobId", Long.toString(job.getId()))
                        .param("expenseCost", cost)
                        .param("description", "Description")
                        .param("category", "No Category")
                        .param("expenseDate", "01/01/2025")
                        .principal(principal))
                .andExpect(view().name("expenseFormTemplate"))
                .andExpect(model().attribute("expenseCost", cost))
                .andExpect(model().attribute("description", "Description"))
                .andExpect(model().attribute("category", "No Category"))
                .andExpect(model().attribute("expenseDate", "01/01/2025"))
                .andExpect(model().attribute("expenseCostErrorMessage", expectedErrorMessage));
    }

    static Stream<Arguments> invalidDescriptions() {
        return Stream.of(
                Arguments.of("", ExpenseService.EXPENSE_DESCRIPTION_EMPTY),
                Arguments.of("A".repeat(513), ExpenseService.EXPENSE_DESCRIPTION_TOO_LONG)
        );
    }
    @ParameterizedTest
    @MethodSource("invalidDescriptions")
    public void postExpenseForm_InvalidDescription_ErrorMessageShows(String description, String expectedErrorMessage) throws Exception {
        mockMvc.perform(post("/my-renovations/add-expense")
                        .param("recordId", Long.toString(record.getId()))
                        .param("jobId", Long.toString(job.getId()))
                        .param("expenseCost", "1.5")
                        .param("description", description)
                        .param("category", "No Category")
                        .param("expenseDate", "01/01/2025")
                        .principal(principal))
                .andExpect(view().name("expenseFormTemplate"))
                .andExpect(model().attribute("expenseCost", "1.5"))
                .andExpect(model().attribute("description", description))
                .andExpect(model().attribute("category", "No Category"))
                .andExpect(model().attribute("expenseDate", "01/01/2025"))
                .andExpect(model().attribute("descriptionErrorMessage", expectedErrorMessage));

        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        Mockito.verify(expenseRepository, Mockito.never()).save(expenseCaptor.capture());
    }

    static Stream<Arguments> invalidDates() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, +1);

        return Stream.of(
                Arguments.of("3025/01/01", ExpenseService.EXPENSE_DATE_INVALID_FORMAT),
                Arguments.of("01-01-3025", ExpenseService.EXPENSE_DATE_INVALID_FORMAT),
                Arguments.of(sdf.format(calendar.getTime()), ExpenseService.EXPENSE_DATE_IN_FUTURE),
                Arguments.of("30/02/3025", ExpenseService.EXPENSE_DATE_INVALID_DATE)
        );
    }
    @ParameterizedTest
    @MethodSource("invalidDates")
    void postExpenseForm_InvalidDate_ErrorMessageShows(String invalidDate, String expectedErrorMessage) throws Exception {
        mockMvc.perform(post("/my-renovations/add-expense")
                        .param("recordId", Long.toString(record.getId()))
                        .param("jobId", Long.toString(job.getId()))
                        .param("expenseCost", "1.5")
                        .param("description", "Description")
                        .param("category", "No Category")
                        .param("expenseDate", invalidDate)
                        .principal(principal))
                .andExpect(view().name("expenseFormTemplate"))
                .andExpect(model().attribute("expenseCost", "1.5"))
                .andExpect(model().attribute("description", "Description"))
                .andExpect(model().attribute("category", "No Category"))
                .andExpect(model().attribute("expenseDate", invalidDate))
                .andExpect(model().attribute("expenseDateErrorMessage", expectedErrorMessage));

        ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
        Mockito.verify(expenseRepository, Mockito.never()).save(expenseCaptor.capture());
    }
}
