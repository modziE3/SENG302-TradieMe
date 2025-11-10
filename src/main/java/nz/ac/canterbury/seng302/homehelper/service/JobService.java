package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.*;
import nz.ac.canterbury.seng302.homehelper.entity.dto.JobCardInfo;
import nz.ac.canterbury.seng302.homehelper.entity.dto.JobFilter;
import nz.ac.canterbury.seng302.homehelper.repository.RoomRepository;
import nz.ac.canterbury.seng302.homehelper.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService.writeLocationString;

/**
 * Service class for Jobs
 * This class links automatically with @link{JobRepository}
 */
@Service
public class JobService {
    private final JobRepository jobRepository;
    private final RoomRepository roomRepository;
    private final ValidationService validationService;

    private Integer numberOfJobs = 6;

    public static final String JOB_NAME_EMPTY_OR_INVALID = "Job name cannot be empty and must only include " +
            "letters, numbers, spaces, dots, hyphens or apostrophes";
    public static final String JOB_NAME_OVER_255_CHARS = "Job name must be 255 characters or less";

    public static final String JOB_DESCRIPTION_EMPTY = "Job description cannot be empty";
    public static final String JOB_DESCRIPTION_OVER_512_CHARS = "Job description must be 512 characters or less";

    public static final String JOB_TYPE_INVALID = "Job type must be chosen";

    public static final String JOB_DUE_DATE_EMPTY = "Due date cannot be empty";
    public static final String JOB_DUE_DATE_INVALID_FORMAT = "Due date is not in valid format, DD/MM/YYYY";
    public static final String JOB_DUE_DATE_NOT_IN_FUTURE = "Due date must be in the future";
    public static final String JOB_DUE_DATE_INVALID_DATE = "Due date is not a valid date";
    public static final String JOB_DUE_DATE_BEFORE_START = "Due date must be after start date";

    public static final String JOB_START_DATE_EMPTY = "Start date cannot be empty";
    public static final String JOB_START_DATE_INVALID_FORMAT = "Start date is not in valid format, DD/MM/YYYY";
    public static final String JOB_START_DATE_NOT_IN_FUTURE = "Start date must be in the future";
    public static final String JOB_START_DATE_INVALID_DATE = "Start date is not a valid date";
    public static final String JOB_START_DATE_AFTER_DUE = "Start date must be before due date";

    public static final List<String> ICON_LIST = List.of(
            "bolt-solid",
            "broom-solid",
            "faucet-solid",
            "hammer-solid",
            "house-chimney-crack-solid",
            "paint-roller-solid"
    );

    @Autowired
    public JobService(JobRepository jobRepository, RoomRepository roomRepository, ValidationService validationService) {
        this.jobRepository = jobRepository;
        this.roomRepository = roomRepository;
        this.validationService = validationService;
    }

    /**
     * Gets a job by its unique ID number
     * @param id ID number of the job
     * @return Job with the unique ID number
     */
    public Job getJobById(long id) {
        return jobRepository.findById(id).orElse(null);
    }

    /**
     * Gets a job and validates if it belongs to a renovation record
     * @param id ID of the job
     * @param record renovation record the job should belong to
     * @return The job if it exists and belongs to the renovation record, otherwise null is returned
     */
    public Job getAndAuthenticateJob(long id, RenovationRecord record) {
        Job job = getJobById(id);
        job = (job != null && Objects.equals(job.getRenovationRecord().getId(), record.getId())) ? job : null;
        return job;
    }

    /**
     * Gets all jobs in database storage
     * @return All jobs currently saved in storage
     */
    public List<Job> getJobs() {
        return jobRepository.findAll();
    }

