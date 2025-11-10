package nz.ac.canterbury.seng302.homehelper.controller.renovations;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Room;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.service.LocationQueryService;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.security.Principal;
import java.util.List;
import java.util.Objects;

/**
 * Controller for the add/edit renovation form
 */
@Controller
public class RenovationFormController {
    private final RenovationRecordRepository renovationRecordRepository;
    Logger logger = LoggerFactory.getLogger(RenovationFormController.class);

    private final RenovationRecordService renovationRecordService;
    private final LocationService locationService;
    private final LocationQueryService locationQueryService;

    @Autowired
    public RenovationFormController(RenovationRecordService renovationRecordService, LocationService locationService,
                                    LocationQueryService locationQueryService, RenovationRecordRepository renovationRecordRepository) {
        this.renovationRecordService = renovationRecordService;
        this.locationService = locationService;
        this.locationQueryService = locationQueryService;
        this.renovationRecordRepository = renovationRecordRepository;
    }

    /**
     * Gets the form for creating a new renovation record
     * @param displayRooms rooms list displayed on the page
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param request HttpServletRequest used to get previous page
     * @return thymeleaf renovationForm
     */
    @GetMapping("/my-renovations/create-renovation")
    public String getCreateRenovationRecordForm(
            @RequestParam(name = "displayRooms", defaultValue = "") String displayRooms,
            Model model,
            HttpServletRequest request,
            HttpSession session) {
        logger.info("GET /my-renovations/create-renovation");

        locationQueryService.setLocationQuerySessionAttribute(request, session);

        model.addAttribute("renoName", "");
        model.addAttribute("renoDescription", "");
        model.addAttribute("streetAddress", "");
        model.addAttribute("suburb", "");
        model.addAttribute("city", "");
        model.addAttribute("postcode", "");
        model.addAttribute("country", "");
        model.addAttribute("latitude", "");
        model.addAttribute("longitude", "");
        model.addAttribute("displayRooms", displayRooms.trim());
        model.addAttribute("descLen", "0/512");
        model.addAttribute("prevPage", request.getHeader("Referer"));
        return "renovationFormTemplate";
    }

    /**
     * Posts the title, description, and list of rooms entered in the form and
     * creates a new renovation record using them
     * @param title Title entered in the form
     * @param description Description entered in the form
     * @param rooms List of rooms entered in the form
     * @param streetAddress Street address entered in the form
     * @param suburb Suburb entered in the form
     * @param city City entered in the form
     * @param postcode Postcode entered in the form
     * @param country Country entered in the form
     * @param prevPage Page that the user was on before trying to add a record
     * @param principal the currently authenticated user.
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @return thymeleaf renovationForm
     */
    @PostMapping("/my-renovations/create-renovation")
    public String attemptAddRenovationRecord(
            @RequestParam(name = "title") String title,
            @RequestParam(name = "description") String description,
            @RequestParam(name = "rooms") String rooms,
            @RequestParam(name = "streetAddress") String streetAddress,
            @RequestParam(name = "suburb") String suburb,
            @RequestParam(name = "city") String city,
            @RequestParam(name = "postcode") String postcode,
            @RequestParam(name = "country") String country,
            @RequestParam(name = "latitude") String latitude,
            @RequestParam(name = "longitude") String longitude,
            @RequestParam(name = "prevPage") String prevPage,
            Principal principal,
            Model model) {
        logger.info("POST /my-renovations/create-renovation");

        List<String> roomNamesList = splitRoomNamesString(rooms);
        List<Room> roomsList = roomNamesList.stream().map(Room::new).toList();
        try {
            RenovationRecord record = new RenovationRecord(title, description, roomsList, principal.getName());
            try {
                record.setLatitude(Float.parseFloat(latitude));
                record.setLongitude(Float.parseFloat(longitude));
            } catch (Exception ignored) {}
            List<String> locationInfo = renovationRecordService.setEmptyLocationsToNull(
                    List.of(streetAddress, suburb, city, postcode, country));
            renovationRecordService.validateRenovationRecord(record, title, description, roomsList, locationInfo,
                    principal.getName(), false);

            renovationRecordService.setRenovationRecordLocation(record, locationInfo);
            renovationRecordService.addRenovationRecord(record);

            return "redirect:/my-renovations/details?recordId="+record.getId();
        } catch (IllegalArgumentException e) {
            logger.error("Form submission error {}", e.getMessage());
            model.addAttribute("renoName", title);
            model.addAttribute("renoDescription", description);
            model.addAttribute("streetAddress", streetAddress);
            model.addAttribute("suburb", suburb);
            model.addAttribute("city", city);
            model.addAttribute("postcode", postcode);
            model.addAttribute("country", country);
            model.addAttribute("latitude", latitude);
            model.addAttribute("longitude", longitude);
            model.addAttribute("renoRooms", "[" + String.join("`", roomNamesList) + "]");
            model.addAttribute("descLen", description.codePointCount(0, description.length()) + "/512");
            model.addAttribute("prevPage", prevPage);

            setErrorMessages(model, e.getMessage());

            return "renovationFormTemplate";
        }
    }

