package nz.ac.canterbury.seng302.homehelper.repository;

import nz.ac.canterbury.seng302.homehelper.entity.Job;
import nz.ac.canterbury.seng302.homehelper.entity.Quote;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for quotes
 */
@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findAllByDescription(String description);
    List<Quote> findAllByUserId(Long userId);
    List<Quote> findAllByJobId(Long jobId);
    List<Quote> findAllByUser(User user);
    /**
     * Gets the quotes sent by a user, filtered with a given status. If status is null then the
     * filter is ignored.
     * @param userEmail the email of the user that sent the quotes.
     * @param status the status used to filter the quotes
     * @return a list of quotes sent by a user filtered with a given status.
     */
    @Query("SELECT q FROM Quote q WHERE q.user.email = :userEmail AND (:status is NULL OR LOWER(q.status) = :status)")
    List<Quote> findSentQuotes(String userEmail, String status);


    /**
     * Gets the received quotes of jobs owned by a given user. The quotes are filtered to a
     * status. If the status is null then the filter is ignored.
     * @param userEmail the email of the owner of the jobs for which quotes have been sent to.
     * @param status the status used to filter the quotes.
     * @return a list of quotes received by a user filtered with a given status.
     */
    @Query("SELECT q FROM Quote q WHERE q.job.renovationRecord.userEmail = :userEmail AND (:status is NULL OR LOWER(q.status) = :status)")
    List<Quote> findReceivedQuotes(String userEmail, String status);
    void deleteAll();

    /**
     * Gets the quote by user email and quote id and filters by status
     * @param userEmail the email of the owner of the jobs for which quotes have been sent to.
     * @param status the status used to filter the quotes.
     * @param id the id of the quote
     * @return a quote which matches given id
     */
    @Query("SELECT q FROM Quote q WHERE q.user.email = :userEmail AND (:status is NULL OR LOWER(q.status) = :status) AND q.id = :id")
    Quote findQuoteById(String userEmail, String status, Long id);

    @Query("SELECT q from Quote q WHERE q.user.email = :userEmail AND (:status is NULL OR LOWER(q.status) = LOWER(:status)) AND q.job.id = :jobId")
    List<Quote> findQuotesForJob(String userEmail, String status, Long jobId);

    /**
     * finds the completed jobs from quote that were accepted and completed
     * @param id the quotes id
     * @return a list of jobs that will either have nothing in it or have a job in it if that job was completed and this
     * quote was accepted
     */
    @Query("SELECT q.job FROM Quote q WHERE q.id = :id AND q.status = 'Accepted' AND q.job.status = 'Completed'")
    List<Job> findCompletedJobs(Long id);

    /**
     * Manually queries database for quote and deletes it if matches id
     * @param id id of quote
     */
    @Modifying
    @Query ("DELETE FROM Quote q WHERE q.id = :id")
    void deleteQuoteById(Long id);

    @Query(value = "SELECT * FROM quote WHERE job_id = :jobId AND status = :status", nativeQuery = true
    )
    List<Quote> findAllByJobIdAndStatus(Long jobId, String status);
    @Query(value = "SELECT * from quote where job_id = :jobId and status = 'Accepted'", nativeQuery = true)
    List<Quote> findAllByJobIdAndAcceptedStatus(Long jobId);

    @Query(value = "select * from quote where job_id = :jobId and user_id = :tradieId", nativeQuery = true)
    List<Quote> findAllByJobIdAndUserId(Long jobId, Long tradieId);

    Long job(Job job);
}
