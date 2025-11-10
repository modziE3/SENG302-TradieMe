package nz.ac.canterbury.seng302.homehelper.service;

import jakarta.persistence.Tuple;
import nz.ac.canterbury.seng302.homehelper.entity.*;
import nz.ac.canterbury.seng302.homehelper.entity.dto.RenovationCardInfo;
import nz.ac.canterbury.seng302.homehelper.repository.RecentRenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Service class for RenovationRecords.
 * This class links automatically with @link{RenovationRecordRepository}
 */
@Service
public class RenovationRecordService {
    Logger logger = LoggerFactory.getLogger(RenovationRecordService.class);

    private final RenovationRecordRepository renovationRecordRepository;
    private final RecentRenovationRepository recentRenovationRepository;
    private final RoomRepository roomRepository;
    private final LocationService locationService;
    private final ValidationService validationService = new ValidationService();

    private final Integer numberOfRecords = 12;

    public static final String RENO_TITLE_EMPTY = "Renovation record name cannot be empty";
    public static final String RENO_TITLE_TOO_LONG = "Renovation record name must be 60 characters or less";
    public static final String RENO_TITLE_INCORRECT_CHARACTER = "Renovation record name must only include letters, numbers, spaces, dots, commas, hyphens or apostrophes";
    public static final String RENO_TITLE_NOT_UNIQUE = "Record name is not unique";
    public static final String RENO_DESCRIPTION_EMPTY = "Renovation record description cannot be empty";
    public static final String RENO_DESCRIPTION_TOO_LONG = "Renovation record description must be 512 characters or less";
    public static final String RENO_ROOM_NAME_INVALID = "Renovation record room names must only include letters, numbers, spaces, dots, commas, hyphens or apostrophes";

    @Autowired
    public RenovationRecordService(RenovationRecordRepository renovationRecordRepository, RoomRepository roomRepository,
                                   LocationService locationService, RecentRenovationRepository recentRenovationRepository) {
        this.renovationRecordRepository = renovationRecordRepository;
        this.roomRepository = roomRepository;
        this.locationService = locationService;
        this.recentRenovationRepository = recentRenovationRepository;
    }

    /**
     * Gets all renovation records in database storage
     * @return All renovation records currently saved in storage
     */
    public List<RenovationRecord> getRenovationRecords() {
        return renovationRecordRepository.findAll();
    }

    /**
     * Gets all renovation records owned by a user with a given email
     * in database storage.
     * @param email the email of the user.
     * @return All renovation records owned by user with given email.
     */
    public List<RenovationRecord> getRenovationRecordsByOwner(String email) {
        return renovationRecordRepository.findRenovationRecordsByEmail(email);
    }

    /**
     * Gets the renovation record with the given id.
     * @param id unique id of a renovation record.
     * @return The renovation record with the unique id
     */
    public RenovationRecord getRecordById(long id) {
        return renovationRecordRepository.findById(id).orElse(null);
    }

    /**
     * Gets the renovation record and validates it belongs to a user
     * @param id unique id of the renovation record
     * @param email email address of user who owns the renovation record
     * @return The renovation record if it exists and belongs to the user, otherwise null is returned
     */
    public RenovationRecord getAndAuthenticateRecord(long id, String email) {
        RenovationRecord record = getRecordById(id);
        record = (record != null && record.authenticatePage(id, email)) ? record : null;
        return record;
    }

    /**
     * Sets empty location information strings to null
     * @param locationInfo List of location information, containing street address, suburb, city, postcode and country
     * @return New list of location information
     */
    public List<String> setEmptyLocationsToNull(List<String> locationInfo) {
        locationInfo = locationInfo.stream()
                .map((location) -> {
                    if (location.isEmpty()) {
                        return null;
                    } else {
                        return location;
                    }
                }).toList();

        return locationInfo;
    }

    /**
     * Sets the location attributes of a renovation record
     * @param renovationRecord Renovation record that location attributes are set to
     * @param locationInfo List of location information, containing street address, suburb, city, postcode and country
     */
    public void setRenovationRecordLocation(RenovationRecord renovationRecord, List<String> locationInfo) {
        renovationRecord.setStreetAddress(locationInfo.get(0));
        renovationRecord.setSuburb(locationInfo.get(1));
        renovationRecord.setCity(locationInfo.get(2));
        renovationRecord.setPostcode(locationInfo.get(3));
        renovationRecord.setCountry(locationInfo.get(4));
    }

