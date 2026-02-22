package pliska.communicationgraphclusteringbackend.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pliska.communicationgraphclusteringbackend.db.email.EmailEntity;
import pliska.communicationgraphclusteringbackend.loader.email.EmailDbWriterService;
import pliska.communicationgraphclusteringbackend.loader.email.EmailImporter;

import java.nio.file.Path;
import java.util.List;

@Service
public class EmailImportService {
    private EmailImporter emailImporter;
    private EmailDbWriterService emailDbWriterService;
    @Value("${email.import.directory}")
    private String mailDirectoryPath;

    @Autowired
    public EmailImportService(EmailImporter emailImporter, EmailDbWriterService emailDbWriterService) {
        this.emailImporter = emailImporter;
        this.emailDbWriterService = emailDbWriterService;
    }

    public int importEmails(int limit){
        Path emailFolder=Path.of(mailDirectoryPath);
        List<EmailEntity> emails= emailImporter.readEmailsFromFolderWithLimit(emailFolder,limit);
        emailDbWriterService.saveEmailsIntoDb(emails);
        return emails.size();

    }

    public int importAllEmails(){
        Path emailFolder=Path.of(mailDirectoryPath);
        List<EmailEntity> emails= emailImporter.readAllEmailsFromFolder(emailFolder);
        emailDbWriterService.saveEmailsIntoDb(emails);
        return emails.size();

    }
}
