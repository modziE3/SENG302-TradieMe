package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class RecentRenovation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "recent_renovation_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "renovation_record_id")
    private RenovationRecord renovationRecord;

    @Column
    private LocalDateTime timestamp;

    // Constructors, getters, setters
    public RecentRenovation() {
    }

    public RecentRenovation(User user, RenovationRecord renovationRecord) {
        this.user = user;
        this.renovationRecord = renovationRecord;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public RenovationRecord getRenovationRecord() {
        return renovationRecord;
    }

    public void setRenovationRecord(RenovationRecord renovationRecord) {
        this.renovationRecord = renovationRecord;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
