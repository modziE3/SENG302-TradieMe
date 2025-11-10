package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity class reflecting a room for a renovation record
 */
@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "renovation_record_id")
    private RenovationRecord renovationRecord;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "room_job",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "job_id")
    )
    private List<Job> jobs;

    @Column
    private String imageFilename;

    public Room() {}

    public Room(String name) {
        this.name = name;
        this.jobs = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public String getName() {return name;}

    public RenovationRecord getRenovationRecord() {return renovationRecord;}

    public List<Job> getJobs() {return jobs;}

    public void setId(Long id) {this.id = id;}

    public void setName(String name) {this.name = name;}

    public void setRenovationRecord(RenovationRecord renovationRecord) {this.renovationRecord = renovationRecord;}

    public void setJobs(List<Job> jobs) {this.jobs = jobs;}

    public void addJob(Job job) {this.jobs.add(job);}

    public void removeJob(Job job) {this.jobs.remove(job);}

    public String getImageFilename() {
        return imageFilename;
    }

    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }
}