    /**
     * Gets the details of a renovation record and displays them in the input fields of an edit form
     * @param recordId ID of the renovation record
     * @param principal the currently authenticated user.
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param request HttpServletRequest used to get previous page
     * @return thymeleaf renovationForm
     */
    @GetMapping("/my-renovations/edit-renovation")
    public String getEditRenovationRecordForm(
            @RequestParam(name = "recordId") long recordId,
            @RequestParam(name = "search", required = false) String search,
            Principal principal,
            Model model,
            HttpServletRequest request,
            HttpSession session) {
        logger.info("GET /my-renovations/edit-renovation");

        try {
            locationQueryService.setLocationQuerySessionAttribute(request, session);

            RenovationRecord record = renovationRecordService.getAndAuthenticateRecord(recordId, principal.getName());

            model.addAttribute("searched", search.split("-", 2)[0]);
            model.addAttribute("search",
                    ((Objects.equals(search.split("-", 2)[1], "null"))) ? "" : search.split("-", 2)[1]);
            model.addAttribute("renoName", record.getName());
            model.addAttribute("oldName", record.getName());
            model.addAttribute("recordId", record.getId());
            model.addAttribute("renoDescription", record.getDescription());
            model.addAttribute("streetAddress", record.getStreetAddress());
            model.addAttribute("suburb", record.getSuburb());
            model.addAttribute("city", record.getCity());
            model.addAttribute("postcode", record.getPostcode());
            model.addAttribute("country", record.getCountry());
            model.addAttribute("latitude", record.getLatitude() == 0.0f ? null : record.getLatitude());
            model.addAttribute("longitude", record.getLongitude() == 0.0f ? null : record.getLongitude());
            model.addAttribute("renoRooms",
                    "["+String.join("`", record.getRooms().stream().map(Room::getName).toList())+"]");
            model.addAttribute("mode", "edit");
            model.addAttribute("prevPage", request.getHeader("Referer"));
            model.addAttribute("descLen",
                    record.getDescription().codePointCount(0, record.getDescription().length())+"/512");

            return "renovationFormTemplate";
        } catch (NullPointerException e) {
            logger.warn(e.getMessage());
            model.addAttribute("loggedIn", principal != null);
            model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
            return "error";
        }
    }

