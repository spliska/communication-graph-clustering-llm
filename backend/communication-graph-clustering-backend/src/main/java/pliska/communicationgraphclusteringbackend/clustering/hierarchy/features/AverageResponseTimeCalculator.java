package pliska.communicationgraphclusteringbackend.clustering.hierarchy.features;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pliska.communicationgraphclusteringbackend.api.GraphDto;
import pliska.communicationgraphclusteringbackend.db.email.EmailEntity;
import pliska.communicationgraphclusteringbackend.db.email.EmailRepository;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.List;

@Service
public class AverageResponseTimeCalculator {
    @Autowired
    private EmailRepository emailRepository;

    public Long calculateAverageResponseTime(String emailAddressSender,String emailAddressResponder){
      List<EmailEntity> sentEmails=emailRepository.findAllBySenderAndRecipientOrderByDateAsc(emailAddressSender,emailAddressResponder);
      List<EmailEntity> responseEmails=emailRepository.findAllBySenderAndRecipientOrderByDateAsc(emailAddressResponder,emailAddressSender);

        if (sentEmails.isEmpty() || responseEmails.isEmpty()) {
            return 0L;
        }

        Iterator<EmailEntity> responseIterator = responseEmails.iterator();
        long totalResponseTimeInSeconds = 0;
        int count = 0;

        EmailEntity currentResponseEmail = null;
        if (responseIterator.hasNext()) {
            currentResponseEmail = responseIterator.next();
        }

        for (EmailEntity sentEmail : sentEmails) {
            OffsetDateTime sentDate = sentEmail.getDate();
            if (sentDate == null) {
                continue;
            }

            while (currentResponseEmail != null && currentResponseEmail.getDate() != null && currentResponseEmail.getDate().isBefore(sentDate)
            ) {
                currentResponseEmail = responseIterator.hasNext() ? responseIterator.next() : null;
            }

            if (currentResponseEmail != null && currentResponseEmail.getDate().isAfter(sentDate)) {
                long responseTime = Duration.between(sentDate, currentResponseEmail.getDate()).toSeconds();
                totalResponseTimeInSeconds += responseTime;

                count++;

                currentResponseEmail = responseIterator.hasNext() ? responseIterator.next() : null;
            }
        }

        return count > 0 ? totalResponseTimeInSeconds / count : 0;
    }
}
