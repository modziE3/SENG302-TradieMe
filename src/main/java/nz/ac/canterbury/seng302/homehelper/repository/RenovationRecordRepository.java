package nz.ac.canterbury.seng302.homehelper.repository;

import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Renovation record repository accessor using Spring's @link{CrudRepository}.
 */
@Repository
public interface RenovationRecordRepository extends CrudRepository<RenovationRecord, Long> {

    List<RenovationRecord> findAll();

    void deleteById(Long id);

    Optional<RenovationRecord> findByName(String name);

    @Query("SELECT record FROM RenovationRecord record WHERE record.userEmail = :email")
    List<RenovationRecord> findRenovationRecordsByEmail(@Param("email") String email);


    @Transactional
    @Query(value = "SELECT r FROM RenovationRecord r " +
            "WHERE (r.isPublic = TRUE OR r.userEmail = :currentUser) " +
//            "WHERE (r.userEmail = :currentUser) " +
            "AND (upper(r.name) LIKE upper(concat('%', :searchString, '%')) " +
            "OR upper(r.description) LIKE upper(concat('%', :searchString, '%')) )")
    List<RenovationRecord> findMatchingRenovationRecordsBySearchString(@Param("searchString") String searchString, @Param("currentUser") String currentUser);

    @Transactional
    @Query(value = "SELECT DISTINCT * FROM renovation_record " +
            "WHERE (renovation_record.IS_PUBLIC = TRUE OR renovation_record.USER_EMAIL = :currentUser) " +
            "AND ((upper(renovation_record.NAME) LIKE upper(concat('%', :searchString, '%')) " +
            "OR upper(renovation_record.DESCRIPTION) LIKE upper(concat('%', :searchString, '%')) )" +
            "AND renovation_record.RENOVATION_RECORD_ID IN (SELECT RENOVATION_RECORD_ID " +
            "FROM renovation_record_tag join tag on renovation_record_tag.TAG_ID = tag.TAG_ID " +
            "Where tag.NAME in :searchTags " +
            "group by RENOVATION_RECORD_ID " +
            "having count(renovation_record_id) = :#{#searchTags.size()} ))", nativeQuery = true)
    List<RenovationRecord> findMatchingRecordsByStringAndTags(@Param("searchString") String searchString, @Param("currentUser") String currentUser, @Param("searchTags") List<String> searchTags);

    @Transactional
    @Query(value = "SELECT DISTINCT * FROM renovation_record " +
            "WHERE (renovation_record.IS_PUBLIC = TRUE OR renovation_record.USER_EMAIL = :currentUser) " +
            "AND ((upper(renovation_record.NAME) LIKE null " +
            "OR upper(renovation_record.DESCRIPTION) LIKE null) " +
            "OR renovation_record.RENOVATION_RECORD_ID IN (SELECT RENOVATION_RECORD_ID " +
            "FROM renovation_record_tag join tag on renovation_record_tag.TAG_ID = tag.TAG_ID " +
            "Where tag.NAME in :searchTags " +
            "group by RENOVATION_RECORD_ID " +
            "having count(renovation_record_id) = :#{#searchTags.size()} ))", nativeQuery = true)
    List<RenovationRecord> findMatchingRecordsByEmptyStringAndTags(@Param("currentUser") String currentUser, @Param("searchTags") List<String> searchTags);

    @Query(value = "SELECT DISTINCT city, suburb FROM renovation_record WHERE city IS NOT NULL", nativeQuery = true)
    List<Tuple> findCitySuburbs();

    @Transactional
    @Modifying
    @Query("UPDATE RenovationRecord r SET r.latitude = :latitude, r.longitude = :longitude WHERE r.id = :recordId")
    void updateCoordinates(Long recordId, float latitude, float longitude);

}