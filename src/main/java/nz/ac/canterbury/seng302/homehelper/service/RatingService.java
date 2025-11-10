package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.Rating;
import nz.ac.canterbury.seng302.homehelper.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class for ratings
 */
@Service
public class RatingService {
    private final RatingRepository ratingRepository;

    @Autowired
    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    /**
     * Adds a new rating to database storage
     * @param rating Rating being stored
     */
    public void addRating(Rating rating) {
        ratingRepository.save(rating);
    }
}
