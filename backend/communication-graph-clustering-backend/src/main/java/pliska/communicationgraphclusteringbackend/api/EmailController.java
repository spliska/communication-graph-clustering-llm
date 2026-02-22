package pliska.communicationgraphclusteringbackend.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import pliska.communicationgraphclusteringbackend.db.email.EmailEntity;
import pliska.communicationgraphclusteringbackend.db.email.EmailRepository;

@RestController
@RequestMapping("/api/emails")
public class EmailController {
    private final EmailImportService importService;
    private final EmailRepository emailRepository;

    public EmailController(EmailImportService importService, EmailRepository emailRepository) {
        this.importService = importService;
        this.emailRepository = emailRepository;
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



}
