package nz.ac.canterbury.seng302.homehelper.entity;


import jakarta.persistence.*;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * Entity class reflecting a job of a renovation record
 */
@Entity
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 1024) // Was 512, but needed to be larger to allow emojis
    private String description;

    @Column
    private String dueDate;

    @Column
    private String startDate;

    @ManyToMany(mappedBy = "jobs", fetch = FetchType.EAGER)
    private List<Room> rooms = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "renovation_record_id")
    private RenovationRecord renovationRecord;

    @Column
    private String icon = null;

    @Column
    private String status = "Not Started";

    @Column
    private String type = "No Type";

    @Column
    private boolean isPosted = false;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "job_id")
    private List<Expense> expenses = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "job_id")
    private List<Quote> quotes = new ArrayList<>();

    @Column
    private LocalDateTime lastUpdated = LocalDateTime.now();

    @ManyToMany(mappedBy = "portfolioJobs", fetch = FetchType.EAGER)
    private List<User> portfolioUsers = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "job_image_filenames", joinColumns = @JoinColumn(name = "job_id"))
    @Column
    private List<String> imageFilenames = new ArrayList<>();

    @Column
    private LocalDate completedTimestamp;

    /**
     * JPA required no-args constructor
     */
    public Job() {}


    /**
     * Creates a new Job object
     * @param name name of the job
     * @param description description of the job
     * @param dueDate date when the job should be done
     */
    public Job(String name, String description, String dueDate, String startDate) {
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.startDate = startDate;
    }

    public Long getId() {return id;}

    public String getName() {return name;}

    public String getDescription() {return description;}

    public String getDueDate() {return dueDate;}

    public String getStartDate() {return startDate;}

    public List<Room> getRooms() {return rooms;}

    public RenovationRecord getRenovationRecord() {return renovationRecord;}

    public void setId(Long id) {this.id = id;}

    public void setName(String name) {this.name = name;}

    public void setDescription(String description) {this.description = description;}

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setStartDate(String startDate) {this.startDate = startDate;}

    public void setRooms(List<Room> rooms) {this.rooms = rooms;}

    public void addRoom(Room room) {this.rooms.add(room);}

    public void removeRoom(Room room) {this.rooms.remove(room);}

    public void setRenovationRecord(RenovationRecord renovationRecord) {this.renovationRecord = renovationRecord;}

    public String getIcon() { return icon; }

    public void setIcon(String icon) { this.icon = icon; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
     }

    public boolean getIsPosted() {
        return isPosted;
    }

    public void setIsPosted(boolean isPosted) {
        this.isPosted = isPosted;
    }

    public List<Expense> getExpenses() {return expenses;}

    public void setExpenses(List<Expense> expenses) {this.expenses = expenses;}

    public void addExpense(Expense expense) {this.expenses.add(expense);}

    public void removeExpense(Expense expense) {this.expenses.remove(expense);}

    public List<Quote> getQuotes() {return quotes;}

    public void setQuotes(List<Quote> quotes) {this.quotes = quotes;}

    public LocalDateTime getLastUpdated() {return lastUpdated;}

    public void setLastUpdated(LocalDateTime lastUpdated) {this.lastUpdated = lastUpdated;}

    public List<User> getPortfolioUsers() {
        return portfolioUsers;
    }

    public void setPortfolioUsers(List<User> portfolioUsers) {
        this.portfolioUsers = portfolioUsers;
    }

    public void addPortfolioUser(User user) {
        this.portfolioUsers.add(user);
    }

    public void removePortfolioUser(User user) {
        this.portfolioUsers.remove(user);
    }

    public List<String> getImageFilenames() {return imageFilenames;}

    public void setImageFilenames(List<String> imageFilenames) {this.imageFilenames = imageFilenames;}

    public void addImageFilename(String imageFilename) {this.imageFilenames.add(imageFilename);}

    public void removeImageFilename(String imageFilename) {this.imageFilenames.remove(imageFilename);}

    public LocalDate getCompletedTimestamp() {
        return completedTimestamp;
    }

    public void setCompletedTimestamp(LocalDate completedTimestamp) {
        this.completedTimestamp = completedTimestamp;
    }
}
