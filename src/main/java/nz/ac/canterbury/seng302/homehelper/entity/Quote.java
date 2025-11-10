package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;

/**
 * Entity class reflecting a quote for a job
 */
@Entity
public class Quote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quote_id")
    private Long id;

    @Column(nullable = false)
    private String price;

    @Column(nullable = false)
    private String workTime;

    @Column
    private String email;

    @Column
    private String phoneNumber;

    @Column
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private String status = "Pending";

    @Column
    private Boolean rated;

    public Quote() {}

    public Quote(String price, String workTime, String email, String phoneNumber, String description) {
        this.price = price;
        this.workTime = workTime;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.description = description;
        this.rated = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPrice() {
        return price;
    }

    public String getWorkTime() {
        return workTime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getRated() {
        return rated;
    }
    public void setRated(Boolean rated) {
        this.rated = rated;
    }

    public Long getWorkTimeAsLong() {
        return Long.parseLong(workTime);
    }
    public float getPriceAsFloat() {
        return Float.parseFloat(price);
    }
}
