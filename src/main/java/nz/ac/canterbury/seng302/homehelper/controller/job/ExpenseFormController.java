package nz.ac.canterbury.seng302.homehelper.controller.job;

import nz.ac.canterbury.seng302.homehelper.controller.account.RegistrationFormController;
import nz.ac.canterbury.seng302.homehelper.entity.Expense;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.service.ExpenseService;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class ExpenseFormController {
    Logger logger = LoggerFactory.getLogger(RegistrationFormController.class);

    private final ExpenseService expenseService;
    private final JobService jobService;
    private final RenovationRecordService renovationRecordService;

    @Autowired
    public ExpenseFormController(ExpenseService expenseService, JobService jobService, RenovationRecordService renovationRecordService) {
        this.expenseService = expenseService;
        this.jobService = jobService;
        this.renovationRecordService = renovationRecordService;
    }

    /**
     * Gets the add expense form page
     * @param recordId ID of the renovation record
     * @param jobId ID of the job
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @return thymeleaf template for the add expense form
     */
    @GetMapping("/my-renovations/add-expense")
    public String getCreateExpenseForm(
            @RequestParam(name = "recordId") long recordId,
            @RequestParam(name = "jobId") long jobId,
            Model model) {
        logger.info("GET /add-expense");

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        String todayDateString = dateFormat.format(new Date());

        model.addAttribute("recordId", recordId);
        model.addAttribute("jobId", jobId);
        model.addAttribute("expenseCost", "");
        model.addAttribute("description", "");
        model.addAttribute("descLen", "0/512");
        model.addAttribute("category", "");
        model.addAttribute("expenseDate", todayDateString);
        return "expenseFormTemplate";
    }

    /**
     * Gets the details entered into the add expense form, creates a new expense object using them and adds it to
     * storage
     * @param recordId ID of the renovation record
     * @param jobId ID of the job
     * @param expenseCost cost of the expense
     * @param description description of the expense
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param principal the currently authenticated user
     * @return redirect to renovation record details page if no errors, otherwise the add expense form
     */
    @PostMapping("/my-renovations/add-expense")
    public String postCreateExpenseForm(
            @RequestParam(name = "recordId") long recordId,
            @RequestParam(name = "jobId") long jobId,
            @RequestParam(name = "expenseCost") String expenseCost,
            @RequestParam(name = "description") String description,
            @RequestParam(name = "category") String category,
            @RequestParam(name = "expenseDate") String expenseDate,
            Model model,
            Principal principal) {
        logger.info("POST /add-expense");

        try {
            RenovationRecord record = renovationRecordService.getAndAuthenticateRecord(recordId, principal.getName());
            Job job = jobService.getAndAuthenticateJob(jobId, record);

            Expense expense = new Expense(expenseCost, description, category, expenseDate);
            expense.setJob(job);

            expenseService.validateExpense(expense);

            expenseService.addExpense(expense);
            return "redirect:/my-renovations/job-details?jobId=" + jobId;
        } catch (IllegalArgumentException e) {
            model.addAttribute("recordId", recordId);
            model.addAttribute("jobId", jobId);
            model.addAttribute("expenseCost", expenseCost);
            model.addAttribute("description", description);
            model.addAttribute("descLen", description.codePointCount(0, description.length()) + "/512");
            model.addAttribute("category", category);
            model.addAttribute("expenseDate", expenseDate);

            List<String> errorMessages= expenseService.getErrorMessages(e.getMessage());
            model.addAttribute("expenseCostErrorMessage", errorMessages.get(0));
            model.addAttribute("descriptionErrorMessage", errorMessages.get(1));
            model.addAttribute("expenseDateErrorMessage", errorMessages.get(2));

            return "expenseFormTemplate";
        }
    }
}
