package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.*;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.dto.MapMarker;
import nz.ac.canterbury.seng302.homehelper.entity.dto.MapPosition;
import nz.ac.canterbury.seng302.homehelper.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for User, defined by the @link{Service} annotation.
 * This class links automatically with @link{UserRepository}, see the @link{Autowired} annotation below
 */
@Service
public class UserService {
    private static final long TEN_MINUTES = 600000; // in milliseconds

    // Error messages for invalid user details
    public static final String FIRST_NAME_EMPTY = "First name cannot be empty";
    public static final String FIRST_NAME_INVALID_CHARS = "First name must only include letters, spaces, hyphens or apostrophes";
    public static final String FIRST_NAME_OVER_64_CHARS = "First name must be 64 characters long or less";
    public static final String LAST_NAME_INVALID_CHARS = "Last name must only include letters, spaces, hyphens or apostrophes";
    public static final String LAST_NAME_OVER_64_CHARS = "Last name must be 64 characters long or less";
    public static final String EMAIL_INVALID = "Email address must be in the form 'jane@doe.nz'";
    public static final String EMAIL_EXISTS = "This email address is already in use";
    public static final String PASSWORD_INVALID = "Your password must be at least 8 " +
            "characters long and include at least one uppercase letter, one lowercase letter, one number" +
            ", and one special character";
    public static final String PASSWORDS_DONT_MATCH = "Passwords do not match";

    private final UserRepository userRepository;
    private final ValidationService validationService;
    private final PasswordEncoder passwordEncoder;
    private final TaskScheduler taskScheduler;
    private final LocationService locationService;
    private final RenovationRecordService renovationRecordService;
    private final RenovationRecordRepository renovationRecordRepository;
    private final RecentRenovationRepository recentRenovationRepository;
    private final QuoteRepository quoteRepository;

    @Autowired
    public UserService(UserRepository userRepository, ValidationService validationService, TaskScheduler taskScheduler, LocationService locationService, RenovationRecordService renovationRecordService, RenovationRecordRepository renovationRecordRepository, RecentRenovationRepository recentRenovationRepository, QuoteRepository quoteRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.validationService = validationService;
        this.taskScheduler = taskScheduler;
        this.locationService = locationService;
        this.renovationRecordService = renovationRecordService;
        this.renovationRecordRepository = renovationRecordRepository;
        this.recentRenovationRepository = recentRenovationRepository;
        this.quoteRepository = quoteRepository;
    }

    /**
     * Gets all Users from persistence
     * @param email string to search on name (partial matching)
     * @return all UserResults currently saved in persistence
     */
    public User getUser(String email) {
        return userRepository.findByEmailContainingIgnoreCase(email);
    }

