package nz.ac.canterbury.seng302.homehelper.unit.controller;

import nz.ac.canterbury.seng302.homehelper.controller.renovations.RenovationDetailsController;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class RenovationDetailsControllerTest {
    @Mock
    private RenovationRecordService renovationRecordService;
    @Mock
    private JobService jobService;
    @Mock
    private ExpenseService expenseService;
    @Mock
    private UserService userService;
    @Mock
    private TagService tagService;
    @InjectMocks
    private RenovationDetailsController renovationDetailsController;

    private Principal principal;
    private Model model;
    private RenovationRecord testRecord;
    private User testUser;

    @BeforeEach
    public void setUp() {
        principal = Mockito.mock(Principal.class);
        model = Mockito.mock(Model.class);

        testUser = new User("John", "Doe", "john@example.com", "P4$$word",
                null, null);
        testRecord = new RenovationRecord("Test", "Test",
                List.of(new Room("Room1"), new Room("Room2")), "john@example.com");
        testRecord.setId(1L);
        Job job = new Job("Test", "Test", "01/01/2025", "01/01/2024");
        job.setId(1L);
        testRecord.setJobs(List.of(job));
    }

    @Test
    public void getRenovationDetails_RenovationRecordExists_RenovationDetailsTemplateShown() {
        Mockito.when(renovationRecordService.getRecordById(testRecord.getId())).thenReturn(testRecord);
        Mockito.when(renovationRecordService.getRenovationRecordsByOwner(testUser.getEmail())).thenReturn(List.of(testRecord));
        Mockito.when(principal.getName()).thenReturn(testUser.getEmail());

        // Creating an empty Slice object was provided by ChatGPT
        Slice<Job> emptySlice = new SliceImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 10),
                false
        );
        Mockito.when(jobService.getJobPages(1, testRecord, "false")).thenReturn(emptySlice);

        Mockito.when(jobService.getPageList(1, testRecord, "false")).thenReturn(List.of(1));
        Mockito.when(userService.getUser(testUser.getEmail())).thenReturn(testUser);

        String viewName = renovationDetailsController.getRenovationDetails(testRecord.getId(),
                1, 1,"false-0--", "false", model, principal);

        Assertions.assertEquals("renovationDetailsTemplate", viewName);
        Mockito.verify(model).addAttribute("recordId", testRecord.getId());
        Mockito.verify(model).addAttribute("renoDescription", testRecord.getDescription());
        Mockito.verify(model).addAttribute("rooms", testRecord.getRooms());
        Mockito.verify(model).addAttribute("jobPages", List.of(1));
        Mockito.verify(model).addAttribute("jobPage", 1);
    }

    @Test
    public void getRenovationDetails_RenovationRecordDoesNotExist_ErrorTemplateShown() {
        Mockito.when(renovationRecordService.getRecordById(2L)).thenReturn(null);

        String viewName = renovationDetailsController.getRenovationDetails(2L,
                1, 1, "", "false", model, principal);

        Assertions.assertEquals("error", viewName);
    }

    @Test
    public void postJobPageNumber_RenovationRecordExistsAndValidPageNumber_RedirectedToNewPage() {
        Mockito.when(renovationRecordService.getRecordById(testRecord.getId())).thenReturn(testRecord);

        // Creating an empty Slice object was provided by ChatGPT
        Slice<Job> emptySlice = new SliceImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 10),
                false
        );
        Mockito.when(jobService.getPageList(1, testRecord, "false")).thenReturn(List.of(1));
        Mockito.when(expenseService.getPageList(Mockito.any(), Mockito.anyList())).thenReturn(List.of(1));

        String viewName = renovationDetailsController.postJobPageNumber(testRecord.getId(),
                1, 1, 1, 1, "false-0-", "false", model, principal);

        Assertions.assertEquals("redirect:/my-renovations/details?recordId="+testRecord.getId()
                +"&job-page=1"
                +"&expensesPage=1"
                +"&search=false-0-", viewName);
    }

    @Test
    public void postJobPageNumber_RenovationRecordExistsAndInvalidPageNumber_RenovationDetailsTemplateShown() {
        Mockito.when(renovationRecordService.getRecordById(testRecord.getId())).thenReturn(testRecord);

        // Creating an empty Slice object was provided by ChatGPT
        Slice<Job> emptySlice = new SliceImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 10),
                false
        );
        Mockito.when(jobService.getPageList(1, testRecord, "false")).thenReturn(List.of(1));

        String viewName = renovationDetailsController.postJobPageNumber(testRecord.getId(),
                1, 2, 1, 1, "false-0--", "false", model, principal);

        Assertions.assertEquals("redirect:/my-renovations/details?recordId="+testRecord.getId()
                +"&job-page=1"
                +"&expensesPage=1"
                +"&search=false-0--", viewName);
    }

    @Test
    public void postJobPageNumber_RenovationRecordDoesNotExist_ErrorTemplateShown() {
        Mockito.when(renovationRecordService.getRecordById(2L)).thenReturn(null);

        String viewName = renovationDetailsController.postJobPageNumber(2L,
                1, 1, 1, 1, "false-0-", "false", model, principal);

        Assertions.assertEquals("error", viewName);
    }
}
