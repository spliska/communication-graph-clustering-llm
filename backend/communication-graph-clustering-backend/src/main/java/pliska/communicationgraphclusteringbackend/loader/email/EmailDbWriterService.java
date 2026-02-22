package pliska.communicationgraphclusteringbackend.loader.email;

import org.springframework.stereotype.Service;
import pliska.communicationgraphclusteringbackend.db.email.EmailEntity;
import pliska.communicationgraphclusteringbackend.db.email.EmailRepository;

import java.util.List;

@Service
public class EmailDbWriterService {
   private final EmailRepository repository;


    public EmailDbWriterService(EmailRepository repository) {
        this.repository = repository;
    }

    public void saveEmailsIntoDb(List<EmailEntity> emailList){
        for (EmailEntity email:emailList){
            if(email.getMessageId()!=null && repository.existsByMessageId(email.getMessageId()))
                continue;
            repository.save(email);
        }
    }
}
