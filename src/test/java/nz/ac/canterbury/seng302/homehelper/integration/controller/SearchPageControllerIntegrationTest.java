package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.controller.renovations.SearchPageController;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.TagService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import java.security.Principal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class SearchPageControllerIntegrationTest {
    @SpyBean
    private RenovationRecordService renovationRecordService;
    @MockBean
    private RenovationRecordRepository renovationRecordRepository;
    @SpyBean
    private UserService userService;
    @MockBean
    private UserRepository userRepository;
    @SpyBean
    private TagService tagService;
    @MockBean
    private TagRepository tagRepository;
    @Autowired
    private SearchPageController searchPageController;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private MockMvc mockMvc;
    private Principal authentication;

    @PostConstruct
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(searchPageController).build();
        User testUser = new User("Jane", "Doe", "jane@example.com", passwordEncoder.encode("P4$$word"),
                null, userService.generateValidationCode());
        testUser.grantAuthority("ROLE_USER");
        authentication = new UsernamePasswordAuthenticationToken(testUser.getEmail(), null, testUser.getAuthorities());
    }

    @Test
    public void getSearchPageNoSearch() throws Exception {
        mockMvc.perform(get("/search")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("searchResultsTemplate"))
                .andExpect(model().attribute("searchString", ""));
    }

    @Test
    public void getSearchPageStringSearch() throws Exception {
        mockMvc.perform(get("/search")
                    .param("searchString", "test")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("searchResultsTemplate"))
                .andExpect(model().attribute("searchString", "test"));
    }


    @Test
    public void getSearchPageEmojiSearch() throws Exception {
        mockMvc.perform(get("/search")
                .param("searchString", "🤣").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(view().name("searchResultsTemplate"))
                .andExpect(model().attribute("searchString", "🤣"));
    }


    @Test
    public void postSearch() throws Exception {
        mockMvc.perform(post("/search")
                        .param("searchString", "test")
                        .param("searchTags", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:./search?searchString=test&searchTags="));
    }

    @Test
    public void postSearch_NullValueEntered_SearchStringEmpty() throws Exception {
        mockMvc.perform(post("/search")
                        .param("searchString", (String) null)
                        .param("searchTags", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:./search?searchString=&searchTags="));
    }

    @Test
    public void userInputtedPage_ValidInput() throws Exception {
        Mockito.when(renovationRecordService.getPageList(any(), any(List.class))).thenReturn(List.of(1, 2, 3, 4));
        mockMvc.perform(post("/search-page-number")
                        .param("pageNumber", "3")
                        .param("searchString", "Test")
                        .param("searchTags", "Tag1;Tag2")
                        .principal(authentication))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:./search?searchString=Test&searchTags=Tag1;Tag2&results-page=3"));
    }

    @Test
    public void userInputtedPage_InvalidInput() throws Exception {
        Mockito.when(renovationRecordService.getPageList(any(), any(List.class))).thenReturn(List.of(1, 2, 3, 4));
        mockMvc.perform(post("/search-page-number")
                        .param("pageNumber", "6")
                        .param("searchString", "Test")
                        .param("searchTags", "Tag1;Tag2")
                        .principal(authentication))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:./search?searchString=Test&searchTags=Tag1;Tag2&results-page=1"));

    }

    @Test
    public void postSearchTag_NewTagNameProvided_TagNameAddedToSearchTags() throws Exception {
        mockMvc.perform(post("/search-add-tag")
                        .param("searchString", "test")
                        .param("searchTags", "")
                        .param("results-page", "")
                        .param("newTag", "Tag 1")
                        .param("clearSearch", "")
                        .principal(authentication))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:./search?searchString=test&searchTags=Tag 1&results-page=1&clearSearch=true"));
    }

    @Test
    public void postSearchTag_NewDifferentTagNameProvided_TagNameAddedToSearchTags() throws Exception {
        mockMvc.perform(post("/search-add-tag")
                        .param("searchString", "test")
                        .param("searchTags", "Tag 1")
                        .param("results-page", "")
                        .param("newTag", "Tag 2")
                        .param("clearSearch", "")
                        .principal(authentication))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:./search?searchString=test&searchTags=Tag 1;Tag 2&results-page=1&clearSearch=true"));
    }

    @Test
    public void postSearchTag_DuplicateTagNameAndNoSearchStringProvided_TagErrorMessageAppears() throws Exception {
        mockMvc.perform(post("/search-add-tag")
                        .param("searchString", "")
                        .param("searchTags", "Tag 1")
                        .param("results-page", "")
                        .param("newTag", "Tag 1")
                        .param("clearSearch", "")
                        .principal(authentication))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("tagErrorMessage", TagService.TAG_ALREADY_EXISTS));
    }

    @Test
    public void postSearchTag_DuplicateTagNameAndSearchStringProvided_TagErrorMessageAppears() throws Exception {
        mockMvc.perform(post("/search-add-tag")
                        .param("searchString", "test")
                        .param("searchTags", "Tag 1")
                        .param("results-page", "")
                        .param("newTag", "Tag 1")
                        .param("clearSearch", "")
                        .principal(authentication))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("tagErrorMessage", TagService.TAG_ALREADY_EXISTS));
    }

    @Test
    public void postRemoveSearchTag_TagNameProvided_TagNameRemovedFromSearchTags() throws Exception {
        mockMvc.perform(post("/search-remove-tag")
                        .param("searchString", "test")
                        .param("searchTags", "Tag 1;Tag 2;Tag 3")
                        .param("results-page", "")
                        .param("tag", "Tag 2")
                        .param("clearSearch", "")
                        .principal(authentication))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:./search?searchString=test&searchTags=Tag 1;Tag 3&results-page=1&clearSearch=false"));

    }

    @Test
    public void postAddSearchTag_AlreadyFiveTags_ErrorMessageAppears() throws Exception {
        mockMvc.perform(post("/search-add-tag")
                .param("searchString", "test")
                .param("searchTags", "Tag 1;Tag 2;Tag 3; Tag 4; Tag 5")
                .param("results-page", "")
                .param("newTag", "Tag 6")
                        .param("clearSearch", "")
                .principal(authentication))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("tagErrorMessage", TagService.FIVE_TAGS_ALREADY_EXIST));
    }

}