    /**
     * Attempts to edit the record and redirect to the record details page.
     * @param title new title to be validated
     * @param description new description to be validated
     * @param streetAddress Street address entered in the form
     * @param suburb Suburb entered in the form
     * @param city City entered in the form
     * @param postcode Postcode entered in the form
     * @param country Country entered in the form
     * @param rooms new rooms list to be validated
     * @param oldTitle old title of record so we can find the record
     * @param prevPage page that the user was on before trying to edit
     * @param recordId id of the record so we can find the record
     * @param search search information
     * @param principal current authenticated user
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @return thymeleaf renovationForm
     */
    @PostMapping("/my-renovations/edit-renovation")
    public String attemptEditRenovationRecord(
            @RequestParam(name = "title") String title,
            @RequestParam(name = "description") String description,
            @RequestParam(name = "streetAddress") String streetAddress,
            @RequestParam(name = "suburb") String suburb,
            @RequestParam(name = "city") String city,
            @RequestParam(name = "postcode") String postcode,
            @RequestParam(name = "country") String country,
            @RequestParam(name = "latitude") String latitude,
            @RequestParam(name = "longitude") String longitude,
            @RequestParam(name = "rooms") String rooms,
            @RequestParam(name = "oldName") String oldTitle,
            @RequestParam(name = "prevPage") String prevPage,
            @RequestParam(name = "recordId") long recordId,
            @RequestParam(name = "search") String search,
            Principal principal,
            Model model) {
        logger.info("POST /my-renovations/edit-renovation");

        List<String> roomNamesList = splitRoomNamesString(rooms);
        List<Room> roomsList = roomNamesList.stream().map(Room::new).toList();
        try {
            RenovationRecord record = renovationRecordService.getAndAuthenticateRecord(recordId, principal.getName());

            List<String> locationInfo = renovationRecordService.setEmptyLocationsToNull(
                    List.of(streetAddress, suburb, city, postcode, country));
            renovationRecordService.validateRenovationRecord(record, title, description, roomsList, locationInfo,
                    principal.getName(), true);

            renovationRecordService.setRenovationRecordLocation(record, locationInfo);
            renovationRecordService.editRenovationRecord(record, title, description, roomsList);

            try {
                renovationRecordRepository.updateCoordinates(recordId, Float.parseFloat(latitude), Float.parseFloat(longitude));
            } catch (Exception ignored) {}

            return "redirect:/my-renovations/details?recordId="+record.getId() + "&search=" + search;
        } catch (IllegalArgumentException e) {
            logger.error("Edit form submission error {}", e.getMessage());

            model.addAttribute("search", search);
            model.addAttribute("renoName", title);
            model.addAttribute("oldName", oldTitle);
            model.addAttribute("recordId", recordId);
            model.addAttribute("renoDescription", description);
            model.addAttribute("streetAddress", streetAddress);
            model.addAttribute("suburb", suburb);
            model.addAttribute("city", city);
            model.addAttribute("postcode", postcode);
            model.addAttribute("country", country);
            model.addAttribute("latitude", latitude);
            model.addAttribute("longitude", longitude);
            model.addAttribute("renoRooms", "["+String.join("`", roomNamesList)+"]");
            model.addAttribute("mode", "edit");
            model.addAttribute("descLen", description.codePointCount(0, description.length())+"/512");
            model.addAttribute("prevPage", prevPage);

            setErrorMessages(model, e.getMessage());

            return "renovationFormTemplate";
        } catch (NullPointerException e) {
            logger.warn(e.getMessage());
            model.addAttribute("loggedIn", principal != null);
            model.addAttribute("renovationRecords", renovationRecordService.getRenovationRecordsByOwner(principal.getName()));
            return "error";
        }
    }

    /**
     * Splits string of room names into a list
     * @param rooms string of room names
     * @return list of room name strings
     */
    private List<String> splitRoomNamesString(String rooms) {
        String roomsDecoded = StringEscapeUtils.unescapeHtml4(rooms); //ChatGpt code
        String roomsString = roomsDecoded.replaceAll("^\"|\"$", "").trim(); //Chatgpt code
        List<String> roomNamesList = Arrays.stream(roomsString.split("`"))
                .filter(room -> !room.isBlank())
                .toList();
        return roomNamesList;
    }

    /**
     * Sets error messages for the add/edit renovation form
     * @param model (map-like) representation of data to be used in thymeleaf display
     * @param allErrorMessages String of all error messages wanting to be displayed
     */
    private void setErrorMessages(Model model, String allErrorMessages) {
        List<String> errorMessages = renovationRecordService.getErrorMessages(allErrorMessages);
        model.addAttribute("renovationTitleErrorMessage", errorMessages.get(0));
        model.addAttribute("renovationDescriptionErrorMessage", errorMessages.get(1));
        model.addAttribute("renovationRoomErrorMessage", errorMessages.get(2));

        locationService.setLocationErrorMessages(allErrorMessages, model);
    }
}
