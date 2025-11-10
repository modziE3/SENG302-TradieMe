package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.Expense;
import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findAll();

    List<Expense> findAllByJobId(Long jobId);

    @Query("SELECT t FROM Expense t WHERE t.job = :job")
    Slice<Expense> findAllFromJob(Job job, Pageable pageable);

    @Query("SELECT e FROM Expense e WHERE e.job.renovationRecord = :record")
    Slice<Expense> findAllFromRecord(RenovationRecord record, Pageable pageable);
}
