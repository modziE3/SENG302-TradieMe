package nz.ac.canterbury.seng302.homehelper.controller.renovations;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.security.Principal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Controller for the search / browse renovations page
 */
@Controller
public class SearchPageController {
    Logger logger = LoggerFactory.getLogger(SearchPageController.class);
    private final RenovationRecordService renovationRecordService;
    private final UserService userService;
    private final TagService tagService;

    @Autowired
    public SearchPageController(RenovationRecordService renovationRecordService, UserService userService,
                                TagService tagService) {
        this.renovationRecordService = renovationRecordService;
        this.userService = userService;
        this.tagService = tagService;
    }

    /**
     * Handles the get request for the search page.
     * @param searchString the user search.
     * @param searchTags the string of search tags in the search bar
     * @param resultsPage the current page number of the search results page
     * @param model the data for the template to display
     * @param principal the currently authenticated user
     * @return the search results template.
     */
    @GetMapping("/search")
    public String getSearchPage(@RequestParam(name = "searchString", required = false) String searchString,
                                @RequestParam(name = "searchTags", required = false, defaultValue = "") String searchTags,
                                @RequestParam(name = "results-page", defaultValue = "1") Integer resultsPage,
                                @RequestParam(name = "clearSearch", required = false, defaultValue = "false") boolean clearSearch,
                                Model model,
                                Principal principal) {
        logger.info("GET /search?searchString={}&searchTags={}", searchString, searchTags);
        if (clearSearch || searchString == null) {
            searchString = "";
        }



        List<RenovationRecord> results;
        List<String> tagList = (searchTags == null || searchTags.isEmpty()) ? new ArrayList<>() : Arrays.asList(searchTags.split(";"));

        results = renovationRecordService.getMatchingRenovationRecords(searchString, principal.getName(), tagList)
                .stream()
                .sorted(Comparator.comparing(RenovationRecord::getCreatedTimestamp).reversed())
                .toList();

        List<Integer> pages = renovationRecordService.getPageList(resultsPage, results);
        List<RenovationRecord> paginatedResults = renovationRecordService.getRenovationRecordPages(resultsPage, results);

        model.addAttribute("searchString", searchString);
        model.addAttribute("searchTags", searchTags);
        model.addAttribute("tagNames", "["+String.join("`", tagService.getAllTags()
                .stream().map(Tag::getName).toList())+"]");
        model.addAttribute("results", renovationRecordService.getRenovationRecordCards(userService, paginatedResults));
        model.addAttribute("pages", pages);
        model.addAttribute("searchPage", resultsPage);
        model.addAttribute("lastPage", renovationRecordService.getNumPages(results));
        model.addAttribute("onSearchPage", true);

        model.addAttribute("loggedIn", principal != null);
        if (principal != null) {
            User user = userService.getUser(principal.getName());
            if (user != null && user.getVerificationCode() != null) {
                return "redirect:/registration-code";
            }
            model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
            model.addAttribute("user", user);
            if (user != null && user.getProfilePicture() != null) {
                model.addAttribute("profileImage", "/profileImages/" + user.getProfilePicture());
            }
        }
        return "searchResultsTemplate";
    }

    /**
     * Allows for the searching of renovations using the user inputs as the search queries.
     * @param searchString the user defined string to search by.
     * @param searchTags the user defined tags to search by
     * @return redirect to the search results with user search queries as the parameters.
     */
    @PostMapping("/search")
    public String postSearchPage(@RequestParam(name = "searchString", required = false) String searchString,
                                 @RequestParam(name = "searchTags") String searchTags) {
        logger.info("POST /search?searchString={}&searchTags={}", searchString, searchTags);

        if (searchString == null || searchString.isEmpty()) {
            searchString = "";
        }

        String searchEncode = URLEncoder.encode(searchString, StandardCharsets.UTF_8);
        String tagsEncode = URLEncoder.encode(searchTags, StandardCharsets.UTF_8);
        return "redirect:./search?searchString=" + searchEncode + "&searchTags=" + tagsEncode;
    }

