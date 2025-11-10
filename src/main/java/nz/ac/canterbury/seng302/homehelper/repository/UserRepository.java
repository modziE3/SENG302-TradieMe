package nz.ac.canterbury.seng302.homehelper.repository;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    List<User> findAll();

    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findUserByEmail(@Param("email") String email);

    @Query("SELECT u.password FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    String findPasswordByEmailIgnoreCase(@Param("email")String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    User findByEmailContainingIgnoreCase(@Param("email") String email);

    @Query("SELECT u.verificationCode FROM User u WHERE u.verificationCode = :verificationCode")
    String findVerificationCodeByVerificationCode(@Param("verificationCode") String verificationCode);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.password =:newHashedPassword WHERE u.email = :userEmail")
    void updatePasswordWithUserEmail(@Param("userEmail") String userEmail, @Param("newHashedPassword") String newHashedPassword);

    @Query("SELECT u FROM User u WHERE u.id = :id")
    User findUserById(@Param("id") Long id);

    @Query("SELECT u FROM User u WHERE size(u.quotes) > 0")
    List<User> findAllQuoteSenders();

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.email = :newEmail, u.firstName = :newFirstName, u.lastName = :newLastName, " +
            "u.profilePictureFilename = :newProfileImage WHERE u.id = :userId")
    void updateDetails(@Param("userId") Long userId,
                       @Param("newEmail") String newEmail,
                       @Param("newFirstName") String newFirstName,
                       @Param("newLastName") String newLastName,
                       @Param("newProfileImage") String newProfileImage);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.streetAddress = :newStreetAddress, u.suburb = :newSuburb, u.city = :newCity, " +
            "u.postcode = :newPostcode, u.country = :newCountry WHERE u.id = :userId")
    void updateLocation(@Param("userId") Long userId,
                        @Param("newStreetAddress") String newStreetAddress,
                        @Param("newSuburb") String newSuburb,
                        @Param("newCity") String newCity,
                        @Param("newPostcode") String newPostcode,
                        @Param("newCountry") String newCountry);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.latitude = :latitude, u.longitude = :longitude WHERE u.id = :userId")
    void updateCoordinates(Long userId, float latitude, float longitude);


}
