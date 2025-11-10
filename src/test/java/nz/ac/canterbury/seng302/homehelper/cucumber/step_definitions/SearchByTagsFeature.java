package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.controller.renovations.SearchPageController;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import nz.ac.canterbury.seng302.homehelper.service.*;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class SearchByTagsFeature {
    private static MockMvc mockMvc;
    private static Principal principal;
    private static TagRepository tagRepository;
    private static ValidationService validationService;
    private static ModerationService moderationService;
    private static RenovationRecordService renovationRecordService;
    private static TagService tagService;
    private static UserService userService;
    private static SearchPageController searchPageController;
    private static String searchString;
    private static String searchTags;

    @BeforeAll
    public static void beforeAll() {
        principal = Mockito.mock(Principal.class);
        validationService = new ValidationService();
        moderationService = new ModerationService();
        renovationRecordService = Mockito.mock(RenovationRecordService.class);
        userService = Mockito.mock(UserService.class);
        tagRepository = Mockito.mock(TagRepository.class);
        tagService = new TagService(tagRepository, validationService, moderationService);
        searchPageController = new SearchPageController(renovationRecordService, userService, tagService);
        mockMvc = MockMvcBuilders.standaloneSetup(searchPageController).build();
    }

    // AC1
    @Given("I enter a search string in the search renovation bar")
    public void i_enter_a_search_string_in_the_search_renovation_bar() throws Exception {
        searchString = "Tag";
        mockMvc.perform(get("/search")
                        .param("searchString", searchString)
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("searchResultsTemplate"))
                .andExpect(model().attribute("searchString", searchString));
    }

    // AC2
    @Given("I see a list of matching tags for my search query")
    public void i_see_a_list_of_matching_tags_for_my_search_query() {
        List<String> matchingTags = new ArrayList<>();
        for (Tag tag : tagService.getAllTags()) {
            if (tag.getName().startsWith(searchString)) {
                matchingTags.add(tag.getName());
            }
        }
        Assertions.assertEquals(matchingTags, tagService.getAllTags().stream().map(Tag::getName).toList());
    }

    // AC3
    @Given("I see the tags {string}, {string}, and {string} in the search bar")
    public void i_see_the_tags_and_in_the_search_bar(String tag1, String tag2, String tag3) throws Exception {
        searchTags = tag1+";"+tag2+";"+tag3;
        mockMvc.perform(get("/search")
                        .param("searchString", searchString)
                        .param("searchTags", searchTags)
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("searchResultsTemplate"))
                .andExpect(model().attribute("searchString", searchString))
                .andExpect(model().attribute("searchTags", searchTags));
    }

    // AC1
    @When("The search string partially matches a tag known by the system")
    public void the_search_string_partially_matches_a_tag_known_by_the_system() {
        Mockito.when(tagRepository.findAll()).thenReturn(List.of(new Tag("Tag 1"), new Tag("Tag 2"), new Tag("Tag 3")));
        Assertions.assertTrue(tagService.getAllTags().get(0).getName().startsWith(searchString));
    }

    // AC2
    @When("I select a tag from the list")
    public void i_select_a_tag_from_the_list() throws Exception {
        mockMvc.perform(post("/search-add-tag")
                        .param("searchString", "")
                        .param("searchTags", "")
                        .param("newTag", "Tag 1")
                        .principal(principal))
                .andExpect(status().is3xxRedirection());
    }

    // AC3
    @When("I click the X next to {string}")
    public void i_click_the_x_next_to(String tag2) throws Exception {
        searchString = "Tag";
        mockMvc.perform(post("/search-remove-tag")
                        .param("searchString", searchString)
                        .param("searchTags", searchTags)
                        .param("tag", tag2)
                        .principal(principal))
                .andExpect(status().is3xxRedirection());
    }

    // AC1
    @Then("I can see the list of matching tags")
    public void i_can_see_the_list_of_matching_tags() {
        List<String> matchingTags = new ArrayList<>();
        for (Tag tag : tagService.getAllTags()) {
            if (tag.getName().startsWith(searchString)) {
                matchingTags.add(tag.getName());
            }
        }
        Assertions.assertEquals(matchingTags, tagService.getAllTags().stream().map(Tag::getName).toList());
    }

    // AC2
    @Then("The tag is added to the content of the search bar")
    public void the_tag_is_added_to_the_content_of_the_search_bar() throws Exception {
        mockMvc.perform(post("/search-add-tag")
                        .param("searchString", "")
                        .param("searchTags", "")
                        .param("newTag", "Tag 1")
                        .principal(principal))
                .andExpect(view().name("redirect:./search?searchString=&searchTags=Tag 1&results-page=1&clearSearch=true"));
    }

    // AC3
    @Then("{string} is removed from the search content and {string} and {string} are left")
    public void is_removed_from_the_search_content(String tag2, String tag1, String tag3) throws Exception {
        mockMvc.perform(post("/search-remove-tag")
                        .param("searchString", searchString)
                        .param("searchTags", searchTags)
                        .param("tag", tag2)
                        .principal(principal))
                .andExpect(view().name("redirect:./search?searchString=" + searchString + "&searchTags=" +
                        tag1+";"+tag3 + "&results-page=1&clearSearch=false"));
    }

    // AC5
    @Given("I have added five tags to the search bar")
    public void i_have_added_five_tags_to_the_search_bar() throws Exception {
        searchTags = "Tag 1;Tag 2;Tag 3;Tag 4;Tag 5";
        mockMvc.perform(get("/search")
                        .param("searchString", "")
                        .param("searchTags", searchTags)
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("searchResultsTemplate"))
                .andExpect(model().attribute("searchString", ""))
                .andExpect(model().attribute("searchTags", searchTags));
    }

    // AC5
    @When("I try to add a new tag to the search bar")
    public void i_try_to_add_a_new_tag_to_the_search_bar() throws Exception {
        mockMvc.perform(post("/search-add-tag")
                        .param("searchString", "")
                        .param("searchTags", searchTags)
                        .param("newTag", "Tag 6")
                        .principal(principal))
                .andExpect(status().is3xxRedirection());
    }

    // AC5
    @Then("I get an error message saying you cannot add more than five tags")
    public void i_get_an_error_message_saying_you_cannot_add_more_than_five_tags() throws Exception {
        mockMvc.perform(post("/search-add-tag")
                        .param("searchString", "")
                        .param("searchTags", searchTags)
                        .param("newTag", "Tag 6")
                        .principal(principal))
                .andExpect(flash().attribute("tagErrorMessage", TagService.FIVE_TAGS_ALREADY_EXIST));
    }
}
