package pliska.communicationgraphclusteringbackend.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import pliska.communicationgraphclusteringbackend.ai.openai.EmailAnalysisResult;
import pliska.communicationgraphclusteringbackend.ai.openai.EmailAnalyzerService;
import pliska.communicationgraphclusteringbackend.db.email.EmailEntity;
import pliska.communicationgraphclusteringbackend.db.email.EmailRepository;
import pliska.communicationgraphclusteringbackend.loader.metadata.OrganizationalDataImporter;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/emails")
public class EmailController {
    private final EmailImportService importService;
    private final EmailRepository emailRepository;
    private final EmailAnalyzerService emailAnalyzerService;
    private final OrganizationalDataImporter organizationalDataImporter;

    public EmailController(EmailImportService importService, EmailRepository emailRepository, EmailAnalyzerService emailAnalyzerService, OrganizationalDataImporter organizationalDataImporter) {
        this.importService = importService;
        this.emailRepository = emailRepository;
        this.emailAnalyzerService = emailAnalyzerService;
        this.organizationalDataImporter = organizationalDataImporter;
    }

    @PostMapping("/import")
    public int importEmails(int limit) {
        if (limit < 1) limit = 1;
        if (limit > 500000) limit = 500000;

        return importService.importEmails(limit);
    }

    @PostMapping("/import-all-emails")
    public int importAllEmails() {
        return importService.importAllEmails();
    }

    @GetMapping
    public Page<EmailEntity> list(Pageable pageable) {
        return emailRepository.findAll(pageable);
    }

    @GetMapping("/{id}")
    public EmailEntity getOne(@PathVariable Long id) {
        return emailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Email not found: " + id));
    }

    @GetMapping("/analyze")
    public int analyzeEmail(){
        List<EmailEntity> emails=emailRepository.findAll();
        emailAnalyzerService.analyzeEmailInBatchAndSaveResult(emails);
        return 1;
    }

    @GetMapping("/analyze/by-user/{emailAddress}")
    public Boolean analyzeEmailsOfUser(String emailAddress){
        List<EmailEntity> emails = emailRepository.findAllBySender(emailAddress);
        return emailAnalyzerService.analyzeEmailInBatchAndSaveResult(emails);

    }

    @GetMapping("/analyze/{id}")
    public EmailAnalysisResult analyzeSpecificEmail(@PathVariable Long id){
        EmailEntity email=emailRepository.getEmailEntityById(id);
        return emailAnalyzerService.analyzeEmail(email.getSubject(), email.getBody());
    }

    @GetMapping("/ground-truth/{file}")
    public Boolean importGroundTruth(String file) throws IOException {
       return organizationalDataImporter.importData(file);

    }



}