    /**
     * Adds a renovation record to database storage
     * @param renovationRecord Renovation record being added
     */
    public RenovationRecord addRenovationRecord(RenovationRecord renovationRecord) {
        return renovationRecordRepository.save(renovationRecord);
    }

    /**
     * Validates attribute values of a renovation record for editing.
     * If all values are correct, then record is saved with the new values.
     * Otherwise, throws an exception and displays all failed validations
     * @param renovationRecord renovation record that is being edited
     * @param name new name to be either rejected or saved
     * @param description new description to be either rejected or saved
     * @param rooms new rooms list to be either saved or rejected
     */
    public void editRenovationRecord(RenovationRecord renovationRecord, String name, String description, List<Room> rooms) {
        renovationRecord.setName(name);
        renovationRecord.setDescription(description);

        List<Room> roomsToDelete = new ArrayList<>();
        for (Room room : renovationRecord.getRooms()) {
            if (!rooms.stream().map(Room::getName).toList().contains(room.getName())) {
                roomsToDelete.add(room);
            }
        }
        for (Room room : roomsToDelete) {
            renovationRecord.getRooms().remove(room);
            roomRepository.deleteByRoomId(room.getId());
        }
        for (Room room : rooms) {
            if (!renovationRecord.getRooms().stream().map(Room::getName).toList().contains(room.getName())) {
                room.setRenovationRecord(renovationRecord);
                roomRepository.save(room);
            }
        }
        renovationRecordRepository.save(renovationRecord);

        for (Room room : roomsToDelete) {
            roomRepository.deleteById(room.getId());
        }
    }

    /**
     * Changes the public status of a renovation record and saves this change
     * @param record Renovation record being edited
     * @param isPublic Boolean value that the record's public status is being set to
     */
    public void editRenovationRecordPublicStatus(RenovationRecord record, boolean isPublic) {
        record.setIsPublic(isPublic);
        renovationRecordRepository.save(record);
    }

    /**
     * Adds a tag to the renovation record and saves this change
     * @param record Renovation record being edited
     * @param tag the tag being added to the renovations list of tags
     */
    public void editRenovationRecordTags(RenovationRecord record, Tag tag) {
        record.addTag(tag);
        renovationRecordRepository.save(record);
    }

    /**
     * Validates the values of a renovation record so it can either be added to the database or rejected.
     * @param record this record is only used in edit mode to check if the name is the same.
     * @param name the name of the renovation to be validated
     * @param description the description of the renovation to be validated
     * @param rooms the rooms of the renovations to be validated
     * @param editMode is a boolean that is used to tell the validation that the user is editing a
     *                 record rather than creating one
     * @throws IllegalArgumentException when the inputs arent valid/dont meet to ACs
     */
    public void validateRenovationRecord(RenovationRecord record, String name, String description, List<Room> rooms,
                                         List<String> locationInfo, String userEmail, boolean editMode) throws IllegalArgumentException {
        List<String> errors = new ArrayList<>();
        int maxTitleLength = 60;

        if (validationService.stringEmpty(name)) {
            errors.add(RENO_TITLE_EMPTY);
        } else if (name.length() > maxTitleLength) {
            errors.add(RENO_TITLE_TOO_LONG);
        }
        if (validationService.containsNonAlphaNumeric(name)) {
            errors.add(RENO_TITLE_INCORRECT_CHARACTER);
        }
        if (editMode) {
            if (!Objects.equals(record.getName(), name) && !recordNameUnique(name, userEmail)) {
                errors.add(RENO_TITLE_NOT_UNIQUE);
            }
        } else if (!recordNameUnique(name, userEmail)) {
            errors.add(RENO_TITLE_NOT_UNIQUE);
        }
        if (validationService.stringEmpty(description)) {
            errors.add(RENO_DESCRIPTION_EMPTY);
        } else if (!validationService.correctDescriptionLength(description)) {
            errors.add(RENO_DESCRIPTION_TOO_LONG);
        }
        for (Room room : rooms) {
            if (validationService.containsNonAlphaNumeric(room.getName())) {
                errors.add(RENO_ROOM_NAME_INVALID);
            }
        }
        if (!locationService.locationErrors(locationInfo).isEmpty()) {
            errors.add(locationService.locationErrors(locationInfo));
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }
    }

