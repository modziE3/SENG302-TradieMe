package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.controller.HomePageController;
import nz.ac.canterbury.seng302.homehelper.controller.renovations.SearchPageController;
import nz.ac.canterbury.seng302.homehelper.controller.renovations.RenovationDetailsController;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RecentRenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RoomRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class SearchPageFeature {
    @Autowired
    private SearchPageController searchPageController;
    @Autowired
    private HomePageController homePageController;
    @Autowired
    private RenovationDetailsController renovationDetailsController;
    @Autowired
    private UserService userService;
    @Autowired
    private RenovationRecordService renovationRecordService;
    @Autowired
    private TagService tagService;
    @Autowired
    private JobService jobService;
    @Autowired
    private QuoteService quoteService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private ExpenseService expenseService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private RenovationRecordRepository renovationRecordRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private RecentRenovationRepository recentRenovationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Principal principal;

    private MockMvc mockMvc;
    private RenovationRecord renovationRecord;
    private List<RenovationRecord> records;

    @Before("@SearchPage")
    public void beforeAll() {
        renovationRecordRepository.deleteAll();
        roomRepository.deleteAll();
        recentRenovationRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User("first", "last", "john@example.com", "P4$$word",
                null, null);
        UsernamePasswordAuthenticationToken authenticationToken = mock(UsernamePasswordAuthenticationToken.class);
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authenticationToken);
        when(authenticationToken.getPrincipal()).thenReturn(user);
        SecurityContextHolder.setContext(context);
        when(principal.getName()).thenReturn("john@example.com");
        userService.addUser(user);
    }

    // AC1, 2
    @Given("I have created a renovation record with public status set to {string}")
    public void i_have_created_a_renovation_record_with_public_status_set_to(String publicStatus) {
        renovationRecord = new RenovationRecord("Record Name", "Record Description", List.of(), "john@example.com");
        renovationRecord.setIsPublic(Objects.equals(publicStatus, "true"));
        renovationRecord.setId(1L);
        renovationRecordService.addRenovationRecord(renovationRecord);
    }

    @When("I toggle a switch labeled Public and Private to highlight {string}")
    public void i_toggle_a_switch_labeled_public_and_private_to_highlight(String publicStatus) throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(renovationDetailsController).build();

        mockMvc.perform(post("/my-renovations/details/update-public-status")
                        .param("recordId", renovationRecord.getId().toString())
                        .param("public", String.valueOf(Objects.equals(publicStatus, "Public")))
                .principal(principal));
    }

    @Then("the public status of the renovation record will be set to {string}")
    public void the_public_status_of_the_renovation_record_will_be_set_to(String publicStatus) {
        renovationRecord = renovationRecordService.getRecordById(renovationRecord.getId());
        Assertions.assertEquals(renovationRecord.getIsPublic(), Boolean.parseBoolean(publicStatus));
    }

    //AC3
    @Given("I am logged in")
    public void i_am_logged_in() throws Exception{
        mockMvc = MockMvcBuilders.standaloneSetup(homePageController).build();
        mockMvc.perform(MockMvcRequestBuilders.get("/home")
                        .principal(() -> "john@example.com"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attributeExists("loggedIn"))
                .andExpect(MockMvcResultMatchers.model().attribute("loggedIn", true))
                .andExpect(MockMvcResultMatchers.view().name("homePageTemplate"));
    }

    @When("I press the Browse Renovations button")
    public void i_press_the_button() throws Exception{
        mockMvc = MockMvcBuilders.standaloneSetup(searchPageController).build();
        mockMvc.perform(get("/search")
                        .principal(() -> "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("searchResultsTemplate"));
    }
    @Then("I see a list of public renovation records sorted by more recently created ones in descending order.")
    public void i_see_a_list_of_public_renovation_records_sorted_by_more_recently_created_ones_in_descending_order() {
        List<RenovationRecord> renovations = new ArrayList<>();
        RenovationRecord record1 = new RenovationRecord("name", "description", null, "john@example.com");
        RenovationRecord record2 = new RenovationRecord("name2", "description2", null, "john@example.com");
        RenovationRecord record3 = new RenovationRecord("name3", "description3", null, "john@example.com");

        record1.setCreatedTimestamp(LocalDateTime.of(2025, 5, 13, 10, 0));
        record2.setCreatedTimestamp(LocalDateTime.of(2025, 5, 12, 10, 0));
        record3.setCreatedTimestamp(LocalDateTime.of(2025, 5, 14, 10, 0));

        renovations.add(record1);
        renovations.add(record2);
        renovations.add(record3);

        renovations.sort(Comparator.comparing(RenovationRecord::getCreatedTimestamp, Comparator.reverseOrder()));

        List<LocalDateTime> createdDates = renovations.stream()
                .map(RenovationRecord::getCreatedTimestamp)
                .toList();


        List<LocalDateTime> expected = List.of(
                LocalDateTime.of(2025, 5, 14, 10, 0),
                LocalDateTime.of(2025, 5, 13, 10, 0),
                LocalDateTime.of(2025, 5, 12, 10, 0)
        );


        Assertions.assertEquals(expected, createdDates);
    }

    @Given("I see the list of public renovation records")
    public void i_see_the_list_of_public_renovation_records() throws Exception {
        records = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            RenovationRecord record = new RenovationRecord("Record "+i, "Record "+i, List.of(), "john@example.com");
            record.setIsPublic(true);
            records.add(renovationRecordService.addRenovationRecord(record));
        }

        mockMvc = MockMvcBuilders.standaloneSetup(searchPageController).build();
        mockMvc.perform(get("/search")
                        .param("results-page", "1")
                        .principal(() -> "john@example.com"))
                .andExpect(model().attributeExists("results"));
    }

    @Given("there are more than ten pages")
    public void there_are_more_than_ten_pages() throws Exception {
        Assertions.assertTrue(renovationRecordService.getNumPages(records) >= 10);

        mockMvc.perform(get("/search")
                        .principal(() -> "john@example.com"))
                .andExpect(model().attribute("pages", renovationRecordService.getPageList(1, records)))
                .andExpect(model().attribute("lastPage", renovationRecordService.getNumPages(records)));
    }

    @When("I input page number {int} that is within the range of available pages")
    public void i_input_page_number_that_is_within_the_range_of_available_pages(Integer pageNumber) {
        Assertions.assertTrue(0 < pageNumber && pageNumber <= renovationRecordService.getNumPages(records));
    }

    @When("I confirm that I want to go to page {int}")
    public void i_confirm_that_i_want_to_go_to_page(Integer pageNumber) throws Exception {
        mockMvc.perform(post("/search-page-number")
                .param("pageNumber", pageNumber.toString())
                .principal(() -> "john@example.com"));
    }

    @Then("I go to the list of renovation records corresponding to page {int}")
    public void i_go_to_the_list_of_renovation_records_corresponding_to_page(Integer pageNumber) throws Exception {
        mockMvc.perform(post("/search-page-number")
                        .param("searchString", "")
                        .param("searchTags", "")
                        .param("results-page", "1")
                        .param("pageNumber", pageNumber.toString())
                        .principal(() -> "john@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:./search?searchString=&searchTags=&results-page=" + pageNumber));
    }

}
