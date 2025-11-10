package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.controller.renovations.SearchPageController;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.repository.*;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class SearchingRenovationRecordsFeature {

    private static MockMvc mockMvc;
    private static RenovationRecordService renovationRecordService;
    private static Principal principal;
    private static RecentRenovationRepository recentRenovationRepository;
    private static RenovationRecordRepository renovationRecordRepository;
    private static QuoteRepository quoteRepository;

    public List<RenovationRecord> makeRenovationRecordList(int value) {
        List<RenovationRecord> renovationRecords = new ArrayList<>();
        int i = 0;
        while (i < value) {
            renovationRecords.add(new RenovationRecord(("example" + i), "text", null, null));
            i++;
        }
        return renovationRecords;
    }

    @BeforeAll
    public static void beforeAll() {
        recentRenovationRepository = Mockito.mock(RecentRenovationRepository.class);
        renovationRecordRepository = Mockito.mock(RenovationRecordRepository.class);
        renovationRecordService = Mockito.mock(RenovationRecordService.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        ValidationService validationService = new ValidationService();
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        LocationService locationService = new LocationService(validationService);
        UserService userService = new UserService(userRepository, validationService, taskScheduler, locationService, renovationRecordService, renovationRecordRepository, recentRenovationRepository, quoteRepository);
        TagService tagService = Mockito.mock(TagService.class);
        SearchPageController searchPageController = new SearchPageController(renovationRecordService, userService, tagService);

        principal = Mockito.mock(Principal.class);
        mockMvc = MockMvcBuilders.standaloneSetup(searchPageController).build();
    }

    @Given("I am on the search renovations page")
    public void i_am_on_the_search_renovations_page() throws Exception {
        Mockito.when(renovationRecordService.getRenovationRecords()).thenReturn(makeRenovationRecordList(14));
        Mockito.when(renovationRecordService.getPageList(any(), any(List.class))).thenReturn(List.of(1, 2, 3));

        mockMvc.perform(get("/search")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("searchString", ""));
    }

    @When("I enter a search string {string} and search for it")
    public void i_enter_a_search_string_and_search_for_it(String searchString) throws Exception {
        Mockito.when(renovationRecordService.getPageList(any(), any(List.class))).thenReturn(List.of(1, 2, 3));

        mockMvc.perform(post("/search")
                        .param("searchString", searchString)
                        .param("searchTags", "")
                        .param("results-page", "1")
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:./search?searchString="+searchString+"&searchTags="));
    }

    @Then("I am only shown my renovation records whose name or description include my search value")
    public void i_am_only_shown_my_renovation_records_whose_name_or_description_include_my_search_value() throws Exception {
        List<RenovationRecord> renovationRecords = makeRenovationRecordList(28);
        Mockito.when(renovationRecordService.getRenovationRecordPages(any(), any(List.class))).thenReturn(renovationRecords.subList(4,5));

        mockMvc.perform(get("/search")
                .param("searchString", "3")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("results"));
    }

    @Then("I see pagination buttons and the results are split into pages")
    public void i_see_pagination_buttons_and_the_results_are_split_into_pages() throws Exception {
        List<RenovationRecord> renovationRecords = makeRenovationRecordList(14);
        List<Integer> paginationNumbers = Arrays.asList(1, 2, 3);
        Mockito.when(renovationRecordService.getRenovationRecordPages(any(), any(List.class))).thenReturn(renovationRecords);

        mockMvc.perform(get("/search")
                .param("searchString", "e")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pages", paginationNumbers));
    }

    @Then("I see pagination buttons set up for more than ten pages")
    public void i_see_pagination_buttons_set_up_for_more_than_pages() throws Exception {
        List<RenovationRecord> renovationRecords = makeRenovationRecordList(70);
        List<Integer> paginationNumbers = Arrays.asList(1, 2, 3, 4, 5);
        Mockito.when(renovationRecordService.getMatchingRenovationRecords("e", principal.getName(), Collections.emptyList())).thenReturn(renovationRecords);
        Mockito.when(renovationRecordService.getPageList(any(), any(List.class))).thenReturn(paginationNumbers);

        mockMvc.perform(get("/search")
                .param("searchString", "e")
                .param("results-page", "3")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pages", paginationNumbers));
    }
}
