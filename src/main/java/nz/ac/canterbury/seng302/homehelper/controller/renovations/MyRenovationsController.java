package nz.ac.canterbury.seng302.homehelper.controller.renovations;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.PaginationUtil;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

/**
 * Controller for My Renovations page
 * This class links automatically with @link{RenovationRecordService}
 */
@Controller
public class MyRenovationsController {
    Logger logger = LoggerFactory.getLogger(MyRenovationsController.class);

    private final RenovationRecordService renovationRecordService;
    private final UserService userService;

    @Autowired
    public MyRenovationsController(RenovationRecordService renovationRecordService, UserService userService) {
        this.renovationRecordService = renovationRecordService;
        this.userService = userService;
    }

    /**
     * Gets all renovation records in database storage to be displayed
     * @param renovationsPage Current pagination page the user is on
     * @param principal the currently authenticated user.
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @return thymeleaf myRenovationsTemplate
     */
    @GetMapping("/my-renovations")
    public String showRenovationRecords(
            @RequestParam(name = "renovationsPage", defaultValue = "1") Integer renovationsPage,
            Principal principal,
            Model model) {
        logger.info("GET /my-renovations");

        List<RenovationRecord> allRenovationRecords = renovationRecordService.getRenovationRecordsByOwner(principal.getName());
        int renovationsPerPage = 18;
        int lastPageNumber = PaginationUtil.getLastPageNumber(allRenovationRecords.size(), renovationsPerPage);
        if (renovationsPage == null || renovationsPage < 1) {
            renovationsPage = 1;
        } else if (renovationsPage > lastPageNumber) {
            renovationsPage = lastPageNumber;
        }

        model.addAttribute("renovationsResults",
                PaginationUtil.getPageNumbers(allRenovationRecords.size(), renovationsPerPage));
        model.addAttribute("renovationsPage", renovationsPage);
        model.addAttribute("lastPageRenovations",lastPageNumber);
        model.addAttribute("paginatedRenovationRecords",
                PaginationUtil.getPage(allRenovationRecords, renovationsPage, renovationsPerPage));

        model.addAttribute("renovationRecords", allRenovationRecords);

        String email = principal.getName();
        User user = userService.getUser(email);
        model.addAttribute("user", user);
        if (user.getProfilePicture() != null) {
            model.addAttribute("profileImage", "/profileImages/" + user.getProfilePicture());
        }
        model.addAttribute("loggedIn", principal != null);
        return "myRenovationsTemplate";
    }

    /**
     * Posts the page number entered into the pagination input bar for My Renovations page and redirects to the pagination page
     * @param renovationsPage Current pagination page the My Renovations page is on
     * @param renovationsPageNumber Number entered into the My Renovations pagination input bar
     * @return redirect to the correct pagination page for My Renovations
     */
    @PostMapping("/my-renovations")
    public String postRenovationsPageNumber(
            @RequestParam(name = "renovationsPage", defaultValue = "1") Integer renovationsPage,
            @RequestParam(name = "renovationsPageNumber", required = false) Integer renovationsPageNumber) {
        logger.info("POST /my-renovations");
        if (renovationsPageNumber != null) {
            return "redirect:/my-renovations?renovationsPage=" + renovationsPageNumber;
        } else {
            return "redirect:/my-renovations?renovationsPage=" + renovationsPage;
        }
    }

    /**
     * Posts the record ID of a record chosen for deletion and deletes the record
     * @param recordId ID of record wanting to be deleted
     * @param renovationsPage Current pagination page the user is on
     * @return redirect to the pagination page the user is currently on
     */
    @PostMapping("/delete-renovation")
    public String deleteRenovationRecord(
            @RequestParam(name = "recordId") Long recordId,
            @RequestParam(name = "renovationsPage", defaultValue = "1") Integer renovationsPage) {
        logger.info("POST /delete-renovation");
        renovationRecordService.deleteRenovationRecord(recordId);
        return "redirect:./my-renovations?renovationsPage=" + renovationsPage;
    }
}
