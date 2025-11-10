package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class reflecting a renovation record
 */
@Entity
public class RenovationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "renovation_record_id")
    private Long id;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false, length = 1024) // Was 512, but needed to be larger to allow emojis
    private String description;

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

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "renovation_record_id")
    private List<Room> rooms = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "renovation_record_id")
    private List<Job> jobs = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "renovation_record_tag",
            joinColumns = @JoinColumn(name = "renovation_record_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();

    @Column(nullable = false)
    private boolean isPublic = false;

    @Column
    private LocalDateTime createdTimestamp;

    @OneToMany(mappedBy = "renovationRecord", fetch = FetchType.EAGER)
    private List<RecentRenovation> recentRenovations = new ArrayList<>();

    /**
     * JPA required no-args constructor
     */
    public RenovationRecord() {}

    /**
     * Creates a new RenovationRecord object
     * @param name name of the renovation record
     * @param description description of the renovation in the record
     */
    public RenovationRecord(String name, String description, List<Room> rooms, String userEmail) {
        this.name = name;
        this.description = description;
        this.rooms = rooms;
        this.userEmail = userEmail;
        this.createdTimestamp = LocalDateTime.now();
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUserEmail() {
        return this.userEmail;
    }

    public String getDescription() {
        return description;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setPublic(){
        this.isPublic = true;
    }

    public boolean getIsPublic() {
        return isPublic;
    }

    public void setId(Long id) {this.id = id;}

    public void setName(String name) {
        this.name = name;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    public String getSuburb() {
        return suburb;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Boolean authenticatePage(long id, String userEmail) {
        return this.userEmail.equals(userEmail) && this.id == id;
    }

    public List<Tag> getTags() {return tags;}

    public void addTag(Tag tag) {this.tags.add(tag);}

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
}
