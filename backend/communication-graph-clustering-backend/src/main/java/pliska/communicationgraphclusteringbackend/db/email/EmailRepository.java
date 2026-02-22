package pliska.communicationgraphclusteringbackend.db.email;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepository extends JpaRepository<EmailEntity, Long> {
    boolean existsByMessageId(String messageId);
}
