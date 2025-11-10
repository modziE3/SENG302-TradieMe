package nz.ac.canterbury.seng302.homehelper.entity.dto;

public record JobFilter(
        String keywords,
        String jobTypes,
        String city,
        String suburb,
        String startDate,
        String dueDate
) {
}
