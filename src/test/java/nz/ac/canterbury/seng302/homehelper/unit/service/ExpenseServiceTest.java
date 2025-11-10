package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.*;
import nz.ac.canterbury.seng302.homehelper.repository.ExpenseRepository;
import nz.ac.canterbury.seng302.homehelper.service.ExpenseService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock
    public ExpenseRepository expenseRepository;
    @InjectMocks
    public ExpenseService expenseService;

    @Test
    public void getNumPages_JobHas12PagesExpenses_Returns12Pages() {
        Job job = new Job("Job", "cool", "19/07/2025", "20/07/2025");
        for (int i = 0; i < 100; i++) {
            Expense expense = new Expense("10", "cool", "Material", "19/07/2025");
            job.addExpense(expense);
        }
        Assertions.assertEquals(12, expenseService.getNumPages(job.getExpenses()));
    }

    @Test
    public void getNumPages_JobHasNoExpenses_ReturnsNoPages() {
        Job job = new Job("Job", "cool", "19/07/2025", "20/07/2025");
        Assertions.assertEquals(0, expenseService.getNumPages(job.getExpenses()));
    }

    @Test
    public void getPageList_JobHas12PagesExpensesOnPage1_ReturnsPageNumbers() {
        Job job = new Job("Job", "cool", "19/07/2025", "20/07/2025");
        for (int i = 0; i < 100; i++) {
            Expense expense = new Expense("10", "cool", "Material", "19/07/2025");
            job.addExpense(expense);
        }
        Assertions.assertEquals(List.of(1, 2, 3, 12), expenseService.getPageList(1, job.getExpenses()));

    }

    @Test
    public void getPageList_JobHas12PagesExpensesOnPage3_ReturnsPageNumbers() {
        Job job = new Job("Job", "cool", "19/07/2025", "20/07/2025");
        for (int i = 0; i < 100; i++) {
            Expense expense = new Expense("10", "cool", "Material", "19/07/2025");
            job.addExpense(expense);
        }
        Assertions.assertEquals(List.of(1, 2, 3, 4, 5, 12), expenseService.getPageList(3, job.getExpenses()));
    }

    @Test
    public void getPageList_JobHas3PagesExpensesOnPage3_ReturnsPageNumbers() {
        Job job = new Job("Job", "cool", "19/07/2025", "20/07/2025");
        for (int i = 0; i < 25; i++) {
            Expense expense = new Expense("10", "cool", "Material", "19/07/2025");
            job.addExpense(expense);
        }
        Assertions.assertEquals(List.of(1, 2, 3), expenseService.getPageList(3, job.getExpenses()));
    }

    @Test
    public void getPageList_JobHas3PagesExpensesOnPage1_ReturnsPageNumbers() {
        Job job = new Job("Job", "cool", "19/07/2025", "20/07/2025");
        for (int i = 0; i < 25; i++) {
            Expense expense = new Expense("10", "cool", "Material", "19/07/2025");
            job.addExpense(expense);
        }
        Assertions.assertEquals(List.of(1, 2, 3), expenseService.getPageList(1, job.getExpenses()));
    }

    @Test
    public void getPageList_JobHas1PageExpensesOnPage1_ReturnsPageNumbers() {
        Job job = new Job("Job", "cool", "19/07/2025", "20/07/2025");
        for (int i = 0; i < 7; i++) {
            Expense expense = new Expense("10", "cool", "Material", "19/07/2025");
            job.addExpense(expense);
        }
        Assertions.assertEquals(List.of(1), expenseService.getPageList(1, job.getExpenses()));
    }

    @Test
    public void transferQuoteToExpense_ExpenseCreated() {
        Quote quote = new Quote("50", "400", "email@email.com", "021010101010", "description" );
        User testUser = new User("Alexander", "Cheals", "123@123.123", "password", null, null);
        quote.setUser(testUser);
        Job job = new Job("Job", "cool", "19/07/2025", "20/07/2025");
        expenseService.transferQuoteToExpense(quote, job);

        ArgumentCaptor<Expense> expenseArgumentCaptor = ArgumentCaptor.forClass(Expense.class);
        Mockito.verify(expenseRepository, Mockito.times(1)).save(expenseArgumentCaptor.capture());
        Expense expense = expenseArgumentCaptor.getValue();
        Assertions.assertEquals(quote.getPrice(), expense.getCost());
        Assertions.assertEquals("Quote", expense.getCategory());
        Assertions.assertEquals("Quote from Alexander Cheals", expense.getDescription());
    }
}