    /**
     * Goes through a string of all detected errors from the add renovation or edit renovation form
     * and assigns an error message from the string to be displayed for each
     * field of the form
     * @param allErrorMessages String of all errors detected from the add renovation or edit renovation form
     * @return List of error messages (one for each input field of the form) that will
     * be displayed on the form
     */
    public List<String> getErrorMessages(String allErrorMessages) {
        String renovationTitleErrorMessage = "";

        if (allErrorMessages.contains(RENO_TITLE_NOT_UNIQUE)) {
            renovationTitleErrorMessage = RENO_TITLE_NOT_UNIQUE;
        }
        if (allErrorMessages.contains(RENO_TITLE_INCORRECT_CHARACTER)) {
            renovationTitleErrorMessage = RENO_TITLE_INCORRECT_CHARACTER;
        }
        if (allErrorMessages.contains(RENO_TITLE_EMPTY)) {
            renovationTitleErrorMessage = RENO_TITLE_EMPTY;
        }
        if (allErrorMessages.contains(RENO_TITLE_TOO_LONG)) {
            renovationTitleErrorMessage = RENO_TITLE_TOO_LONG;
        }

        String renovationDescriptionErrorMessage = "";
        if (allErrorMessages.contains(RENO_DESCRIPTION_EMPTY)) {
            renovationDescriptionErrorMessage = RENO_DESCRIPTION_EMPTY;
        }
        if (allErrorMessages.contains(RENO_DESCRIPTION_TOO_LONG)) {
            renovationDescriptionErrorMessage = RENO_DESCRIPTION_TOO_LONG;
        }

        String renovationRoomsErrorMessage = "";
        if (allErrorMessages.contains(RENO_ROOM_NAME_INVALID)) {
            renovationRoomsErrorMessage = RENO_ROOM_NAME_INVALID;
        }

        return List.of(renovationTitleErrorMessage, renovationDescriptionErrorMessage, renovationRoomsErrorMessage);
    }

    /**
     * Deletes a renovation record from database storage
     * @param recordId ID of the record wanting to be deleted
     */
    public void deleteRenovationRecord(Long recordId) {
        recentRenovationRepository.deleteByRecordId(recordId);
        renovationRecordRepository.deleteById(recordId);
    }

