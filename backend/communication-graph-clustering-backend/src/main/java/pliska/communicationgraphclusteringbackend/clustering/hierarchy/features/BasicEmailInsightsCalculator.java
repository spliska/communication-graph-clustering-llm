package pliska.communicationgraphclusteringbackend.clustering.hierarchy.features;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pliska.communicationgraphclusteringbackend.db.email.EmailRepository;

@Service
public class BasicEmailInsightsCalculator {
    @Autowired
    private  EmailRepository emailRepository;
    public int numberOfEmailsSent(String sender){
        return emailRepository.findAllBySender(sender).size();
    }

    public int numberOfEmailsReceived(String recipient){
        return emailRepository.findAllByRecipient(recipient).size();
    }

    public double ratioOfMultiRecipientEmails(String sender){
        return emailRepository.countBySenderEmailsWithMoreThanOneRecipient(sender)/numberOfEmailsSent(sender);
    }




}
