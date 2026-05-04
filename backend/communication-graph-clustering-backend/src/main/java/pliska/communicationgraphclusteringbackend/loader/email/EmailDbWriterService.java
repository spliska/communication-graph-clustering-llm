package pliska.communicationgraphclusteringbackend.loader.email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pliska.communicationgraphclusteringbackend.db.email.EmailEntity;
import pliska.communicationgraphclusteringbackend.db.email.EmailRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailDbWriterService {
    private static final Logger logger = LoggerFactory.getLogger(EmailDbWriterService.class);
    private final EmailRepository repository;

    public EmailDbWriterService(EmailRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void saveEmailsIntoDb(List<EmailEntity> emailList) {
        if (emailList == null || emailList.isEmpty()) {
            logger.warn("Empty or null email list provided. Skipping save operation.");
            return;
        }

        logger.info("Starting to save {} emails into the database.", emailList.size());

        try {

            List<String> messageIds = emailList.stream()
                    .map(EmailEntity::getMessageId)
                    .filter(messageId -> messageId != null && !messageId.isBlank())
                    .collect(Collectors.toList());

            List<String> existingMessageIds = repository.findAllMessageIdsByMessageIdIn(messageIds);

            List<EmailEntity> newEmails = emailList.stream()
                    .filter(email -> email.getMessageId() == null || !existingMessageIds.contains(email.getMessageId()))
                    .collect(Collectors.toList());

            if (!newEmails.isEmpty()) {
                repository.saveAll(newEmails);
                logger.info("Successfully saved {} new emails into the database.", newEmails.size());
            } else {
                logger.info("No new emails to save. All messageIds already exist in the database.");
            }
        } catch (Exception e) {
            logger.error("An error occurred while saving emails into the database.", e);
            throw e;
        }
    }
}