    /**
     * Swaps the page around based on user input from a button or a text box
     * @param searchString the user defined string to search by
     * @param searchTags the user defined tags to search by
     * @param newPageNumber the page number of the page the user wants to go to
     * @param principal the currently authenticated user.
     * @return redirect to the entered page number of the search page
     */
    @PostMapping("/search-page-number")
    public String postSearchPageNumber(@RequestParam(name = "searchString", required = false) String searchString,
                                       @RequestParam(name = "searchTags") String searchTags,
                                       @RequestParam(name = "results-page", defaultValue = "1") Integer resultsPage,
                                       @RequestParam(name = "pageNumber") Integer newPageNumber,
                                       Principal principal) {
        logger.info("POST /search-page-number?pageNumber={}", newPageNumber);

        List<String> tagList = (searchTags == null || searchTags.isEmpty()) ? new ArrayList<>() : Arrays.asList(searchTags.split(";"));
        List<RenovationRecord> results = renovationRecordService.getMatchingRenovationRecords(searchString, principal.getName(), tagList)
                .stream()
                .sorted(Comparator.comparing(RenovationRecord::getCreatedTimestamp).reversed())
                .toList();

        List<Integer> pages = renovationRecordService.getPageList(resultsPage, results);

        if (renovationRecordService.pageNumberIsInRecordPageList(pages, newPageNumber)) {
            resultsPage = newPageNumber;
        }

        String searchEncode = URLEncoder.encode(searchString, StandardCharsets.UTF_8);
        return "redirect:./search?searchString="
                +searchEncode
                +"&searchTags="+searchTags
                +"&results-page="+resultsPage;
    }

    /**
     * Posts the name of a tag being added to the search bar and adds it to the list of search tags
     * @param searchString the user defined string to search by
     * @param searchTags the string of search tags in the search bar
     * @param resultsPage the current page number of the search results page
     * @param newTag name of tag being added
     * @return redirect to the search page
     */
    @PostMapping("/search-add-tag")
    public String postSearchTag(@RequestParam(name = "searchString", required = false) String searchString,
                                @RequestParam(name = "searchTags") String searchTags,
                                @RequestParam(name = "results-page", defaultValue = "1") Integer resultsPage,
                                @RequestParam(name = "newTag") String newTag,
                                RedirectAttributes redirectAttributes) {
        logger.info("POST /search-add-tag?newTag={}", newTag);

        if (searchString == null || searchString.isEmpty()) {
            searchString = "";
        }

        String tagError = tagService.validateSearchTag(searchTags, newTag);
        if (!tagError.isEmpty()) {
            redirectAttributes.addFlashAttribute("tagErrorMessage", tagError);
        } else {
            searchTags = tagService.addNewSearchTag(searchTags, newTag);
        }

        logger.info("Redirecting with searchString='{}'", searchString);
        String searchEncode = URLEncoder.encode(searchString, StandardCharsets.UTF_8);
        return "redirect:./search?searchString="
                +searchEncode
                +"&searchTags="+searchTags
                +"&results-page="+resultsPage
                +"&clearSearch=true";
    }

    /**
     * Posts the name of a tag wanting to be deleted from the search bar and removes it from the list of search tags
     * @param searchString the user defined string to search by
     * @param searchTags the string of search tags in the search bar
     * @param resultsPage the current page number of the search results page
     * @param tagName name of tag being removed
     * @return redirect to the search page
     */
    @PostMapping("/search-remove-tag")
    public String postRemoveSearchTag(@RequestParam(name = "searchString", required = false) String searchString,
                                      @RequestParam(name = "searchTags") String searchTags,
                                      @RequestParam(name = "results-page", defaultValue = "1") Integer resultsPage,
                                      @RequestParam(name = "tag") String tagName) {
        logger.info("POST /search-remove-tag?tag={}", tagName);

        List<String> searchTagsSplit = new ArrayList<>(Arrays.asList(searchTags.split(";")));
        searchTagsSplit.remove(tagName);
        searchTags = String.join(";", searchTagsSplit);

        String searchEncode = URLEncoder.encode(searchString, StandardCharsets.UTF_8);
        return "redirect:./search?searchString="
                +searchEncode
                +"&searchTags="+searchTags
                +"&results-page="+resultsPage
                +"&clearSearch=false";
    }
}
