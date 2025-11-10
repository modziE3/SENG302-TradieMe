package nz.ac.canterbury.seng302.homehelper.entity.dto;

/**
 * DTO class for tradie ratings
 */
public class TradieRating {

    private Long tradieId;
    private int rating;

    public Long getTradieId() { return tradieId; }

    public int getRating() { return rating; }

    public void setRating(int rating) { this.rating = rating; }

    public void setTradieId(Long tradieId) { this.tradieId = tradieId; }
}
