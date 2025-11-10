package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Job repository accessor using Spring's @link{CrudRepository}.
 */
@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findAll();

    @Query("SELECT COUNT(t.id) from Job t WHERE t.renovationRecord = :renovationRecordId")
    long countJobsForRenovation(RenovationRecord renovationRecordId);

    @Query("SELECT t FROM Job t WHERE t.renovationRecord = :renovationRecord")
    Slice<Job> findAllFromRenovationRecord(RenovationRecord renovationRecord, Pageable pageable);

    @Query("SELECT t FROM Job t WHERE t.renovationRecord = :renovationRecord AND t.status = :filter")
    Slice<Job> findAllFromRenovationRecordFiltered(RenovationRecord renovationRecord, Pageable pageable, String filter);

    @Query("SELECT j from Job j WHERE j.isPosted = true")
    List<Job> findPostedJobs();
}
