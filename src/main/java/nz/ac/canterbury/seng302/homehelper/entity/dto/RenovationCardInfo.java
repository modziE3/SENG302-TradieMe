package nz.ac.canterbury.seng302.homehelper.entity.dto;

public record RenovationCardInfo (
        String name,
        String description,
        String location,
        String userFullName,
        Long recordId,
        String profilePicture
) {
    public String getName() {
        return name;
    }
    public String getDescription() { return description; }
    public String getUserFullName() { return userFullName; }
    public String getLocation() {
        return location;
    }
    public Long getRecordId() {return recordId;}
    public String getProfilePicture() { return profilePicture; }
}

