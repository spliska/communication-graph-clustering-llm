package pliska.communicationgraphclusteringbackend.loader.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pliska.communicationgraphclusteringbackend.db.email.EmailEntity;
import pliska.communicationgraphclusteringbackend.db.email.EmailRepository;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

@Component
public class EmailImporter {

    private static final Logger logger = LoggerFactory.getLogger(EmailImporter.class);

    private final EmailRepository emailRepository;

    public EmailImporter(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    public List<EmailEntity> readEmailsFromFolderWithLimit(Path folderPath, int limit) {
        List<EmailEntity> emailEntities = new ArrayList<>();
        Queue<Path> directoriesToVisit = new LinkedList<>();
        directoriesToVisit.add(folderPath);

        while (!directoriesToVisit.isEmpty() && emailEntities.size() < limit) {
            Path currentDir = directoriesToVisit.poll();
            try (Stream<Path> files = Files.list(currentDir)) {
                files.forEach(file -> {
                    if (Files.isDirectory(file)) {
                        directoriesToVisit.add(file);
                    } else if (isNotHiddenFile(file)) {
                        try {
                            if (emailEntities.size() < limit) {
                                EmailEntity emailEntity = parseFile(file);
                                if (emailEntity != null) {
                                    emailEntities.add(emailEntity);
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Failed to process file: {}. Reason: {}", file, e.getMessage(), e);
                        }
                    }
                });
            } catch (IOException e) {
                logger.error("Failed to list files in directory: {}. Reason: {}", currentDir, e.getMessage(), e);
            }
        }

        return emailEntities;
    }

    public List<EmailEntity> readAllEmailsFromFolder(Path folderPath) {
        List<EmailEntity> emailEntities = new ArrayList<>();
        Queue<Path> directoriesToVisit = new LinkedList<>();
        directoriesToVisit.add(folderPath);

        while (!directoriesToVisit.isEmpty()) {
            Path currentDir = directoriesToVisit.poll();
            try (Stream<Path> files = Files.list(currentDir)) {
                files.forEach(file -> {
                    if (Files.isDirectory(file)) {
                        directoriesToVisit.add(file);
                    } else if (isNotHiddenFile(file)) {
                        try {
                            EmailEntity emailEntity = parseFile(file);
                            if (emailEntity != null) {
                                emailEntities.add(emailEntity);
                            }
                        } catch (Exception e) {
                            logger.error("Failed to process file: {}. Reason: {}", file, e.getMessage(), e);
                        }
                    }
                });
            } catch (IOException e) {
                logger.error("Failed to list files in directory: {}. Reason: {}", currentDir, e.getMessage(), e);
            }
        }

        return emailEntities;
    }

    private boolean isNotHiddenFile(Path file) {
        try {
            return !Files.isHidden(file) && !file.getFileName().toString().startsWith(".");
        } catch (IOException e) {
            logger.warn("Failed to check if file is hidden: {}. Skipping.", file, e);
            return false;
        }
    }

    public EmailEntity parseFile(Path file) throws IOException {
        try {
            String rawContent = Files.readString(file, StandardCharsets.UTF_8);

            int separator = findHeaderBodySeparator(rawContent);
            if (separator < 0) {
                throw new RuntimeException("Invalid email format: No header-body separator found.");
            }

            String headerPart = rawContent.substring(0, separator);
            String bodyPart = rawContent.substring(separator + 1);

            Charset charset = detectCharset(headerPart);
            if (!charset.equals(StandardCharsets.UTF_8)) {
                rawContent = Files.readString(file, charset);

                headerPart = rawContent.substring(0, separator);
                bodyPart = rawContent.substring(separator + 1);
            }

            Map<String, String> headers = parseHeaders(headerPart);

            return buildEmailEntity(headers, bodyPart, file);
        } catch (Exception e) {
            logger.error("Error while parsing email file: {}. Reason: {}", file, e.getMessage(), e);
            throw e;
        }
    }

    private Charset detectCharset(String rawHeaderPart) {
        Map<String, String> headers = parseHeaders(rawHeaderPart);
        String contentType = headers.getOrDefault("Content-Type", "").toLowerCase();

        if (contentType.contains("charset=")) {
            try {
                String charset = contentType.split("charset=")[1].split(";")[0].trim().replaceAll("[\"']", "");
                logger.debug("Detected charset: {}", charset);
                return Charset.forName(charset);
            } catch (Exception e) {
                logger.warn("Invalid charset in Content-Type. Defaulting to UTF-8: {}", contentType);
            }
        }
        return StandardCharsets.UTF_8;
    }

    private Map<String, String> parseHeaders(String headerPart) {
        Map<String, String> headers = new HashMap<>();
        String[] lines = headerPart.split("\r\n");

        for (String line : lines) {
            int separatorIndex = line.indexOf(':');
            if (separatorIndex > 0) {
                String key = line.substring(0, separatorIndex).trim();
                String value = line.substring(separatorIndex + 1).trim();
                headers.put(key, value);
            }
        }
        return headers;
    }

    private int findHeaderBodySeparator(String raw) {
        return raw.indexOf("\r\n\r\n");
    }

    private EmailEntity buildEmailEntity(Map<String, String> headers, String body, Path file) {
        EmailEntity emailEntity = new EmailEntity();

        emailEntity.setMessageId(get(headers, "Message-ID"));
        emailEntity.setFrom(get(headers, "From"));
        emailEntity.setToEmailAddress(get(headers, "To"));
        emailEntity.setSubject(get(headers, "Subject"));

        if (emailEntity.getMessageId() == null) {
            logger.warn("Skipping file {}: Missing 'Message-ID' header.", file);
            return null;
        }

        emailEntity.setBody(body);

        return emailEntity;
    }

    private String get(Map<String, String> headers, String key) {
        return headers.getOrDefault(key, null);
    }
}