    /**
     * Checks if the name of a record is unique from other record names
     * @param name Name of the renovation record
     * @return Boolean showing if the record name is unique
     */
    public boolean recordNameUnique(String name, String userEmail) {
        List<RenovationRecord> records = getRenovationRecordsByOwner(userEmail);
        for (RenovationRecord record: records)  {
            if (Objects.equals(record.getName(), name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * returns the renovationRecords where the user search string is contained within the record name or description
     * @param searchString the user search string to check the record against
     * @return a list of the records that contain the search string in the name or description.
     */
    public List<RenovationRecord> getMatchingRenovationRecords(String searchString, String currentUser, List<String> searchTags) {
        if (searchString == null) {
            searchString = "";
        }
        if (searchTags.isEmpty()) {
            logger.info("Not empty, empty");
            return renovationRecordRepository.findMatchingRenovationRecordsBySearchString(searchString, currentUser);
        }
        else if (searchString.isEmpty() && !searchTags.isEmpty()) {
            logger.info("empty, Not empty");
            return renovationRecordRepository.findMatchingRecordsByEmptyStringAndTags(currentUser, searchTags);
        } else {
            logger.info("Not empty, not empty - empty, empty");
            return renovationRecordRepository.findMatchingRecordsByStringAndTags(searchString, currentUser, searchTags);
        }
    }

    /**
     * Gets the total number of pages of records when paginating
     * Uses Math.ceil to round up the result so that the remaining jobs have a page.
     * @param records The RenovationRecords for which are returned by a search.
     * @return The total number of pages to be displayed.
     */
    public int getNumPages(List<RenovationRecord> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        return (int)Math.ceil((double) (records.size()) / numberOfRecords);
    }

    /**
     * Gets the sublist of records for the given page.
     * Uses the numberOfRecords constant to determine the number of records to be included in the sublist.
     * @param page The page number to select.
     * @param records The list of renovation records from the search query.
     * @return The sublist of the list containing the records to show on the page.
     */
    public List<RenovationRecord> getRenovationRecordPages(Integer page, List<RenovationRecord> records) {
        int pageIndex = page - 1;
        int startIndex = pageIndex * numberOfRecords;
        if (records.size() <= numberOfRecords) {
            startIndex = 0;
        }

        int endIndex = (pageIndex + 1) * numberOfRecords;
        if (endIndex > records.size()) {
            endIndex = records.size();
        }
        return records.subList(startIndex, endIndex);
    }

    /**
     * Gets a list of the pages to be displayed for job pagination.
     * If the list is more than 10 then it gets the 4 pages surrounding the current page and the final and first page and returns
     * the list as distinct elements that are filtered to be between the final and 0.
     * Makes the check and creates different lists in order to match the AC3 of UserStory 15
     * Used by the RenovationDetailsController
     * @param currentPage the currently active page to base the others off of.
     * @param records the record for which the jobs are from.
     * @return A list of the visible pagination button pages.
     */
    public List<Integer> getPageList(Integer currentPage, List<RenovationRecord> records) {
        int numPages = getNumPages(records);
        if (numPages > 10) {
            return Stream.of(1, currentPage - 2, currentPage - 1, currentPage, currentPage + 1, currentPage + 2, numPages)
                    .filter(num -> num > 0 && num <= numPages).distinct().toList();
        } else {
            return IntStream.range(1, (numPages+1)).boxed().toList();
        }
    }

    /**
     * Checks if a page number falls inside the page numbers for a list of renovation records
     * @param pageList list of page numbers
     * @param pageNumber page number being validation
     * @return true if page number in page list, false otherwise
     */
    public Boolean pageNumberIsInRecordPageList(List<Integer> pageList, Integer pageNumber) {
        return validationService.pageNumberIsInPageList(pageList, pageNumber);
    }

    /**
     * Writes a string to represent a renovation records location. If the location is
     * not set, the string returned is "location not set by owner".
     * @param renovationRecord the record that is being used to write thr location
     * @return a location string
     */
    public static String writeLocationString(RenovationRecord renovationRecord) {
        String suburb = renovationRecord.getSuburb();
        suburb = (suburb == null || suburb.isEmpty()) ? "" : suburb + ", ";

        return renovationRecord.getCity() == null ? "Location not set by owner" :
                renovationRecord.getStreetAddress() + ", "
                        + suburb
                        + renovationRecord.getCity();
    }

    /**
     * Gets all the city suburb pairs from the database
     * @return Map of cities which have a list of all the suburbs which are linked to them
     */
    public Map<String, List<String>> getCityAndSuburb() {
        Map<String, List<String>> cityAndSuburb = new HashMap<>();
        List<Tuple> locations = renovationRecordRepository.findCitySuburbs();
        for (Tuple location: locations) {
            if (!cityAndSuburb.containsKey(location.get(0).toString())) {
                cityAndSuburb.put(location.get(0).toString(), new ArrayList<>());
            }
            if (location.get(1) != null) {
                cityAndSuburb.get(location.get(0).toString()).add(location.get(1).toString());
            } else {
                cityAndSuburb.get(location.get(0).toString()).add("");
            }
        }
        return cityAndSuburb;
    }

    /**
     * Gets the information for renovation record info cards.
     * @param userService the userService to access the users for each record.
     * @param records the list of RenovationRecords to be used
     * @return List of RenovationCardInfo objects for the renovations in records
     */
    public List<RenovationCardInfo> getRenovationRecordCards(UserService userService, List<RenovationRecord> records) {
        List<RenovationCardInfo> cardInfos = new ArrayList<>();
        for (RenovationRecord record: records) {
            User user = userService.getUser(record.getUserEmail());
            cardInfos.add(
                    new RenovationCardInfo(record.getName(), record.getDescription(), record.getCity(), user.getFirstName() + " " + user.getLastName(), record.getId(), user != null && user.getProfilePicture() != null ? "/profileImages/" + user.getProfilePicture() : null));
        }
        return cardInfos;
    }
}