    /**
     * Gets all posted jobs in database storage
     * @return All posted jobs currently saved in storage
     */
    public List<Job> getRecommendedPostedJobs(User user) {
        List<Job> postedJobs = new ArrayList<>(jobRepository.findPostedJobs().stream().filter(
                job -> !job.getRenovationRecord().getUserEmail().equals(user.getEmail())).toList());

        Collections.shuffle(postedJobs);

        if (!(user.getCity() == null || user.getCity().isEmpty())) {
            List<Job> nearbyJobs = postedJobs.stream().filter(
                    j -> j.getRenovationRecord().getCity() != null &&
                            Objects.equals(j.getRenovationRecord().getCity().toUpperCase(), user.getCity().toUpperCase())).toList();

            for (Job job : nearbyJobs) {
                postedJobs.remove(job);
                postedJobs.addFirst(job);
            }
        }

        int maxRecommendedJobs = 6;
        if (postedJobs.size() > maxRecommendedJobs) {
            postedJobs = postedJobs.subList(0, maxRecommendedJobs);
        }

        return postedJobs;
    }

    /**
     * Sets empty job due date to null before storing job
     * @param job job being stored
     */
    public Job addJob(Job job) throws IllegalArgumentException {
        if (validationService.stringEmpty(job.getDueDate())) {
            job.setDueDate(null);
        }
        if (validationService.stringEmpty(job.getStartDate())) {
            job.setStartDate(null);
        }
        return jobRepository.save(job);
    }

    /**
     * Adds renovation record rooms to a job if the room names are in the list of selected job rooms
     * @param record renovation record the rooms belong to
     * @param job new job which rooms will be added to
     * @param jobRoomNames list of record room names which will be added to the job
     */
    public void addJobRooms(RenovationRecord record, Job job, List<String> jobRoomNames) {
        for (String roomName : jobRoomNames) {
            for (Room room : record.getRooms()) {
                if (room.getName().equals(roomName)) {
                    room.addJob(job);
                    job.addRoom(room);
                }
            }
        }
    }

    /**
     * Validates the new name, description and due date entered into the edit job form
     * If all values are correct, then job is saved with new values
     * Otherwise, throws an exception and displays all failed validations
     * @param job job that is being edited
     * @param newName new name to either be rejected or saved
     * @param newDescription new description to either be rejected or saved
     * @param newType new type to either be rejected or saved
     * @param newDueDate new due date to either be rejected or saved
     * @param newStartDate new start date to either be rejected or saved
     */
    public void editJob(Job job, String newName, String newDescription, String newType, String newDueDate, String newStartDate)
            throws IllegalArgumentException, ParseException {
        Job validatejob = new Job(newName, newDescription, newDueDate, newStartDate);
        validatejob.setType(newType);
        validateJob(validatejob);

        List<String> newDetails = List.of(newName, newDescription, newType);
        List<String> oldDetails = List.of(job.getName(), job.getDescription(), job.getType());
        if (!newDetails.equals(oldDetails) || !job.getStartDate().equals(newStartDate) || !job.getDueDate().equals(newDueDate)) {
            job.setLastUpdated(LocalDateTime.now());
        }

        job.setName(newName);
        job.setDescription(newDescription);
        job.setType(newType);
        if (validationService.stringEmpty(newDueDate)) {
            job.setDueDate(null);
        } else {
            job.setDueDate(newDueDate);
        }
        if (validationService.stringEmpty(newStartDate)) {
            job.setStartDate(null);
        } else {
            job.setStartDate(newStartDate);
        }
        jobRepository.save(job);
    }

    /**
     * Changes the icon of the given job. null values for icon are allowed here as
     * they mean that the user has not chosen an icon yet.
     * @param jobId the id of the {@link Job} entity that needs its icon changed.
     * @param icon This is a string that represents a html class item for an icon.
     */
    public void editIcon(long jobId, String icon) {
        if (icon == null || ICON_LIST.contains(icon)) {
            Job job = jobRepository.findById(jobId).isPresent() ? jobRepository.findById(jobId).get() : null;
            if (job != null) {
                job.setIcon(icon);
                jobRepository.save(job);
            }
        }
    }

