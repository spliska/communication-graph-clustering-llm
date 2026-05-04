package pliska.communicationgraphclusteringbackend.db.email;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmailRepository extends JpaRepository<EmailEntity, Long> {

    boolean existsByMessageId(String messageId);

    @Query("SELECT e.messageId FROM EmailEntity e WHERE e.messageId IN :messageIds")
    List<String> findAllMessageIdsByMessageIdIn(@Param("messageIds") List<String> messageIds);

    EmailEntity getEmailEntityById(Long id);

    @Query("SELECT e FROM EmailEntity e WHERE e.from = :senderEmailAddress")
    List<EmailEntity> findAllBySender(@Param("senderEmailAddress") String senderEmailAddress);

    @Query("SELECT e FROM EmailEntity e WHERE e.from = :senderEmailAddress AND e.toEmailAddress = :recipientEmailAddress ORDER BY e.date ASC")
    List<EmailEntity> findAllBySenderAndRecipientOrderByDateAsc(@Param("senderEmailAddress") String senderEmailAddress, @Param("recipientEmailAddress") String recipientEmailAddress);

    @Query("SELECT e FROM EmailEntity e WHERE e.toEmailAddress = :emailAddress")
    List<EmailEntity> findAllByRecipient(@Param("emailAddress") String emailAddress);

    @Query("SELECT COUNT(e) FROM EmailEntity e WHERE e.from = :senderEmail AND e.toEmailAddress LIKE '%,%'")
    Integer countBySenderEmailsWithMoreThanOneRecipient(@Param("senderEmail") String senderEmail);
}