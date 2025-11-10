package nz.ac.canterbury.seng302.homehelper.repository;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.RecentRenovation;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecentRenovationRepository extends CrudRepository<RecentRenovation, Long> {
    List<RecentRenovation> findAll();

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM recent_renovation " +
                    "WHERE renovation_record_id = :RecordId", nativeQuery = true)
    void deleteByRecordId(@Param("RecordId") Long Id);
}