    /**
     * Looks through the room names entered into the edit job form to determine
     * which will be removed from the job rooms and which will be added to the job rooms
     * @param renovationRecord renovation record where all rooms belong to
     * @param job job that is being edited
     * @param roomNames list of room names that the job will be updated to have
     */
    public void editJobRooms(RenovationRecord renovationRecord, Job job, List<String> roomNames) {
        List<Room> roomsToRemove = new ArrayList<>();
        for (Room room : job.getRooms()) {
            if (!roomNames.contains(room.getName())) {
                roomsToRemove.add(room);
            }
        }
        for (Room room : roomsToRemove) {
            job.removeRoom(room);
            room.removeJob(job);
            roomRepository.save(room);
        }
        for (String roomName : roomNames) {
            for (Room room : renovationRecord.getRooms()) {
                if (room.getName().equals(roomName) && !job.getRooms().contains(room)) {
                    room.addJob(job);
                    job.addRoom(room);
                    roomRepository.save(room);
                }
            }
        }
    }

    /**
     * Changes the status of a job and saves the change in storage
     * @param job job whose status is being changed
     * @param newStatus the new job status the job will be changed to
     */
    public void editJobStatus(Job job, String newStatus) {
        job.setStatus(newStatus);
        if (Objects.equals(newStatus, "Completed")) {
            job.setCompletedTimestamp(LocalDate.now());
        }
        jobRepository.save(job);
    }

    /**
     * Changes the posted status of a job and saves the change in storage
     * @param job job whose posted status is being changed
     * @param isPosted boolean value that the job's posted status is being set to
     */
    public void editJobPostStatus(Job job, boolean isPosted) {
        job.setIsPosted(isPosted);
        jobRepository.save(job);
    }

