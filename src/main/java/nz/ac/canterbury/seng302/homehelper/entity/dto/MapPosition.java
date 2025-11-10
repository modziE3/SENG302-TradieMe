package nz.ac.canterbury.seng302.homehelper.entity.dto;

/**
 * Simple class representing a location as a Latitude and Longitude par
 */
public class MapPosition {
    public double lat;
    public double lng;

    /**
     * Creates a new position with the provided lat and long
     * @param lat latitude for position
     * @param lng longitude for position
     */
    public MapPosition(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }
}