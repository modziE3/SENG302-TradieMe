package nz.ac.canterbury.seng302.homehelper.integration.controller;

import nz.ac.canterbury.seng302.homehelper.controller.renovations.RenovationDetailsController;
import nz.ac.canterbury.seng302.homehelper.entity.*;
import nz.ac.canterbury.seng302.homehelper.repository.*;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.ModerationService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.TagService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class RenovationDetailsControllerIntegrationTest {
    @Autowired
    private RenovationDetailsController renovationDetailsController;
    @MockBean
    private TagRepository tagRepository;
    @MockBean
    private RenovationRecordRepository renovationRecordRepository;
    @MockBean
    private JobRepository jobRepository;
    @MockBean
    private RecentRenovationRepository recentRenovationRepository;
    @MockBean
    private UserRepository userRepository;
    @Autowired
    private TagService tagService;
    @MockBean
    private ModerationService moderationService;
    @Autowired
    private RenovationRecordService renovationRecordService;
    @Autowired
    private JobService jobService;
    @MockBean
    private Principal principal;

    private MockMvc mockMvc;
    private RenovationRecord renovationRecord;
    private Room room;
    private User user;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(renovationDetailsController).build();
        renovationRecord = new RenovationRecord("John", "Cena", null, "john@cena.com");
        room = new Room("Room");
        room.setId(1L);
        renovationRecord.setRooms(List.of(room));
        room.setRenovationRecord(renovationRecord);
        user = new User("John", "Cena", "john@cena.com", "PASSWORD", null, null);
        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord));
        when(principal.getName()).thenReturn("john@cena.com");
    }

    @Test
    void testAddingTag_WithValidValues_TagAdded() throws Exception {
        when(moderationService.isProfanity(Mockito.anyString())).thenReturn(false);

        mockMvc.perform(post("/my-renovations/details/add-tag")
                        .param("recordId", "1")
                        .param("newTag", "Chic")
                        .param("job-page", "1")
                        .principal(principal))
                .andExpect(model().attributeDoesNotExist("tagErrorMessage"));
        ArgumentCaptor<Tag> tagArgumentCaptor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository, Mockito.times(1)).save(tagArgumentCaptor.capture());
        Tag tag = tagArgumentCaptor.getValue();
        assertEquals("chic", tag.getName());
    }

    @Test
    public void getRenovationDetails_RenovationRecordIsPrivateAndUserIsTheOwner_ReturnsRenovationDetailsTemplate() throws Exception {
        when(principal.getName()).thenReturn("john@example.com");
        User user = new User("John", "Doe", "john@example.com", "P4$$word", null, null);
        when(userRepository.findByEmailContainingIgnoreCase(Mockito.anyString())).thenReturn(user);

        RenovationRecord renovationRecord1 = new RenovationRecord("Record", "Record", List.of(), "john@example.com");
        renovationRecord1.setId(1L);
        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord1));
        when(jobRepository.findAllFromRenovationRecord(eq(renovationRecord1), any(Pageable.class))).thenReturn(new SliceImpl<>(Collections.emptyList()));


        mockMvc.perform(get("/my-renovations/details")
                        .param("recordId", "1")
                        .param("job-page", "1")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationDetailsTemplate"))
                .andExpect(model().attribute("publicUser", false));
    }

    @Test
    public void getRenovationDetails_RenovationRecordIsPrivateAndUserIsNotTheOwner_ReturnsErrorPage() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("jane@example.com");

        RenovationRecord renovationRecord = new RenovationRecord("Record", "Record", List.of(), "john@example.com");
        renovationRecord.setId(1L);
        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord));

        mockMvc.perform(get("/my-renovations/details")
                        .param("recordId", "1")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    public void getRenovationDetails_RenovationRecordIsPublicAndUserIsNotTheOwner_ReturnsRenovationDetailsTemplate() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("jane@example.com");
        User user = new User("Jane", "Doe", "jane@example.com", "P4$$word", null, null);
        when(userRepository.findByEmailContainingIgnoreCase(Mockito.anyString())).thenReturn(user);

        RenovationRecord renovationRecord = new RenovationRecord("Record", "Record", List.of(), "john@example.com");
        renovationRecord.setId(1L);
        renovationRecord.setIsPublic(true);
        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord));
        when(jobRepository.findAllFromRenovationRecord(eq(renovationRecord), any(Pageable.class))).thenReturn(new SliceImpl<>(Collections.emptyList()));


        mockMvc.perform(get("/my-renovations/details")
                        .param("recordId", "1")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationDetailsTemplate"))
                .andExpect(model().attribute("publicUser", true));
    }

    @Test
    public void getRenovationDetails_InvalidPageNumberForNonPaginatedJobs_ReturnsErrorPage() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("john@example.com");

        RenovationRecord renovationRecord = new RenovationRecord("Record", "Record", List.of(), "john@example.com");
        renovationRecord.setId(1L);
        renovationRecord.setIsPublic(true);
        renovationRecord.setJobs(List.of(new Job("Job", "Job", "", "")));
        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord));

        mockMvc.perform(get("/my-renovations/details")
                        .param("recordId", "1")
                        .param("job-page", "1000")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    public void getRenovationDetails_InvalidPageNumberForPaginatedJobs_ReturnsErrorPage() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("john@example.com");

        RenovationRecord renovationRecord = new RenovationRecord("Record", "Record", List.of(), "john@example.com");
        renovationRecord.setId(1L);
        renovationRecord.setIsPublic(true);
        List<Job> jobs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            jobs.add(new Job("Job", "Job", "", ""));
        }
        renovationRecord.setJobs(jobs);
        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord));

        mockMvc.perform(get("/my-renovations/details")
                        .param("recordId", "1")
                        .param("job-page", "1000")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    void testAddingTags_WithValidValues() throws Exception {

        RenovationRecord renovationRecord = new RenovationRecord();
        renovationRecord.setUserEmail("test@test.com");
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("test@test.com");
        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord));
        when(moderationService.isProfanity("Chic")).thenReturn(false);
        mockMvc.perform(post("/my-renovations/details/add-tag")
                .param("recordId", "1")
                .param("newTag", "Chic")
                .param("job-page", "1")
                .principal(principal));
        Tag tag = renovationRecord.getTags().get(0);
        assertEquals("chic", tag.getName());
    }

    static Stream<Arguments> invalidTags() {
        return Stream.of(
                Arguments.of("Tag;Tag"),
                Arguments.of("Tag&Tag")
        );
    }
    @ParameterizedTest
    @MethodSource("invalidTags")
    void testAddingTag_RecordNoTagsTagContainsInvalidCharacters_ThrowsError(String invalidTag) throws Exception {
        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord));
        when(principal.getName()).thenReturn("john@cena.com");
        when(jobRepository.findAllFromRenovationRecord(eq(renovationRecord), any(Pageable.class))).thenReturn(new SliceImpl<>(Collections.emptyList()));
        when(userRepository.findByEmailContainingIgnoreCase("john@cena.com")).thenReturn(user);

        mockMvc.perform(post("/my-renovations/details/add-tag")
                        .param("recordId", "1")
                        .param("newTag", invalidTag)
                        .param("job-page", "1")
                        .param("search", "false-0--")
                        .principal(principal))
                .andExpect(flash().attribute("tagErrorMessage", TagService.TAG_NAME_INVALID_CHARS));

        ArgumentCaptor<Tag> argument = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository, never()).save(argument.capture());
    }

    @Test
    void testAddingTag_RecordNoTagsTagNameLengthGreaterThan15_ThrowsError() throws Exception {
        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord));
        when(principal.getName()).thenReturn("john@cena.com");
        when(jobRepository.findAllFromRenovationRecord(eq(renovationRecord), any(Pageable.class))).thenReturn(new SliceImpl<>(Collections.emptyList()));
        when(userRepository.findByEmailContainingIgnoreCase("john@cena.com")).thenReturn(user);

        mockMvc.perform(post("/my-renovations/details/add-tag")
                        .param("recordId", "1")
                        .param("newTag", "TagTagTagTagTagT")
                        .param("job-page", "1")
                        .param("search", "false-0--")
                        .principal(principal))
                .andExpect(flash().attribute("tagErrorMessage", TagService.TAG_NAME_TOO_LONG));

        ArgumentCaptor<Tag> argument = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository, never()).save(argument.capture());
    }

    @Test
    void testAddingTag_RecordNoTagsTagNameNoLetters_ThrowsError() throws Exception {
        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord));
        when(principal.getName()).thenReturn("john@cena.com");
        when(jobRepository.findAllFromRenovationRecord(eq(renovationRecord), any(Pageable.class))).thenReturn(new SliceImpl<>(Collections.emptyList()));
        when(userRepository.findByEmailContainingIgnoreCase("john@cena.com")).thenReturn(user);
        mockMvc.perform(post("/my-renovations/details/add-tag")
                        .param("recordId", "1")
                        .param("newTag", "12345")
                        .param("job-page", "1")
                        .param("search", "false-0--")
                        .principal(principal))
                .andExpect(flash().attribute("tagErrorMessage", TagService.TAG_NAME_NO_LETTERS));

        ArgumentCaptor<Tag> argument = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository, never()).save(argument.capture());
    }

    @Test
    void testAddingTag_RecordFiveTagsTagNameValid_ThrowsError() throws Exception {
        renovationRecord.addTag(new Tag("a"));
        renovationRecord.addTag(new Tag("b"));
        renovationRecord.addTag(new Tag("c"));
        renovationRecord.addTag(new Tag("d"));
        renovationRecord.addTag(new Tag("e"));
        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord));
        when(principal.getName()).thenReturn("john@cena.com");
        when(jobRepository.findAllFromRenovationRecord(eq(renovationRecord), any(Pageable.class))).thenReturn(new SliceImpl<>(Collections.emptyList()));
        when(userRepository.findByEmailContainingIgnoreCase("john@cena.com")).thenReturn(user);

        mockMvc.perform(post("/my-renovations/details/add-tag")
                        .param("recordId", "1")
                        .param("newTag", "tag")
                        .param("job-page", "1")
                        .param("search", "false-0--")
                        .principal(principal))
                .andExpect(flash().attribute("tagErrorMessage", TagService.FIVE_TAGS_ALREADY_EXIST));

        ArgumentCaptor<Tag> argument = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository, never()).save(argument.capture());
    }

    @Test
    void testAddingTag_RecordOneTagsTagAlreadyExists_ThrowsError() throws Exception {
        renovationRecord.addTag(new Tag("a"));
        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord));
        when(principal.getName()).thenReturn("john@cena.com");
        when(jobRepository.findAllFromRenovationRecord(eq(renovationRecord), any(Pageable.class))).thenReturn(new SliceImpl<>(Collections.emptyList()));
        when(userRepository.findByEmailContainingIgnoreCase("john@cena.com")).thenReturn(user);

        mockMvc.perform(post("/my-renovations/details/add-tag")
                        .param("recordId", "1")
                        .param("newTag", "a")
                        .param("job-page", "1")
                        .param("search", "false-0--")
                        .principal(principal))
                .andExpect(flash().attribute("tagErrorMessage", TagService.TAG_ALREADY_EXISTS));

        ArgumentCaptor<Tag> argument = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository, never()).save(argument.capture());
    }

    @Test
    void testAddingTag_RecordNoTagsTagContainsProfanity_ThrowsError() throws Exception {
        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord));
        when(principal.getName()).thenReturn("john@cena.com");
        when(jobRepository.findAllFromRenovationRecord(eq(renovationRecord), any(Pageable.class))).thenReturn(new SliceImpl<>(Collections.emptyList()));
        when(userRepository.findByEmailContainingIgnoreCase("john@cena.com")).thenReturn(user);
        when(moderationService.isProfanity("fuck")).thenReturn(Boolean.TRUE);

        mockMvc.perform(post("/my-renovations/details/add-tag")
                        .param("recordId", "1")
                        .param("newTag", "fuck")
                        .param("job-page", "1")
                        .param("search", "false-0--")
                        .principal(principal))
                .andExpect(flash().attribute("tagErrorMessage", TagService.TAG_NAME_CONTAINS_PROFANITY));

        ArgumentCaptor<Tag> argument = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepository, never()).save(argument.capture());
    }

    @Test
    void removeTag_UserIsNotOwner_TagIsNotRemoved() throws Exception {
        RenovationRecord record = new RenovationRecord("name", "description", List.of(), "test@test.com");
        Tag tag = new Tag("choc");

        record.addTag(tag);
        tag.addRenovationRecord(record);

        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(tagRepository.findByName("choc")).thenReturn(tag);

        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("evil@evil.com");

        Assertions.assertTrue(!renovationRecordService.getRecordById(1).getTags().isEmpty());
        Assertions.assertTrue(!tagService.getTagByName("choc").getRenovationRecords().isEmpty());

        mockMvc.perform(post("/my-renovations/details/remove-tag")
                .param("recordId", "1")
                .param("tagNameRemove", "Choc")
                .principal(principal));

        Assertions.assertTrue(!renovationRecordService.getRecordById(1).getTags().isEmpty());
        Assertions.assertTrue(!tagService.getTagByName("choc").getRenovationRecords().isEmpty());
    }

    @Test
    void removeTag_UserIsOwnerAndTagNoLongerExists_TagIsRemovedFromDatabase() throws Exception {
        RenovationRecord record = new RenovationRecord("name", "description", List.of(), "test@test.com");
        Tag tag = new Tag("choc");

        record.addTag(tag);
        tag.addRenovationRecord(record);

        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(tagRepository.findByName("choc")).thenReturn(tag);

        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("test@test.com");

        Assertions.assertTrue(!renovationRecordService.getRecordById(1).getTags().isEmpty());
        Assertions.assertTrue(!tagService.getTagByName("choc").getRenovationRecords().isEmpty());

        mockMvc.perform(post("/my-renovations/details/remove-tag")
                .param("recordId", "1")
                .param("tagNameRemove", "Choc")
                .principal(principal));

        Assertions.assertTrue(renovationRecordService.getRecordById(1).getTags().isEmpty());
        Assertions.assertTrue(tagService.getTagByName("choc").getRenovationRecords().isEmpty());
        verify(tagRepository, Mockito.times(1)).delete(Mockito.any());
    }

    @Test
    void removeTag_UserIsOwnerAndTagExists_TagIsNotRemovedFromDatabase() throws Exception {
        RenovationRecord record = new RenovationRecord("name", "description", List.of(), "test@test.com");
        RenovationRecord record2 = new RenovationRecord("Example", "description", List.of(), "test@test.com");
        Tag tag = new Tag("choc");

        record.addTag(tag);
        tag.addRenovationRecord(record);
        tag.addRenovationRecord(record2);

        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(tagRepository.findByName("choc")).thenReturn(tag);

        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("test@test.com");

        Assertions.assertTrue(!renovationRecordService.getRecordById(1).getTags().isEmpty());
        Assertions.assertTrue(!tagService.getTagByName("choc").getRenovationRecords().isEmpty());

        mockMvc.perform(post("/my-renovations/details/remove-tag")
                .param("recordId", "1")
                .param("tagNameRemove", "Choc")
                .principal(principal));

        Assertions.assertTrue(renovationRecordService.getRecordById(1).getTags().isEmpty());
        assertEquals(tagService.getTagByName("choc").getRenovationRecords(), List.of(record2));
        verify(tagRepository, Mockito.times(0)).delete(Mockito.any());
    }

    @Test
    void removeTag_UserIsOwnerAndTagNotExists_NoTagsAreRemoved() throws Exception {
        RenovationRecord record = new RenovationRecord("name", "description", List.of(), "test@test.com");

        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("test@test.com");

        int renoSize = renovationRecordService.getRecordById(1).getTags().size();

        mockMvc.perform(post("/my-renovations/details/remove-tag")
                .param("recordId", "1")
                .param("tagNameRemove", "Choc")
                .principal(principal));

        Assertions.assertTrue(renovationRecordService.getRecordById(1).getTags().size() == renoSize);
    }
    @Test
    public void updatePublicStatus_InvalidRenovationRecordID_ReturnsErrorPage() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("john@example.com");

        RenovationRecord renovationRecord = new RenovationRecord("Record", "Record", List.of(), "john@example.com");
        renovationRecord.setId(1L);
        renovationRecord.setIsPublic(false);

        mockMvc.perform(post("/my-renovations/details/update-public-status")
                        .param("recordId", "2")
                        .param("public", "true")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    public void updatePublicStatus_PublicParameterSetToTrue_RenovationRecordSetToPublic() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("john@example.com");

        RenovationRecord renovationRecord = new RenovationRecord("Record", "Record", List.of(), "john@example.com");
        renovationRecord.setId(1L);
        renovationRecord.setIsPublic(false);
        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord));

        mockMvc.perform(post("/my-renovations/details/update-public-status")
                        .param("recordId", "1")
                        .param("public", "true")
                        .principal(mockPrincipal))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<RenovationRecord> recordCaptor = ArgumentCaptor.forClass(RenovationRecord.class);
        verify(renovationRecordRepository).save(recordCaptor.capture());
        RenovationRecord record = recordCaptor.getValue();
        Assertions.assertTrue(record.getIsPublic());
    }

    @Test
    public void getRenovationDetails_FilteredJobs_NoFilter() throws Exception {
        when(principal.getName()).thenReturn("john@example.com");
        User user = new User("John", "Doe", "john@example.com", "P4$$word", null, null);
        when(userRepository.findByEmailContainingIgnoreCase(Mockito.anyString())).thenReturn(user);

        RenovationRecord renovationRecord1 = new RenovationRecord("Record", "Record", List.of(), "john@example.com");
        Job job1 = new Job("job1", "job1", "28/02/2077", "28/03/2077");
        job1.setStatus("Not Started");
        renovationRecord1.setJobs(List.of(job1));
        renovationRecord1.setId(1L);
        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord1));
        when(jobRepository.findAllFromRenovationRecord(eq(renovationRecord1), any(Pageable.class))).thenReturn(new SliceImpl<>(List.of(job1)));


        mockMvc.perform(get("/my-renovations/details")
                        .param("recordId", "1")
                        .param("job-page", "1")
                        .param("filter", "false")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationDetailsTemplate"))
                .andExpect(model().attribute("publicUser", false))
                .andExpect(model().attribute("jobs", List.of(job1)))
                .andExpect(model().attribute("jobPages", Collections.singletonList(1)));
    }

    @Test
    public void getRenovationDetails_FilteredJobs_FilteredWithResult() throws Exception {
        when(principal.getName()).thenReturn("john@example.com");
        User user = new User("John", "Doe", "john@example.com", "P4$$word", null, null);
        when(userRepository.findByEmailContainingIgnoreCase(Mockito.anyString())).thenReturn(user);

        RenovationRecord renovationRecord1 = new RenovationRecord("Record", "Record", List.of(), "john@example.com");
        Job job1 = new Job("job1", "job1", "28/02/2077", "28/03/2077");
        job1.setStatus("Not Started");
        Job job2 = new Job("job2", "job2", "28/02/2077", "28/03/2077");
        job1.setStatus("In Progress");
        renovationRecord1.setJobs(List.of(job1, job2));
        renovationRecord1.setId(1L);
        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord1));
        when(jobRepository.findAllFromRenovationRecordFiltered(eq(renovationRecord1), any(Pageable.class), eq("Not Started"))).thenReturn(new SliceImpl<>(List.of(job1)));


        mockMvc.perform(get("/my-renovations/details")
                        .param("recordId", "1")
                        .param("job-page", "1")
                        .param("filter", "Not Started")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationDetailsTemplate"))
                .andExpect(model().attribute("publicUser", false))
                .andExpect(model().attribute("jobs", List.of(job1)))
                .andExpect(model().attribute("jobPages", Collections.singletonList(1)));
    }

    @Test
    public void getRenovationDetails_FilteredJobs_NoResults() throws Exception {
        when(principal.getName()).thenReturn("john@example.com");
        User user = new User("John", "Doe", "john@example.com", "P4$$word", null, null);
        when(userRepository.findByEmailContainingIgnoreCase(Mockito.anyString())).thenReturn(user);

        RenovationRecord renovationRecord1 = new RenovationRecord("Record", "Record", List.of(), "john@example.com");
        Job job1 = new Job("job1", "job1", "28/02/2077", "28/03/2077");
        job1.setStatus("Not Started");
        renovationRecord1.setId(1L);
        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord1));
        when(jobRepository.findAllFromRenovationRecordFiltered(eq(renovationRecord1), any(Pageable.class), eq("Blocked"))).thenReturn(new SliceImpl<>(Collections.emptyList()));


        mockMvc.perform(get("/my-renovations/details")
                        .param("recordId", "1")
                        .param("job-page", "1")
                        .param("filter", "Blocked")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationDetailsTemplate"))
                .andExpect(model().attribute("publicUser", false))
                .andExpect(model().attribute("jobs", Collections.emptyList()))
                .andExpect(model().attribute("jobPages", Collections.emptyList()));
    }

    @Test
    public void getRenovationDetails_JobsOnRenovationRecord_CorrectJobDetailsProvidedToCalendar() throws Exception {
        when(principal.getName()).thenReturn("jane@doe.nz");
        User user = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        when(userRepository.findByEmailContainingIgnoreCase(Mockito.anyString())).thenReturn(user);

        RenovationRecord renovationRecord1 = new RenovationRecord("Record", "Record", List.of(), "jane@doe.nz");
        Job job1 = new Job("job1", "job1", "28/03/2077", "28/02/2077");
        job1.setId(1L);
        Job job2 = new Job("job2", "job2", null, "28/02/2077");
        job2.setId(2L);
        Job job3 = new Job("job3", "job3", "28/03/2077", null);
        job3.setId(2L);
        Job job4 = new Job("job4", "job4", null, null);
        job4.setId(2L);
        renovationRecord1.setJobs(List.of(job1, job2, job3, job4));
        renovationRecord1.setId(1L);
        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord1));
        when(jobRepository.findAllFromRenovationRecord(eq(renovationRecord1), any(Pageable.class))).thenReturn(new SliceImpl<>(List.of(job1)));

        List<String> startDates = jobService.convertJobStartDatesForCalendar(renovationRecord1.getJobs());
        List<String> dueDates = jobService.convertJobDueDatesForCalendar(renovationRecord1.getJobs());

        mockMvc.perform(get("/my-renovations/details")
                        .param("recordId", "1")
                        .param("job-page", "1")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationDetailsTemplate"))
                .andExpect(model().attribute("jobIds", List.of(job1.getId(), job2.getId(), job3.getId(), job4.getId())))
                .andExpect(model().attribute("jobNames", List.of(job1.getName(), job2.getName(), job3.getName(), job4.getName())))
                .andExpect(model().attribute("jobStartDates", startDates))
                .andExpect(model().attribute("jobDueDates", dueDates));
    }

    @Test
    public void getRenovationDetails_JobWasModifiedRecently_CorrectLastModifiedJobList() throws Exception {
        when(principal.getName()).thenReturn("jane@doe.nz");
        User user = new User("Jane", "Doe", "jane@doe.nz", "P4$$word", null, null);
        when(userRepository.findByEmailContainingIgnoreCase(Mockito.anyString())).thenReturn(user);

        LocalDateTime tenSecondsAgo = LocalDateTime.now().minusSeconds(10);
        LocalDateTime now = LocalDateTime.now();
        RenovationRecord renovationRecord1 = new RenovationRecord("Record", "Record", List.of(), "jane@doe.nz");
        Job job1 = new Job("job1", "job1", "28/03/2077", "28/02/2077");
        job1.setId(1L);
        job1.setLastUpdated(tenSecondsAgo);
        Job job2 = new Job("job2", "job2", null, "28/02/2077");
        job2.setId(2L);
        job2.setLastUpdated(tenSecondsAgo);
        Job job3 = new Job("job3", "job3", "28/03/2077", null);
        job3.setId(2L);
        job3.setLastUpdated(tenSecondsAgo);
        Job job4 = new Job("job4", "job4", null, null);
        job4.setId(2L);
        job4.setLastUpdated(now);
        renovationRecord1.setJobs(List.of(job1, job2, job3, job4));
        renovationRecord1.setId(1L);
        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord1));
        when(jobRepository.findAllFromRenovationRecord(eq(renovationRecord1), any(Pageable.class))).thenReturn(new SliceImpl<>(List.of(job1)));

        List<Boolean> jobWasModified = jobService.jobsWereModified(renovationRecord1.getJobs());

        mockMvc.perform(get("/my-renovations/details")
                        .param("recordId", "1")
                        .param("job-page", "1")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationDetailsTemplate"))
                .andExpect(model().attribute("jobWasModified", jobWasModified));
    }

    static Stream<Arguments> validFiles() {
        return Stream.of(
                Arguments.of("test.png", "image/png"),
                Arguments.of("test.jpg", "image/jpeg"),
                Arguments.of("test.jpeg", "image/jpeg"),
                Arguments.of("test.svg", "image/svg+xml")
        );
    }
    @ParameterizedTest
    @MethodSource("validFiles")
    public void addRoomImage_ValidImageGiven_ImageAddedToRoom(String fileName, String fileType) throws Exception {
        MockMultipartFile file = new MockMultipartFile("submittedFile", fileName, fileType, "test".getBytes());

        mockMvc.perform(multipart("/add-room-image")
                        .file(file)
                        .param("recordId", "1")
                        .param("roomId", "1"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<RenovationRecord> recordCaptor = ArgumentCaptor.forClass(RenovationRecord.class);
        verify(renovationRecordRepository).save(recordCaptor.capture());
        RenovationRecord record = recordCaptor.getValue();
        assertEquals("room"+room.getId()+fileName, record.getRooms().getFirst().getImageFilename());
    }

    static Stream<Arguments> invalidFiles() {
        return Stream.of(
                Arguments.of("test.gif", "image/gif"),
                Arguments.of("test.bmp", "image/bmp"),
                Arguments.of("test.tiff", "image/tiff"),
                Arguments.of("test.webp", "image/webp")
        );
    }
    @ParameterizedTest
    @MethodSource("invalidFiles")
    public void addRoomImage_InvalidImageGiven_ImageNotAddedToRoom(String fileName, String fileType) throws Exception {
        MockMultipartFile file = new MockMultipartFile("submittedFile", fileName, fileType, "test".getBytes());

        mockMvc.perform(multipart("/add-room-image")
                        .file(file)
                        .param("recordId", "1")
                        .param("roomId", "1"))
                .andExpect(status().is3xxRedirection());

        verify(renovationRecordRepository, never()).save(renovationRecord);
        assertNull(renovationRecord.getRooms().getFirst().getImageFilename());
    }
}