    /**
     * Gets a user by its ID from persistence
     * @param id ID to search for the user
     * @return User currently saved in persistence
     */
    public User getUserById(Long id) {
        return userRepository.findUserById(id);
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
     * Sets the location attributes of a user
     * @param user User that location attributes are set to
     * @param locationInfo List of location information, containing street address, suburb, city, postcode and country
     */
    public void setUserLocation(User user, List<String> locationInfo) {
        user.setStreetAddress(locationInfo.get(0));
        user.setSuburb(locationInfo.get(1));
        user.setCity(locationInfo.get(2));
        user.setPostcode(locationInfo.get(3));
        user.setCountry(locationInfo.get(4));
    }

    /**
     * Adds a new user to the database.
     * @param user the new user to be added.
     * @throws IllegalArgumentException when the inputs for the user details do not pass the required checks.
     */
    public void addUser(User user) throws IllegalArgumentException {
        user.setCreatedTimestamp(LocalDate.now());
        userRepository.save(user);
        taskScheduler.schedule(() -> {
            User inDatabaseUser = userRepository.findByEmailContainingIgnoreCase(user.getEmail());
            if (inDatabaseUser.getVerificationCode() != null) {
                userRepository.delete(user);
            }
        }, Instant.now().plusMillis(TEN_MINUTES));
    }

    /**
     * Takes a new user password and encodes it and then updates the user details for the user with the given email so that
     * the new hashed password is stored instead of their old one.
     * @param email the email of the user whose password is being updated.
     * @param newPassword the new plaintext password.
     * @throws IllegalArgumentException in the case that there is any problems updating the database.
     */
    public void updateUserPassword(String email, String newPassword) throws IllegalArgumentException {
        userRepository.updatePasswordWithUserEmail(email, passwordEncoder.encode(newPassword));
    }

    /**
     * When a new user has inputs details into the registration form,
     * these details including the first name, last name, email, and passwords are checked
     * to see if they meet validation criteria
     * @param firstName The user first name to check
     * @param lastName The user last name to check
     * @param email The user email to check
     * @param firstPassword The first password input
     * @param secondPassword The second password input
     * @param locationInfo The user location input
     * @throws IllegalArgumentException throws if any of the inputs are invalid.
     */
    public void validateUserWithPasswordAndEmailUnique(String firstName, String lastName, String email, String firstPassword,
                                                       String secondPassword, List<String> locationInfo, Long userId, boolean checkPassword)
            throws IllegalArgumentException {
        String errors = "";

        try {
            validateUser(email, firstName, lastName, userId);
        } catch (IllegalArgumentException e) {
            errors = errors.concat(" " + e.getMessage());
        }

        User checkerUser = new User(firstName, lastName, email, firstPassword, null, null);

        if (checkPassword) {
            try {
                validatePassword(firstPassword, secondPassword, checkerUser);
            } catch (IllegalArgumentException e) {
                errors = errors.concat(" " + e.getMessage());
            }
        }

        if (!locationService.locationErrors(locationInfo).isEmpty()) {
            errors = errors.concat(" " + locationService.locationErrors(locationInfo));
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors);
        }
    }

