package nz.ac.canterbury.seng302.homehelper.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.controller.renovations.RenovationDetailsController;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.service.ImageService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class RenovationRoomImagesFeature {
    @Autowired
    private RenovationDetailsController renovationDetailsController;
    @Autowired
    private ImageService imageService;
    @Autowired
    private RenovationRecordService renovationRecordService;
    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    private MockMvc mockMvc;
    private MvcResult mvcResult;
    private MockMultipartFile file;
    private RenovationRecord renovationRecord;
    private Room room;

    @Before("@RenovationRoomImages")
    public void before() {
        renovationRecordRepository.deleteAll();

        mockMvc = MockMvcBuilders.standaloneSetup(renovationDetailsController).build();
        room = new Room("Room");
        room.setId(1L);
        renovationRecord = new RenovationRecord("Record", "Record", List.of(room), "jane@doe.nz");
        renovationRecord.setId(1L);
        renovationRecordService.addRenovationRecord(renovationRecord);
    }

    @Given("I choose a valid image of type {string} for my room")
    public void i_choose_a_valid_image_of_type_for_my_room(String fileType) {
        file = new MockMultipartFile("submittedFile", "test", fileType, "test".getBytes());
        assertDoesNotThrow(() -> imageService.validateImage(file));
    }

    @When("I submit the image")
    public void i_submit_the_image() throws Exception {
        mvcResult = mockMvc.perform(multipart("/add-room-image")
                        .file(file)
                        .param("recordId", renovationRecord.getId().toString())
                        .param("roomId", room.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
    }

    @Then("the image for my room is updated")
    public void the_image_for_my_room_is_updated() {
        renovationRecord = renovationRecordService.getRecordById(renovationRecord.getId());
        assertEquals("room"+room.getId()+file.getOriginalFilename(), renovationRecord.getRooms().getFirst().getImageFilename());
        ModelAndView mav = mvcResult.getModelAndView();
        assertNull(mav.getModel().get("fileErrorMessage"));
    }

    @Given("I choose an invalid image of type {string} for my room")
    public void i_choose_an_invalid_image_of_type_for_my_room(String fileType) {
        file = new MockMultipartFile("submittedFile", "test", fileType, "test".getBytes());
        assertThrows(IllegalArgumentException.class, () -> imageService.validateImage(file));
    }

    @Then("the image for my room is not updated and error message {string} is shown")
    public void the_image_for_my_room_is_not_updated_and_error_message_is_shown(String correctErrorMessage) {
        renovationRecord = renovationRecordService.getRecordById(renovationRecord.getId());
        assertNull(renovationRecord.getRooms().getFirst().getImageFilename());
        String errorMessage = (String) mvcResult.getFlashMap().get("fileErrorMessage");
        assertEquals(correctErrorMessage, errorMessage);
    }
}