    /**
     * Validates the name, description and due date of a job
     * @param job the job being validated
     */
    public void validateJob(Job job) throws IllegalArgumentException {
        List<String> errors = new ArrayList<>();

        int maxJobNameLength = 255;
        if (validationService.stringEmpty(job.getName()) ||
                validationService.containsNonAlphaNumeric(job.getName())) {
            errors.add(JOB_NAME_EMPTY_OR_INVALID);
        } else if (job.getName().length() > maxJobNameLength) {
            errors.add(JOB_NAME_OVER_255_CHARS);
        }

        if (validationService.stringEmpty(job.getDescription())) {
            errors.add(JOB_DESCRIPTION_EMPTY);
        } else if (!validationService.correctDescriptionLength(job.getDescription())) {
            errors.add(JOB_DESCRIPTION_OVER_512_CHARS);
        }

        boolean isValidStart = false;
        boolean isValidDueDate = false;
        String jobStartDateTrim = job.getStartDate() == null ? "" : job.getStartDate().trim();
        String jobDueDateTrim = job.getDueDate() == null ? "" : job.getDueDate().trim();

        if (!validationService.stringEmpty(jobDueDateTrim)) {
            if (!validationService.correctDateFormat(jobDueDateTrim)) {
                errors.add(JOB_DUE_DATE_INVALID_FORMAT);
            } else {
                try {
                    if (validationService.dateInThePast(jobDueDateTrim)) {
                        errors.add(JOB_DUE_DATE_NOT_IN_FUTURE);
                    } else {
                        isValidDueDate = true;
                    }
                } catch (ParseException e) {
                    errors.add(JOB_DUE_DATE_INVALID_DATE);
                }
            }
        }

        if (!validationService.stringEmpty(jobStartDateTrim)) {
            if (!validationService.correctDateFormat(jobStartDateTrim)) {
                errors.add(JOB_START_DATE_INVALID_FORMAT);
            } else {
                // Job start date should be able to be set in the past.
//                try {
//                    if (validationService.dateInThePast(jobStartDateTrim)) {
//                        errors.add(JOB_START_DATE_NOT_IN_FUTURE);
//                    } else {
//                        isValidStart = true;
//                    }
//                } catch (ParseException e) {
//                    errors.add(JOB_START_DATE_INVALID_DATE);
//                }
                isValidStart = true;
            }
        }

        if (isValidDueDate && isValidStart) {
            try {
                if (validationService.dateAfterAnotherDate(jobStartDateTrim, jobDueDateTrim)) {
                    errors.add(JOB_START_DATE_AFTER_DUE);
                }
            } catch (ParseException e) {
                errors.add("");
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errors));
        }
    }

    public void validateJobFilter(JobFilter jobFilter) throws IllegalArgumentException {
        List<String> errors = new ArrayList<>();

        if (!validationService.stringEmpty(jobFilter.city())) {
            if (!validationService.checkCity(jobFilter.city())) {
                errors.add(LocationService.CITY_INVALID);
            }
            if (!validationService.stringEmpty(jobFilter.suburb())) {
                if (!validationService.checkSuburb(jobFilter.suburb())) {
                    errors.add(LocationService.SUBURB_INVALID);
                }
            }
        }

        boolean isValidStart = false;
        boolean isValidDueDate = false;
        String jobStartDateTrim = jobFilter.startDate() == null ? "" : jobFilter.startDate().trim();
        String jobDueDateTrim = jobFilter.dueDate() == null ? "" : jobFilter.dueDate().trim();

        if (!validationService.stringEmpty(jobDueDateTrim)) {
            if (!validationService.correctDateFormat(jobDueDateTrim)) {
                errors.add(JOB_DUE_DATE_INVALID_FORMAT);
            } else {
                try {
                    if (validationService.dateInThePast(jobDueDateTrim)) {
                        errors.add(JOB_DUE_DATE_NOT_IN_FUTURE);
                    } else {
                        isValidDueDate = true;
                    }
                } catch (ParseException e) {
                    errors.add(JOB_DUE_DATE_INVALID_DATE);
                }
            }
        }

        if (!validationService.stringEmpty(jobStartDateTrim)) {
            if (!validationService.correctDateFormat(jobStartDateTrim)) {
                errors.add(JOB_START_DATE_INVALID_FORMAT);
            } else {
                try {
                    if (validationService.dateInThePast(jobStartDateTrim)) {
                        errors.add(JOB_START_DATE_NOT_IN_FUTURE);
                    } else {
                        isValidStart = true;
                    }
                } catch (ParseException e) {
                    errors.add(JOB_START_DATE_INVALID_DATE);
                }
            }
        }

        if (isValidDueDate && isValidStart) {
            try {
                if (validationService.dateAfterAnotherDate(jobStartDateTrim, jobDueDateTrim)) {
                    errors.add(JOB_START_DATE_AFTER_DUE);
                }
            } catch (ParseException e) {
                errors.add("");
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errors));
        }
    }

    /**
     * Goes through a string of all detected errors from the add job or edit job form
     * and assigns an error message from the string to be displayed for each
     * field of the form
     * @param allErrorMessages String of all errors detected from the add job or edit job form
     * @return List of error messages (one for each input field of the form) that will
     * be displayed on the form
     */
    public List<String> getErrorMessages(String allErrorMessages) {
        String newJobNameErrorMessage = "";
        if (allErrorMessages.contains(JOB_NAME_EMPTY_OR_INVALID)) {
            newJobNameErrorMessage = JOB_NAME_EMPTY_OR_INVALID;
        }
        if (allErrorMessages.contains(JOB_NAME_OVER_255_CHARS)) {
            newJobNameErrorMessage = JOB_NAME_OVER_255_CHARS;
        }

        String jobDescriptionErrorMessage = "";
        if (allErrorMessages.contains(JOB_DESCRIPTION_EMPTY)) {
            jobDescriptionErrorMessage = JOB_DESCRIPTION_EMPTY;
        }
        if (allErrorMessages.contains(JOB_DESCRIPTION_OVER_512_CHARS)) {
            jobDescriptionErrorMessage = JOB_DESCRIPTION_OVER_512_CHARS;
        }

        String jobTypeErrorMessage = "";
        if (allErrorMessages.contains(JOB_TYPE_INVALID)) {
            jobTypeErrorMessage = JOB_TYPE_INVALID;
        }

        String jobDueDateErrorMessage = "";
        if (allErrorMessages.contains(JOB_DUE_DATE_INVALID_FORMAT)) {
            jobDueDateErrorMessage = JOB_DUE_DATE_INVALID_FORMAT;
        }
        if (allErrorMessages.contains(JOB_DUE_DATE_NOT_IN_FUTURE)) {
            jobDueDateErrorMessage = JOB_DUE_DATE_NOT_IN_FUTURE;
        }
        if (allErrorMessages.contains(JOB_DUE_DATE_INVALID_DATE)) {
            jobDueDateErrorMessage = JOB_DUE_DATE_INVALID_DATE;
        }
        if (allErrorMessages.contains(JOB_DUE_DATE_BEFORE_START)) {
            jobDueDateErrorMessage = JOB_DUE_DATE_BEFORE_START;
        }
        if (allErrorMessages.contains(JOB_DUE_DATE_EMPTY)) {
            jobDueDateErrorMessage = JOB_DUE_DATE_EMPTY;
        }

        String jobStartDateErrorMessage = "";
        if (allErrorMessages.contains(JOB_START_DATE_INVALID_FORMAT)) {
            jobStartDateErrorMessage = JOB_START_DATE_INVALID_FORMAT;
        }
        if (allErrorMessages.contains(JOB_START_DATE_NOT_IN_FUTURE)) {
            jobStartDateErrorMessage = JOB_START_DATE_NOT_IN_FUTURE;
        }
        if (allErrorMessages.contains(JOB_START_DATE_INVALID_DATE)) {
            jobStartDateErrorMessage = JOB_START_DATE_INVALID_DATE;
        }
        if (allErrorMessages.contains(JOB_START_DATE_AFTER_DUE)) {
            jobStartDateErrorMessage = JOB_START_DATE_AFTER_DUE;
        }
        if (allErrorMessages.contains(JOB_START_DATE_EMPTY)) {
            jobStartDateErrorMessage = JOB_START_DATE_EMPTY;
        }

        return List.of(newJobNameErrorMessage, jobDescriptionErrorMessage, jobTypeErrorMessage,
                jobDueDateErrorMessage, jobStartDateErrorMessage);
    }

    public void validateJobBeforePosting(Job job) {
        List<String> errors = new ArrayList<>();

        try {
            validateJob(job);
        } catch (IllegalArgumentException e) {
            errors = new ArrayList<>(List.of(e.getMessage().split("\n")));
        }

        if (Objects.equals(job.getType(), "No Type")) {
            errors.add(JOB_TYPE_INVALID);
        }
        if (validationService.stringEmpty(job.getStartDate())) {
            errors.add(JOB_START_DATE_EMPTY);
        }
        if (validationService.stringEmpty(job.getDueDate())) {
            errors.add(JOB_DUE_DATE_EMPTY);
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errors));
        }
    }

    /**
     * Sets the number of jobs to show in one page.
     * This value is then used in the getNumPages and getJobPages methods and when
     * finding the number of pages and calling the jobRepository to get a Slice object for the jobs.
     * -> User selected job amount is not yet implemented.
     *
     * @param numberOfJobs The number of jobs to be displayed on a single page.
     */
    public void setNumberOfJobs(Integer numberOfJobs) {
        this.numberOfJobs = numberOfJobs;
    }

    /**
     * Gets the total number of pages of jobs when paginating by the set numberOfJobs
     * Uses Math.ceil to round up the result so that the remaining jobs have a page.
     * @param record The RenovationRecord for which the jobs are being paginated.
     * @param filter The filter being applied to the selected jobs
     * @return The total number of pages to be displayed.
     */
    public int getNumPages(RenovationRecord record, String filter) {
        if (numberOfJobs == 0 || record.getJobs() == null || record.getJobs().isEmpty()) {
            return 0;
        }
        if (!Objects.equals(filter, "false")) {
            return (int)Math.ceil((double) (record.getJobs().stream().filter(j -> Objects.equals(j.getStatus(), filter)).toList().size()) / numberOfJobs);
        }
        return (int)Math.ceil((double) (record.getJobs().size()) / numberOfJobs);
    }

    /**
     * Gets the Slice batch object of Jobs on the given page.
     * Uses the set numberOfJobs to determine the number of jobs to be included in the Slice.
     * @param page The page number to select. (Offset for the database query.)
     * @param renovationRecord The RenovationRecord for the jobs to be selected.
     * @param filter The filter being applied to the jobs being selected.
     * @return The Slice object containing the batch of jobs.
     */
    public Slice<Job> getJobPages(Integer page, RenovationRecord renovationRecord, String filter) {
        Integer userInputDifference = 1;
        Pageable pageRequest  = PageRequest.of(page - userInputDifference, numberOfJobs);
        List<String> filterList = Arrays.asList("In Progress", "Not Started", "Blocked", "Cancelled", "Completed");
        if (filterList.contains(filter)) {
            return jobRepository.findAllFromRenovationRecordFiltered(renovationRecord, pageRequest, filter);
        }
        return jobRepository.findAllFromRenovationRecord(renovationRecord, pageRequest);
    }


    /**
     * Gets a list of the pages to be displayed for job pagination.
     * If the list is more than 10 then it gets the 4 pages surrounding the current page and the final and first page and returns
     * the list as distinct elements that are filtered to be between the final and 0.
     * Makes the check and creates different lists in order to match the AC3 of UserStory 15
     * Used by the RenovationDetailsController
     * @param currentPage the currently active page to base the others off of.
     * @param renovationRecord the record for which the jobs are from.
     * @param filter The filter being applied to the jobs being selected.
     * @return A list of the visible pagination button pages.
     */
    public List<Integer> getPageList(Integer currentPage, RenovationRecord renovationRecord, String filter) {
        int numPages = getNumPages(renovationRecord, filter);
        if (numPages > 10) {
            return Stream.of(1, currentPage - 2, currentPage - 1, currentPage, currentPage + 1, currentPage + 2, numPages)
                    .filter(num -> num > 0 && num <= numPages).distinct().toList();
        } else {
            return IntStream.range(1, (numPages + 1)).boxed().toList();
        }
    }

    /**
     * Gets the total number of pages of jobs which have been posted when paginating by the set numberOfJobs
     * Uses Math.ceil to round up the result so that the remaining jobs have a page.
     * @return The total number of pages to be displayed.
     */
    public int getNumPages(List<Job> jobs) {
        if (numberOfJobs == 0 || jobs == null || jobs.isEmpty()) {
            return 0;
        }
        return (int)Math.ceil((double) (jobs.size()) / numberOfJobs);
    }

    /**
     * Gets the Slice batch object of Jobs which are posted on the given page.
     * Uses the set numberOfJobs to determine the number of jobs to be included in the Slice.
     * @return The Slice object containing the batch of jobs.
     */
    public List<Job> getFilteredJobs(JobFilter filter) {
        List<Job> jobs = jobRepository.findPostedJobs();

        if (filter.keywords() != null && !filter.keywords().isEmpty()) {
            jobs = jobs.stream().filter(j ->
                    j.getName().toLowerCase().contains(filter.keywords().toLowerCase()) ||
                            j.getDescription().toLowerCase().contains(filter.keywords().toLowerCase())).toList();
        }

        if (filter.jobTypes() != null && !filter.jobTypes().isEmpty()) {
            jobs = jobs.stream().filter(j ->
                    Objects.equals(j.getType(), filter.jobTypes())).toList();
        }

        if (filter.startDate() != null && !filter.startDate().isEmpty()) {
            jobs = jobs.stream().filter(j -> {
                try {
                    return (Objects.equals(j.getStartDate(), filter.startDate())) ||
                            validationService.dateAfterAnotherDate(j.getStartDate(), filter.startDate());
                } catch (ParseException e) {
                    return false;
                }
            }).toList();
        }
        if (filter.dueDate() != null && !filter.dueDate().isEmpty()) {
            jobs = jobs.stream().filter(j -> {
                try {
                    return (Objects.equals(j.getDueDate(), filter.dueDate())) ||
                            validationService.dateAfterAnotherDate(filter.dueDate(), j.getDueDate());
                } catch (ParseException e) {
                    return false;
                }
            }).toList();
        }
        if (filter.city() != null && !filter.city().isEmpty()) {
            jobs = jobs.stream().filter(j -> j.getRenovationRecord().getCity() != null && Objects.equals(j.getRenovationRecord().getCity().toUpperCase(), filter.city().toUpperCase())).toList();
        }
        if (filter.suburb() != null && !filter.suburb().isEmpty()) {
            jobs = jobs.stream().filter(j -> j.getRenovationRecord().getSuburb() != null && Objects.equals(j.getRenovationRecord().getSuburb().toUpperCase(), filter.suburb().toUpperCase())).toList();
        }
        return jobs;
    }


    /**
     * Gets a list of the pages to be displayed for posted job pagination.
     * If the list is more than 10 then it gets the 4 pages surrounding the current page and the final and first page and returns
     * the list as distinct elements that are filtered to be between the final and 0.
     * Makes the check and creates different lists in order to match the AC3 of UserStory 15
     * Used by the RenovationDetailsController
     * @param currentPage the currently active page to base the others off of.
     * @return A list of the visible pagination button pages.
     */
    public List<Integer> getPageListPosted(Integer currentPage, List<Job> jobs) {
        int numPages = getNumPages(jobs);
        if (numPages > 10) {
            return Stream.of(1, currentPage - 2, currentPage - 1, currentPage, currentPage + 1, currentPage + 2, numPages)
                    .filter(num -> num > 0 && num <= numPages).distinct().toList();
        } else {
            return IntStream.range(1, (numPages + 1)).boxed().toList();
        }
    }

    public List<Job> getPostedJobPages(Integer jobPage, List<Job> filteredJobs) {
        int pageIndex = jobPage - 1;
        int startIndex = pageIndex * numberOfJobs;
        if (filteredJobs.size() <= numberOfJobs) {
            startIndex = 0;
        }

        int endIndex = (pageIndex + 1) * numberOfJobs;
        if (endIndex > filteredJobs.size()) {
            endIndex = filteredJobs.size();
        }
        return filteredJobs.subList(startIndex, endIndex);
    }

    /**
     * Checks if a page number falls inside the page numbers for a list of jobs
     * @param pageList list of page numbers
     * @param pageNumber page number being validation
     * @return true if page number in page list, false otherwise
     */
    public Boolean pageNumberIsInJobPageList(List<Integer> pageList, Integer pageNumber) {
        return validationService.pageNumberIsInPageList(pageList, pageNumber);
    }

    /**
     * Creates a job card which contains info about a job that is to be advertised on
     * the job listings page. This is done for several jobs and the function gathers them in a list.
     * The jobs returned will ONLY be posted jobs.
     * @param userService the service to get to jobs owners
     * @param jobs the jobs to be filtered and converted.
     * @return a list of jobCards to be display
     */
    public List<JobCardInfo> getJobCardsPosted(UserService userService, List<Job> jobs) {
        return getJobCards(userService, jobs, true);
    }

    /**
     * Creates a job card which contains info about a job that is to be advertised on
     * the job listings page. This is done for several jobs and the function gathers them in a list.
     * The jobs returned will be all jobs not just posted jobs.
     * @param userService the service to get to jobs owners
     * @param jobs the jobs to be filtered and converted.
     * @return a list of jobCards to be display
     */
    public List<JobCardInfo> getJobCardsAll(UserService userService, List<Job> jobs) {
        return getJobCards(userService, jobs, false);
    }

    /**
     * Creates a job card which contains info about a job that is to be advertised on
     * the job listings page. This is done for several jobs and the function gathers them in a list.
     * @param userService the service to get to jobs owners
     * @param jobs the jobs to be filtered and converted.
     * @param shouldBePosted if the jobs returned should be posted jobs
     * @return a list of jobCards to be display
     */
    public List<JobCardInfo> getJobCards(UserService userService, List<Job> jobs, boolean shouldBePosted) {
        List<JobCardInfo> jobCards = new ArrayList<>();
        for (Job job : jobs.stream().filter(job -> !shouldBePosted || job.getIsPosted()).toList()) {
            if (job != null) {
                User user = userService.getUser(job.getRenovationRecord().getUserEmail());
                jobCards.add(new JobCardInfo(
                        job.getName(),
                        user.getFirstName() + " " + user.getLastName(),
                        job.getStartDate(),
                        job.getDueDate(),
                        writeLocationString(job.getRenovationRecord()),
                        job.getType(),
                        "Budget not set", // this is because there is no budget yet
                        job.getStatus(),
                        job.getIcon(),
                        job.getId(),
                        job.getRooms().isEmpty() ? "/images/JobDefault.png" : (job.getRooms().getFirst().getImageFilename() == null ? "/images/RenovationRoomDefault.png" : "/profileImages/" + job.getRooms().getFirst().getImageFilename())
                ));
            }
        }
        return jobCards;
    }



    /**
     * Converts all start date formats of jobs into the correct format required for adding jobs to the job calendar
     * @param jobs List of jobs being displayed on the calendar
     * @return List of converted job start date strings
     */
    public List<String> convertJobStartDatesForCalendar(List<Job> jobs) {
        SimpleDateFormat originalFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
        return jobs.stream().map(j -> {
            if (j.getStartDate() == null || j.getStartDate().isEmpty()) {
                return null;
            }
            try {
                Date date = originalFormat.parse(j.getStartDate());
                return targetFormat.format(date);
            } catch (ParseException e) {
                return null;
            }
        }).toList();
    }

    /**
     * Converts all due date formats of jobs into the correct format required for adding jobs to the job calendar
     * @param jobs List of jobs being displayed on the calendar
     * @return List of converted job due date strings
     */
    public List<String> convertJobDueDatesForCalendar(List<Job> jobs) {
        SimpleDateFormat originalFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        return jobs.stream().map(j -> {
            if (j.getDueDate() == null || j.getDueDate().isEmpty()) {
                return null;
            }
            try {
                Date date = originalFormat.parse(j.getDueDate());
                if (j.getStartDate() == null || j.getStartDate().isEmpty()) {
                    return targetFormat.format(date);
                } else {
                    calendar.setTime(date);
                    calendar.add(Calendar.DATE, 1);
                    return targetFormat.format(calendar.getTime());
                }
            } catch (ParseException e) {
                return null;
            }
        }).toList();
    }

    /**
     * Returns a list of boolean values indicating if a job in a list of jobs was modified in the last 10 seconds
     * @param jobs List of jobs being displayed on the calendar
     * @return Boolean list indicating which jobs have been modified
     */
    public List<Boolean> jobsWereModified(List<Job> jobs) {
        return jobs.stream().map(j -> {
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(now, j.getLastUpdated());
            long diff = Math.abs(duration.toSeconds());
            return diff < 10;
        }).toList();
    }
}
