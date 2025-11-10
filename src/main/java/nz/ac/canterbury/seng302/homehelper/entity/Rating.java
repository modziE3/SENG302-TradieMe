package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;

/**
 * Entity class reflecting a rating for a tradie user
 */
@Entity
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rating_id")
    private Long id;

    @Column
    private int rating;

    @JoinColumn(name = "receiving_user_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private User receivingUser;

    @JoinColumn(name = "sending_user_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private User sendingUser;

    public Rating() {}

    public Rating(int rating, User receivingUser, User sendingUser) {
        this.rating = rating;
        this.receivingUser = receivingUser;
        this.sendingUser = sendingUser;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public User getReceivingUser() {
        return receivingUser;
    }

    public void setReceivingUser(User receivingUser) {
        this.receivingUser = receivingUser;
    }

    public User getSendingUser() {
        return sendingUser;
    }

    public void setSendingUser(User sendingUser) {
        this.sendingUser = sendingUser;
    }
}
