package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;

/**
 * Entity that reflecting the code that is attached and sent to a users
 * email when the try to reset the password.
 */
@Entity
public class ResetPasswordToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String token;

    /**
     * JPA required no-args constructor
     */
    protected ResetPasswordToken() {}

    /**
     * Constructor for a ResetPasswordToken entity.
     * @param userEmail a string representing a users email address.
     * @param token The token/code that is sent to the users email.
     */
    public ResetPasswordToken(String userEmail, String token) {
        this.userEmail = userEmail;
        this.token = token;
    }

    /**
     * Simple getter for the id for repo storage.
     * @return long id attached to the entity.
     */
    public Long getId() {
        return id;
    }

    /**
     * Simple getter for the users email address.
     * @return String of the users email address.
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Simple getter for the reset password token/code.
     * @return String of code attached to the users email.
     */
    public String getToken() {
        return token;
    }
}
