package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.Expense;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.Quote;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Service class for expenses
 */
@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ValidationService validationService = new ValidationService();
    private final UserService userService;

    private int numberOfExpenses = 9;

    public static final String EXPENSE_COST_EMPTY = "Expense cost cannot be empty";
    public static final String EXPENSE_COST_INVALID = "Expense cost must only include positive numbers, including decimal point numbers";
    public static final String EXPENSE_COST_TOO_LONG = "Expense cost must be 15 characters or less";
    public static final String EXPENSE_DESCRIPTION_EMPTY = "Expense description cannot be empty";
    public static final String EXPENSE_DESCRIPTION_TOO_LONG = "Expense description must be 512 characters or less";
    public static final String EXPENSE_DATE_INVALID_FORMAT = "Date is not in valid format, DD/MM/YYYY";
    public static final String EXPENSE_DATE_IN_FUTURE = "Date cannot be in the future";
    public static final String EXPENSE_DATE_INVALID_DATE = "Date is not a valid date";

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.userService = userService;
    }

    public List<Expense> getExpensesByJobId(Long jobId) {
        return expenseRepository.findAllByJobId(jobId);
    }

    /**
     * Adds an expense object to storage
     * @param expense expense object being stored
     * @return
     */
    public Expense addExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    /**
     * Validates the attributes of an expense object
     * @param expense expense object being validated
     * @throws IllegalArgumentException if there is at least one invalid expense attribute
     */
    public void validateExpense(Expense expense) throws IllegalArgumentException {
        List<String> errors = new ArrayList<>();

        if (validationService.stringEmpty(expense.getCost())) {
            errors.add(EXPENSE_COST_EMPTY);
        } else if (!validationService.correctExpenseCostFormat(expense.getCost())) {
            errors.add(EXPENSE_COST_INVALID);
        } else if (!validationService.correctExpenseCostLength(expense.getCost())) {
            errors.add(EXPENSE_COST_TOO_LONG);
        }

        if (validationService.stringEmpty(expense.getDescription())) {
            errors.add(EXPENSE_DESCRIPTION_EMPTY);
        } else if (!validationService.correctDescriptionLength(expense.getDescription())) {
            errors.add(EXPENSE_DESCRIPTION_TOO_LONG);
        }

        if (!validationService.correctDateFormat(expense.getDate())) {
            errors.add(EXPENSE_DATE_INVALID_FORMAT);
        } else {
            try {
                if (validationService.dateInTheFuture(expense.getDate())) {
                    errors.add(EXPENSE_DATE_IN_FUTURE);
                }
            } catch (ParseException e) {
                errors.add(EXPENSE_DATE_INVALID_DATE);
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }
    }

    /**
     * Goes through a string of all detected errors from the add expense form
     * and assigns an error message from the string to be displayed for each
     * field of the form
     * @param allErrorMessages String of all errors detected from the add expense form
     * @return List of error messages (one for each input field of the form) that will
     * be displayed on the form
     */
    public List<String> getErrorMessages(String allErrorMessages) {
        String expenseCostErrorMessage = "";
        if (allErrorMessages.contains(EXPENSE_COST_EMPTY)) {
            expenseCostErrorMessage = EXPENSE_COST_EMPTY;
        } else if (allErrorMessages.contains(EXPENSE_COST_INVALID)) {
            expenseCostErrorMessage = EXPENSE_COST_INVALID;
        } else if (allErrorMessages.contains(EXPENSE_COST_TOO_LONG)) {
            expenseCostErrorMessage = EXPENSE_COST_TOO_LONG;
        }

        String expenseDescriptionErrorMessage = "";
        if (allErrorMessages.contains(EXPENSE_DESCRIPTION_EMPTY)) {
            expenseDescriptionErrorMessage = EXPENSE_DESCRIPTION_EMPTY;
        } else if (allErrorMessages.contains(EXPENSE_DESCRIPTION_TOO_LONG)) {
            expenseDescriptionErrorMessage = EXPENSE_DESCRIPTION_TOO_LONG;
        }

        String expenseDateErrorMessage = "";
        if (allErrorMessages.contains(EXPENSE_DATE_INVALID_FORMAT)) {
            expenseDateErrorMessage = EXPENSE_DATE_INVALID_FORMAT;
        }
        if (allErrorMessages.contains(EXPENSE_DATE_IN_FUTURE)) {
            expenseDateErrorMessage = EXPENSE_DATE_IN_FUTURE;
        }
        if (allErrorMessages.contains(EXPENSE_DATE_INVALID_DATE)) {
            expenseDateErrorMessage = EXPENSE_DATE_INVALID_DATE;
        }

        return List.of(expenseCostErrorMessage, expenseDescriptionErrorMessage, expenseDateErrorMessage);
    }

    public void setNumberOfExpenses(int numberOfExpenses) {
        this.numberOfExpenses = numberOfExpenses;
    }

    /**
     * Gets the total number of pages of expenses when paginating by the set numberOfExpenses
     * Uses Math.ceil to round up the result so that the remaining expenses have a page.
     * @param expenses The expenses are being paginated.
     * @return The total number of pages to be displayed.
     */
    public int getNumPages(List<Expense> expenses) {
        if (numberOfExpenses == 0 || expenses == null || expenses.isEmpty()) {
            return 0;
        }
        return (int)Math.ceil((double) (expenses.size()) / numberOfExpenses);
    }

    /**
     * Gets the Slice batch object of expenses on the given page.
     * Uses the set numberOfExpenses to determine the number of jobs to be included in the Slice.
     * @param page The page number to select. (Offset for the database query.)
     * @param job The job for the expenses to be selected.
     * @return The Slice object containing the batch of expenses.
     */
    public Slice<Expense> getExpensePages(Integer page, Job job) {
        Integer userInputDifference = 1;
        Pageable pageRequest  = PageRequest.of(page - userInputDifference, numberOfExpenses);
        return expenseRepository.findAllFromJob(job, pageRequest);
    }

    public Slice<Expense> getExpensePagesFromRecord(Integer page, RenovationRecord record) {
        Integer userInputDifference = 1;
        Pageable pageRequest  = PageRequest.of(page - userInputDifference, numberOfExpenses);
        return expenseRepository.findAllFromRecord(record, pageRequest);
    }


    /**
     * Gets a list of the pages to be displayed for expense pagination.
     * If the list is more than 10 then it gets the 4 pages surrounding the current page and the final and first page and returns
     * the list as distinct elements that are filtered to be between the final and 0.
     * Makes the check and creates different lists in order to match the AC3 of UserStory 15
     * Used by the JobDetailsPageController
     * @param currentPage the currently active page to base the others off of.
     * @param expenses list of expenses to page through.
     * @return A list of the visible pagination button pages.
     */
    public List<Integer> getPageList(Integer currentPage, List<Expense> expenses) {
        int numPages = getNumPages(expenses);
        if (numPages > 10) {
            return Stream.of(1, currentPage - 2, currentPage - 1, currentPage, currentPage + 1, currentPage + 2, numPages)
                    .filter(num -> num > 0 && num <= numPages).distinct().toList();
        } else {
            return IntStream.range(1, (numPages + 1)).boxed().toList();
        }
    }

    /**
     * Checks if a page number falls inside the page numbers for a list of expenses
     * @param pageList list of page numbers
     * @param pageNumber page number being validation
     * @return true if page number in page list, false otherwise
     */
    public Boolean pageNumberIsInExpensePageList(List<Integer> pageList, Integer pageNumber) {
        return validationService.pageNumberIsInPageList(pageList, pageNumber);
    }

    /**
     * Creates a expense for a given quote and saves it to the job given
     * @param quote The quote which a expense is being made for
     * @param job The job which the expense is being added to
     */
    public void transferQuoteToExpense(Quote quote, Job job) {
        String expenseDescription = "Quote from " + quote.getUser().getFirstName() + " " + quote.getUser().getLastName();
        LocalDate expenseDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Expense expense = new Expense(quote.getPrice(), expenseDescription, "Quote", expenseDate.format(formatter));
        expense.setJob(job);
        validateExpense(expense);
        expenseRepository.save(expense);
    }
}
