package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Entity class reflecting a user
 */
@Entity
@Table(name = "tab_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column
    private String streetAddress;

    @Column
    private String suburb;

    @Column
    private String city;

    @Column
    private String postcode;

    @Column
    private String country;

    @Column
    private float latitude;

    @Column
    private float longitude;

    @Column
    private LocalDate createdTimestamp;

    @Column
    private String verificationCode;

    @Column
    private String profilePictureFilename;

    @Column
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private List<Authority> userRoles = new ArrayList<>();

    @Column
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private List<Quote> quotes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_recent_jobs", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "job_id")
    private List<Long> recentJobs = new ArrayList<>();

    @Column
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<RecentRenovation> recentRenovations = new LinkedList<>();

    @Column
    @OneToMany(mappedBy = "receivingUser", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Rating> receivedRatings = new ArrayList<>();

    @Column
    @OneToMany(mappedBy = "sendingUser", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Rating> sentRatings = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_portfolio_jobs",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "job_id")
    )
    private List<Job> portfolioJobs = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "home_page_widget_order", joinColumns = @JoinColumn(name = "user_id"))
    @Column
    private List<String> homePageWidgetOrder = new ArrayList<>(List.of("Job Recommendations", "Job Calendar", "Recently Viewed Jobs", "Recently Viewed Renovations", "Hottest Tradies Leaderboard"));

    protected User() {}

    /**
     * Constructor for the user entities in the database.
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's email
     * @param password the hashed password for the user.
     */
    public User(String firstName, String lastName, String email, String password,
                String profilePictureFilename, String verificationCode) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.profilePictureFilename = profilePictureFilename;
        this.verificationCode = verificationCode;
    }

    /**
     * Gives a specified authority to the user.
     * @param authority the role of user e.g "ROLE_USER" or "ROLE_ADMIN"
     */
    public void grantAuthority(String authority) {
        userRoles.add(new Authority(authority));
    }

    /**
     * Sets the users most recently visited jobs to a given list of jobs. This should be used when the list needs updating.
     * @param recentJobs the new list of jobs that represent the users recent jobs.
     */
    public void setRecentJobs(List<Long> recentJobs) {
        this.recentJobs = recentJobs;
    }

    /**
     * Gets the users most recently visited jobs from the database. This should have a max of maybe four jobs.
     * @return a list of the users most recently visited jobs (in order)
     */
    public List<Long> getRecentJobs() {
        return recentJobs;
    }

    /**
     * Gets a copy of the authorities currently given to the user.
     * @return a list of granted authorities.
     */
    public List<GrantedAuthority> getAuthorities(){
        List<GrantedAuthority> authorities = new ArrayList<>();
        this.userRoles.forEach(authority -> authorities.add(new SimpleGrantedAuthority(authority.getRole())));
        return authorities;
    }

    /**
     * Gets the user's first name.
     * @return The user's first name as a String.
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * Sets the user's first name.
     * @param firstName
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the user's last name.
     * @return The user's last name as a String
     */
    public String getLastName() {
        return this.lastName;
    }

    /**
     * sets the user's last name
     * @param lastName
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    /**
     * Gets the user's email.
     * @return The user's email as a String.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets user email.
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's password.
     * @return The user's password as a String.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets user street address.
     * @param streetAddress
     */
    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    /**
     * Gets the user's street address.
     * @return The user's street address as a String.
     */
    public String getStreetAddress() {
        return streetAddress;
    }

    /**
     * Sets user suburb.
     * @param suburb
     */
    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    /**
     * Gets the user's suburb.
     * @return The user's suburb as a String.
     */
    public String getSuburb() {
        return suburb;
    }

    /**
     * Sets user city.
     * @param city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Gets the user's city.
     * @return The user's city as a String.
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets user postcode.
     * @param postcode
     */
    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    /**
     * Gets the user's postcode.
     * @return The user's postcode as a String.
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * Sets user country.
     * @param country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Gets the user's country.
     * @return The user's country as a String.
     */
    public String getCountry() {
        return country;
    }

    /**
     * Gets the timestamp for the creation of the users profile.
     * @return The timestamp for user creation as a LocalDate
     */
    public LocalDate getCreatedTimestamp() {
        return createdTimestamp;
    }

    /**
     * Gets the timestamp for the creation of the users profile and formats it to be shown on the screen
     * @return The formatted timestamp of the user creation date
     */
    public String getFormattedCreatedTimestamp() {
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return createdTimestamp.format(myFormatObj);
    }

    /**
     * Gets the user's verification code.
     * @return The user's verification code as a string
     */
    public String getVerificationCode() {
        return verificationCode;
    }

    /**
     * Sets the users verification code to be null, this means the user is verified
     */
    public void setUserVerifyCodeNull() {
        this.verificationCode = null;
    }

    /**
     * Sets the creation timestamp for the user.
     * @param now the LocalDate for the user creation
     */
    public void setCreatedTimestamp(LocalDate now) {
        createdTimestamp = now;
    }

    /**
     * Sets the user password.
     * @param password The String to be the new user password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the unique user id attached to the user.
     * @return A long of the user id.
     */
    public Long getId() {
        return id;
    }

    public String getProfilePicture() {  return profilePictureFilename; }

    public void setProfilePicture(String profilePicture) { this.profilePictureFilename = profilePicture; }

    /**
     * Set the user id for testing purposes only
     * @param id A long
     */
    public void setId(Long id) {this.id = id;}

    /**
     * Gets the users recently viewed renovations
     * @return the users recently viewed renovations as a Deque
     */
    public List<RecentRenovation> getRecentRenovations() {
        return recentRenovations;
    }

    /**
     * Sets the users recently viewed renovations
     */
    public void setRecentRenovations(List<RecentRenovation> recentRenovations) {
        this.recentRenovations = recentRenovations;
    }

    public List<Rating> getReceivedRatings() {
        return receivedRatings;
    }

    public void setReceivedRatings(List<Rating> receivedRatings) {
        this.receivedRatings = receivedRatings;
    }

    public List<Rating> getSentRatings() {
        return sentRatings;
    }

    public void setSentRatings(List<Rating> sentRatings) {
        this.sentRatings = sentRatings;
    }

    /**
     * Calculates the average rating for a user out of all ratings that user has received
     * @return Average of all received ratings, or zero if there are no received ratings
     */
    public double getAverageRating() {
        if (receivedRatings.isEmpty()) {
            return 0;
        } else {
            int ratingsSum = 0;
            for (Rating rating : receivedRatings) {
                ratingsSum += rating.getRating();
            }
            float averageRating = (float) ratingsSum / receivedRatings.size();
            double scale = Math.pow(10, 2);
            return Math.round(averageRating * scale) / scale;
        }
    }

    public List<Job> getPortfolioJobs() {
        return portfolioJobs.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    public void setPortfolioJobs(List<Job> portfolioJobs) {
        this.portfolioJobs = portfolioJobs;
    }

    public void addPortfolioJob(Job job) {
        this.portfolioJobs.add(job);
    }

    public void removePortfolioJob(Job job) {
        this.portfolioJobs.remove(job);
    }

    /**
     * Gets the list of home page widget names that are listed in the order they appear on the home page of the user
     * @return List of home page widget names in the order they appear on the home page of the user
     */
    public List<String> getHomePageWidgetOrder() {
        return homePageWidgetOrder;
    }

    /**
     * Sets the list of home page widget names that are listed in the order they appear on the home page of the user
     * @param homePageWidgetOrder List of home page widget names in the order they will appear on the home page of the user
     */
    public void setHomePageWidgetOrder(List<String> homePageWidgetOrder) {
        this.homePageWidgetOrder = homePageWidgetOrder;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    /**
     * should only be used in testing
     * @param quotes
     */
    public void setQuotes(List<Quote> quotes) {
        this.quotes = quotes;
    }

    /**
     * gets the number of quotes the user has sent and been accepted
     * @return the number of quotes the user has sent and been accepted
     */
    public int getNumberOfAcceptedQuotes() {
        return (int) quotes.stream()
                .filter(q -> "Accepted".equals(q.getStatus()))
                .count();
    }

    /**
     * chat helped write this. It gets the users ratings formatted as : ⭐ 4.6 (2,321)
     * @return a string formatted as ⭐ 4.6 (2,321)
     */
    public String formatRatings() {
        if (receivedRatings == null || receivedRatings.isEmpty()) {
            return "⭐ No ratings yet";
        }
        double average = receivedRatings.stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0.0);
        int total = receivedRatings.size();
        DecimalFormat avgFormat = new DecimalFormat("#.0");
        NumberFormat countFormat = NumberFormat.getIntegerInstance();
        return "⭐ " + avgFormat.format(average) + " (" + countFormat.format(total) + ")";
    }

    /**
     * Formats a string as account age e.g. 2 years. it takes the biggest unit.
     * @return a string of the users account age.
     */
    public String formatAccountAge() {
        if (createdTimestamp == null) {
            return "Unknown";
        }
        LocalDate now = LocalDate.now();
        Period period = Period.between(createdTimestamp, now);
        if (period.getYears() > 0) {
            return period.getYears() + (period.getYears() == 1 ? " year" : " years");
        } else if (period.getMonths() > 0) {
            return period.getMonths() + (period.getMonths() == 1 ? " month" : " months");
        } else {
            return period.getDays() + (period.getDays() == 1 ? " day" : " days");
        }
    }

    public LocalDate getAccountAge() {
        return createdTimestamp;
    }
}
