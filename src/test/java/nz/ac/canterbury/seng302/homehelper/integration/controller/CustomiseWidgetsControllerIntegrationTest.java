package nz.ac.canterbury.seng302.homehelper.integration.controller;

import nz.ac.canterbury.seng302.homehelper.controller.CustomiseWidgetsController;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class CustomiseWidgetsControllerIntegrationTest {
    @Autowired
    private CustomiseWidgetsController customiseWidgetsController;
    @Autowired
    private UserService userService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private Principal principal;

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(customiseWidgetsController).build();
        testUser = new User("J", "D", "jane@doe.nz", "P4$$word", null, null);
        when(principal.getName()).thenReturn("jane@doe.nz");
        when(userRepository.findByEmailContainingIgnoreCase("jane@doe.nz")).thenReturn(testUser);
    }

    @Test
    public void customiseWidgets_customiseWidgetsPageReturned() throws Exception {
        mockMvc.perform(get("/customise-widgets")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("customiseWidgetsTemplate"))
                .andExpect(model().attribute("user", testUser))
                .andExpect(model().attribute("allWidgets", CustomiseWidgetsController.ALL_WIDGETS));
    }

    @Test
    public void postWidgetOrder_NoWidgetsSelected_UserHomePageWidgetOrderListEmpty() throws Exception {
        String widgetOrder = "";
        mockMvc.perform(post("/customise-widgets")
                        .param("widgetOrder", widgetOrder)
                        .principal(principal))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User user = userCaptor.getValue();
        assertEquals(0, user.getHomePageWidgetOrder().size());
    }

    static Stream<List<String>> widgetOrderings() {
        return Stream.of(
                List.of("Job Recommendations"),
                List.of("Job Recommendations", "Job Calendar"),
                List.of("Job Recommendations", "Job Calendar", "Recent Jobs"),
                List.of("Job Recommendations", "Job Calendar", "Recent Jobs", "Recently Viewed Renovations")
        );
    }
    @ParameterizedTest
    @MethodSource("widgetOrderings")
    public void postWidgetOrder_WidgetsSelected_UserHomePageWidgetOrderListChanged(List<String> widgetOrdering) throws Exception {
        String widgetOrder = String.join(",", widgetOrdering);
        mockMvc.perform(post("/customise-widgets")
                        .param("widgetOrder", widgetOrder)
                        .principal(principal))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User user = userCaptor.getValue();
        assertEquals(widgetOrdering, user.getHomePageWidgetOrder());
    }
}