    /**
     * Checks the user details to ensure that they meet the validation criteria.
     * @param email The user email to check
     * @param firstName The user first name to check
     * @param lastName The user last name to check
     * @throws IllegalArgumentException throws if any of the inputs are invalid.
     */
    public void validateUser(String email, String firstName, String lastName, Long userId) throws IllegalArgumentException {
        List<String> errors = new ArrayList<>();

        if (validationService.stringEmpty(firstName)) {
            errors.add(FIRST_NAME_EMPTY);
        }
        if (!validationService.checkName(firstName)) {
            errors.add(FIRST_NAME_INVALID_CHARS);
        }
        if (!validationService.correctNameLength(firstName)) {
            errors.add(FIRST_NAME_OVER_64_CHARS);
        }
        if (!validationService.checkName(lastName)) {
            errors.add(LAST_NAME_INVALID_CHARS);
        }
        if (!validationService.correctNameLength(lastName)) {
            errors.add(LAST_NAME_OVER_64_CHARS);
        }
        if (!validationService.checkEmailForm(email)) {
            errors.add(EMAIL_INVALID);
        }
        try {
            checkEmailNotUsed(userId, email);
        } catch (IllegalArgumentException e) {
            errors.add(e.getMessage());
        }
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }
    }

    /**
     * Checks the user password inputs when a new user is being registered.
     * @param firstPassword The first password input
     * @param secondPassword The second password input
     * @throws IllegalArgumentException Throws if the password inputs do not meet the validation checks.
     */
    public void validatePassword(String firstPassword, String secondPassword, User user) throws IllegalArgumentException {
        List<String> errors = new ArrayList<>();

        if (!validationService.checkPassword(firstPassword, user)) {
            errors.add(PASSWORD_INVALID);
        }
        if (!validationService.passwordMatch(firstPassword, secondPassword)) {
            errors.add(PASSWORDS_DONT_MATCH);
        }
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }
    }


    /**
     * Checks that the email entered is not being used by another email.
     * @param userId The user id of the user trying to use the input email.
     *               Enter as null when registering a new user.
     * @param email The email to check
     * @throws IllegalArgumentException Throws if the email is being used already.
     */
    public void checkEmailNotUsed(Long userId, String email) throws IllegalArgumentException {
        if (!checkEmailIsUnique(email)) {
            User checkUser = getUser(email);
            if (!Objects.equals(checkUser.getId(), userId)) {
                throw new IllegalArgumentException(EMAIL_EXISTS);
            }
        }
    }

    /**
     * Checks that the location information provided is valid
     * @param locationInfo List of location information, containing street address, suburb, city, postcode and country
     * @throws IllegalArgumentException throws if the location is not valid.
     */
    public void validateLocation(List<String> locationInfo) throws IllegalArgumentException {
        if (!validationService.correctLocationInfo(locationInfo)) {
            throw new IllegalArgumentException("Location must have at least a street address");
        }
    }

    /**
     * Goes through a string of all detected errors from a user details form
     * and assigns an error message from the string to be displayed for each
     * field of the form
     * @param allErrorMessages String of all errors detected from a user details form
     * @return List of error messages (one for each input field of the form) that will
     * be displayed on the form
     */
    public List<String> getErrorMessages(String allErrorMessages) {
        String firstNameErrorMessage = "";
        if (allErrorMessages.contains(FIRST_NAME_EMPTY)) {
            firstNameErrorMessage = FIRST_NAME_EMPTY;
        }
        if (allErrorMessages.contains(FIRST_NAME_INVALID_CHARS)) {
            firstNameErrorMessage = FIRST_NAME_INVALID_CHARS;
        }
        if (allErrorMessages.contains(FIRST_NAME_OVER_64_CHARS)) {
            firstNameErrorMessage = FIRST_NAME_OVER_64_CHARS;
        }

        String lastNameErrorMessage = "";
        if (allErrorMessages.contains(LAST_NAME_INVALID_CHARS)) {
            lastNameErrorMessage = LAST_NAME_INVALID_CHARS;
        }
        if (allErrorMessages.contains(LAST_NAME_OVER_64_CHARS)) {
            lastNameErrorMessage = LAST_NAME_OVER_64_CHARS;
        }

        String emailErrorMessage = "";
        if (allErrorMessages.contains(EMAIL_INVALID)) {
            emailErrorMessage = EMAIL_INVALID;
        }
        if (allErrorMessages.contains(EMAIL_EXISTS)) {
            emailErrorMessage = EMAIL_EXISTS;
        }

        String passwordErrorMessage = "";
        if (allErrorMessages.contains(PASSWORD_INVALID)) {
            passwordErrorMessage = PASSWORD_INVALID;
        }

        String secondPasswordErrorMessage = "";
        if (allErrorMessages.contains(PASSWORDS_DONT_MATCH)) {
            secondPasswordErrorMessage = PASSWORDS_DONT_MATCH;
        }

        return List.of(firstNameErrorMessage, lastNameErrorMessage, emailErrorMessage, passwordErrorMessage, secondPasswordErrorMessage);
    }

    /**
     * Uses {@code getSecureRandomSixDigitCode(r)} to generate a random six-digit code. This function ensures it's not
     * already in the database.
     * @return a string of a six-digit code.
     */
    public String generateValidationCode() {
        SecureRandom random = new SecureRandom();
        String verificationCode = getSecureRandomSixDigitCode(random);
        while (Objects.equals(verificationCode, userRepository.findVerificationCodeByVerificationCode(verificationCode))) {
            verificationCode = getSecureRandomSixDigitCode(random);
        }
        return verificationCode;
    }

    /**
     * Generates a random six-digit code.
     * @param random the random used to generate the code.
     * @return string of a random six-digit code.
     */
    public String getSecureRandomSixDigitCode(SecureRandom random) {
        return String.format("%06d", random.nextInt(999999));
    }

    /**
     * Sets the users verify code to be null, this should happen when the user is being verified.
     * @param user to be verified
     */
    public void updateUserToBeVerified(User user) {
        user.setUserVerifyCodeNull(); // If an account is verified its code is null
        userRepository.save(user);
    }

    /**
     * checks if a user already has the email that is passed in
     * @param email the email that is going to be checked
     * @return true if there is no user that is already associated with that email
     */
    public boolean checkEmailIsUnique(String email){
        return userRepository.findByEmailContainingIgnoreCase(email) == null;
    }

    /**
     * Updates a users list of most recently visited jobs, a new job is added to the front of the list.
     * @param user the user to be updated.
     * @param job the job being added to the users list.
     */
    public void addJobToUsersMostRecent(User user, Job job) {
        List<Long> oldJobs = new ArrayList<>();
        oldJobs.addAll(user.getRecentJobs());
        oldJobs.remove(job.getId());
        List<Long> newJobs = new ArrayList<>();
        newJobs.add(job.getId());
        for (int i = 0; i < Math.min(oldJobs.size(), 3); i++) {
            newJobs.add(oldJobs.get(i));
        }
        user.setRecentJobs(newJobs);
        userRepository.save(user);
    }

    /**
     * uses new details to update existing user details
     * @param userId user's id (doesn't change)
     * @param newEmail user's updated email
     * @param newFirstName user's updated first name
     * @param newLastName user's updated last name
     */
    @Transactional
    public void updateDetails(Long userId, String newEmail, String newFirstName, String newLastName,
                              String newProfileImage) throws IllegalArgumentException {
        userRepository.updateDetails(userId, newEmail, newFirstName, newLastName, newProfileImage);
    }

    /**
     * User nw location details to update existing user location details
     * @param userId User's ID
     * @param locationInfo List of location information, containing street address, suburb, city, postcode and country
     */
    @Transactional
    public void updateLocation(Long userId, List<String> locationInfo) {
        String streetAddress = locationInfo.get(0);
        String suburb = locationInfo.get(1);
        String city = locationInfo.get(2);
        String postcode = locationInfo.get(3);
        String country = locationInfo.get(4);

        userRepository.updateLocation(userId, streetAddress, suburb, city, postcode, country);
    }

    /**
     * Updates the users recently viewed renovations. Adds the renovation to the start of the list so they are in
     * order of most recent first and removes oldest when the list contains more than 10 items
     * @param user the user to update
     * @param viewedRenovation the renovation record to add to the recently viewed
     */
    @Transactional
    public void updateRecentRenovations(User user, RenovationRecord viewedRenovation) {
        List<RecentRenovation> recentRenovations = user.getRecentRenovations();
        recentRenovations.sort(Comparator.comparing(RecentRenovation::getTimestamp).reversed());
        RecentRenovation recentRenovation =  new RecentRenovation(user, viewedRenovation);
        if (recentRenovations.stream().map(RecentRenovation::getRenovationRecord).toList().contains(viewedRenovation)) {
            int index = recentRenovations.stream().map(RecentRenovation::getRenovationRecord).toList().indexOf(viewedRenovation);
            recentRenovationRepository.delete(recentRenovations.get(index));
            recentRenovations.remove(index);
            userRepository.save(user);
            renovationRecordRepository.save(viewedRenovation);
        }
        recentRenovations.addFirst(recentRenovation);
        if (recentRenovations.size() > 10) {
            recentRenovationRepository.delete(recentRenovations.getLast());
            recentRenovations.removeLast();
        }
        user.setRecentRenovations(recentRenovations);
        recentRenovationRepository.save(recentRenovation);
        userRepository.save(user);
        renovationRecordRepository.save(viewedRenovation);
    }

    /**
     * Gets the users list of recently viewed renovations and ensures they are in order of newest first
     * @param user the user whose recent renovations are being got
     * @return the list of recently viewed RenovationRecords
     */
    public List<RenovationRecord> getRecentRenovations(User user) {
        List<RecentRenovation> recentRenovations = user.getRecentRenovations();
        recentRenovations.sort(Comparator.comparing(RecentRenovation::getTimestamp));
        List<RenovationRecord> renovations = new ArrayList<>();
        for (RecentRenovation r : recentRenovations) {
            RenovationRecord viewedRenovation = renovationRecordService.getRecordById(r.getRenovationRecord().getId());
            renovations.addFirst(viewedRenovation);
        }
        return renovations;
    }

    /**
     * gets the jobs that a user has worked on as a tradie that have been completed
     * @param userId the users id
     * @return a list of completed jobs worked on by the user
     */
    public List<Job> getCompletedJobsUserHasWorkedOn(Long userId) {
        List<Quote> quotes = quoteRepository.findAllByUserId(userId);

        List<Job> completedJobs = new ArrayList<>();
        for (Quote quote : quotes) {
            if (Objects.equals(quote.getStatus(), "Accepted") && Objects.equals(quote.getJob().getStatus(), "Completed")) {
                completedJobs.add(quote.getJob());
            }
        }
        return completedJobs;
    }

    /**
     * Gets the jobs that a user has worked on as a tradie that have been completed
     * and the user has added to their portfolio of jobs on their profile
     * @param user the user
     * @return a list of completed jobs worked on by the user that are also in the user's list of portfolio jobs
     */
    public List<Job> getPortfolioJobs(User user) {
        List<Job> completedJobs = getCompletedJobsUserHasWorkedOn(user.getId());
        List<Job> portfolioJobs = new ArrayList<>();
        for (Job job : completedJobs) {
            if (job.getPortfolioUsers().stream().map(u -> u.getId()).toList().contains(user.getId())) {
                portfolioJobs.add(job);
            }
        }

        return portfolioJobs;
    }

    /**
     * Gets a list of map marker dtos. These carry the coords and name and id.
     * @param userId tradie that has the portfolio jobs.
     * @return a list of map marker dtos.
     */
    public List<MapMarker> getPortfolioMapMarkers(long userId) {
        User tradie = this.getUserById(userId);
        return tradie.getPortfolioJobs().stream()
                .filter(job -> { try {
                    float lat = job.getRenovationRecord().getLatitude();
                    float lng = job.getRenovationRecord().getLongitude();
                    return lat != 0 && lng != 0;
                } catch (NullPointerException e) {return false;}})
                .map(job -> new MapMarker(job.getName(), job.getId(), new MapPosition(job.getRenovationRecord().getLatitude(), job.getRenovationRecord().getLongitude())))
                .toList();
    }

    /**
     * gets the 5 tradies with the highest amount of accepted quotes
     * @return a list of the 5 tradies with the highest amount of accepted quotes
     */
    public List<User> getHottestTradies() {
        List<User> tradies = userRepository.findAllQuoteSenders();
        return tradies.stream()
                .sorted(Comparator.comparingInt(User::getNumberOfAcceptedQuotes).reversed())
                .limit(5)
                .toList();
    }

    /**
     * Calculates the average work efficiency of a user across all their completed jobs. Jobs without a completion timestamp are ignored.
     * <p>
     * Efficiency is measured as the ratio of the actual time taken to complete a job
     * versus the planned time (from start date to due date). A ratio of:
     * <ul>
     *   <li><b>1.0</b> means the job was completed exactly on time.</li>
     *   <li><b>&lt; 1.0</b> means the job was completed faster than planned.</li>
     *   <li><b>&gt; 1.0</b> means the job took longer than planned.</li>
     * </ul>
     *
     * @param userId the id of the user
     * @return the average efficiency ratio
     */
    public double getUsersWorkEfficiency(Long userId) {
        List<Job> completedJobs = getCompletedJobsUserHasWorkedOn(userId).stream()
                .filter(j -> j.getCompletedTimestamp() != null)
                .toList();
        if (completedJobs.isEmpty()) return 0.0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        double totalEfficiency = 0;
        int count = 0;
        for (Job job : completedJobs) {
            LocalDate startDate = LocalDate.parse(job.getStartDate(), formatter);
            LocalDate dueDate = LocalDate.parse(job.getDueDate(), formatter);
            LocalDate completedDate = job.getCompletedTimestamp();
            long plannedDays = ChronoUnit.DAYS.between(startDate, dueDate);
            long actualDays = ChronoUnit.DAYS.between(startDate, completedDate);
            if (actualDays <= 0) continue;
            if (plannedDays <= 0) plannedDays = 1;

            double efficiencyRatio = (double) actualDays / plannedDays;
            totalEfficiency += efficiencyRatio;
            count++;
        }
        if (count == 0) return 0.0;
        return Math.round((totalEfficiency / count) * 100.0) / 100.0;
    }
}
