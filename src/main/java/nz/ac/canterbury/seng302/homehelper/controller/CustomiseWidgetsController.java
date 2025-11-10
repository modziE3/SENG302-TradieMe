package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.entity.User;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the customise widgets page
 */
@Controller
public class CustomiseWidgetsController {
    Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserService userService;
    private final RenovationRecordService renovationRecordService;

    public static final List<String> ALL_WIDGETS = List.of(
            "Job Recommendations", "Job Calendar", "Recently Viewed Jobs", "Recently Viewed Renovations", "Hottest Tradies Leaderboard"
    );

    @Autowired
    public CustomiseWidgetsController(UserService userService, RenovationRecordService renovationRecordService) {
        this.userService = userService;
        this.renovationRecordService = renovationRecordService;
    }

    /**
     * Gets the customise widgets page and
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param principal the currently authenticated user.
     * @return thymeleaf template for the customise widgets page
     */
    @GetMapping("/customise-widgets")
    public String customiseWidgets(
            Model model,
            Principal principal) {
        logger.info("GET /customise-widgets");
        User user = userService.getUser(principal.getName());
        model.addAttribute("loggedIn", principal != null);
        model.addAttribute("user", user);
        model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
        model.addAttribute("allWidgets", ALL_WIDGETS);
        return "customiseWidgetsTemplate";
    }

    /**
     * Posts string of the order of the home page widgets that was submitted in the form
     * @param widgetOrder String of the order the home page widgets will appear in on the home page
     * @return redirect to the home page
     */
    @PostMapping("/customise-widgets")
    public String postWidgetOrder(
            @RequestParam(name = "widgetOrder") String widgetOrder,
            Principal principal) {
        logger.info("POST /customise-widgets");
        User user = userService.getUser(principal.getName());
        List<String> newWidgets = new ArrayList<>();
        if (!widgetOrder.isEmpty()) {
            newWidgets.addAll(List.of(widgetOrder.split(",")));
        }
        user.setHomePageWidgetOrder(newWidgets);
        userService.addUser(user);
        return "redirect:/home";
    }
}
