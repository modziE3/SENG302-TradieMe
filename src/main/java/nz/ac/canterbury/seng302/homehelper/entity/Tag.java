package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity class reflecting a tag for a renovation record
 */
@Entity
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @Column(nullable = false, length = 60)
    private String name;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.EAGER)
    private List<RenovationRecord> renovationRecords = new ArrayList<>();

    public Tag() {}

    public Tag(String name) {this.name = name;}

    public Long getId() {return id;}

    public String getName() {return name;}

    public List<RenovationRecord> getRenovationRecords() {
        return renovationRecords;
    }

    public void setRenovationRecords(List<RenovationRecord> renovationRecords) {
        this.renovationRecords = renovationRecords;
    }

    public void addRenovationRecord(RenovationRecord renovationRecord) {
        this.renovationRecords.add(renovationRecord);
    }
}
