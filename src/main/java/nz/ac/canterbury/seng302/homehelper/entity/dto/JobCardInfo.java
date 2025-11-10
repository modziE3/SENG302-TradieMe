package nz.ac.canterbury.seng302.homehelper.entity.dto;

/**
 * Used to pass a job listings info to the template. This is just a compiled info record.
 * This file just contains getters and setters for the job card record.
 * @param title of the job listing
 * @param userFullName of owner of job listing
 * @param startDate of the job
 * @param endDate of the job
 * @param location of the renovation
 * @param jobType of the job
 * @param budget the owner is expecting to spend on the job
 * @param jobId id of the job
 */
public record JobCardInfo(
        String title,
        String userFullName,
        String startDate,
        String endDate,
        String location,
        String jobType,
        String budget,
        String jobState,
        String jobIcon,
        Long jobId,
        String jobImage
) {
    public String getTitle() {
        return title;
    }
    public String getUserFullName() {
        return userFullName;
    }
    public String getStartDate() {
        return startDate;
    }
    public String getEndDate() {
        return endDate;
    }
    public String getLocation() {
        return location;
    }
    public String getBudget() {
        return budget;
    }
    public String getJobType() {
        return jobType;
    }
    public String getJobState() {
        return jobState;
    }
    public String getJobIcon() {return jobIcon;}
    public Long getJobId() {return jobId;}
    public String getJobImage() {return jobImage;}
}
