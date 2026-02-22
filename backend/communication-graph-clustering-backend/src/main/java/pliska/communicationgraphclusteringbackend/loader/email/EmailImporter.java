package pliska.communicationgraphclusteringbackend.loader.email;

import org.springframework.stereotype.Component;
import pliska.communicationgraphclusteringbackend.db.email.EmailEntity;
import pliska.communicationgraphclusteringbackend.db.email.EmailRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

@Component
public class EmailImporter {

    private final EmailRepository emailRepository;

    public EmailImporter(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    public List<EmailEntity> readAllEmailsFromFolder(Path folderPath) {
        List<EmailEntity> emailEntityList = new ArrayList<EmailEntity>();
        try (Stream<Path> pathsStream = Files.walk(folderPath)) {
            pathsStream.filter(Files::isRegularFile).forEach(
                    path -> {
                        try{
                            EmailEntity emailEntity= null;
                            try {
                                emailEntity = parseFile(path);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            if (emailEntity !=null) emailEntityList.add(emailEntity);

                        } catch (RuntimeException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return emailEntityList;
    }

    public List<EmailEntity> readEmailsFromFolderWithLimit(Path folderPath,int limit) {
        List<EmailEntity> emailEntityList = new ArrayList<EmailEntity>();
        try (Stream<Path> pathsStream = Files.walk(folderPath)) {
            pathsStream.filter(Files::isRegularFile)
                    .limit(limit)
                    .forEach(
                    path -> {
                        try{
                            EmailEntity emailEntity= null;
                            try {
                                emailEntity = parseFile(path);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            if (emailEntity !=null) emailEntityList.add(emailEntity);

                        } catch (RuntimeException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return emailEntityList;
    }

    public EmailEntity parseFile(Path file) throws IOException {
        String raw = Files.readString(file, StandardCharsets.UTF_8);

        int sep = findHeaderBodySeparator(raw);
        String headerPart = (sep >= 0) ? raw.substring(0, sep) : raw;
        String bodyPart = (sep >= 0) ? raw.substring(sep).replaceFirst("^\\R+", "") : "";

        Map<String, String> headers = parseHeaders(headerPart);

        EmailEntity e = new EmailEntity();
        e.setSourcePath(file.toString());
        e.setRawHeaders(headerPart);
        e.setBody(bodyPart);

        e.setMessageId(get(headers, "Message-ID"));
        e.setFrom(get(headers, "From"));
        e.setToEmailAddress(get(headers, "To"));
        e.setCc(get(headers, "Cc"));
        e.setBcc(get(headers, "Bcc"));
        e.setSubject(get(headers, "Subject"));
        e.setMimeVersion(get(headers, "Mime-Version"));
        e.setContentType(get(headers, "Content-Type"));
        e.setContentTransferEncoding(get(headers, "Content-Transfer-Encoding"));

        e.setxFrom(get(headers, "X-From"));
        e.setxTo(get(headers, "X-To"));
        e.setxCc(get(headers, "X-cc"));
        e.setxBcc(get(headers, "X-bcc"));
        e.setxFolder(get(headers, "X-Folder"));
        e.setxOrigin(get(headers, "X-Origin"));
        e.setxFileName(get(headers, "X-FileName"));

        e.setDate(parseDate(get(headers, "Date")));

        return e;
    }

    private int findHeaderBodySeparator(String raw) {
        int i = raw.indexOf("\r\n\r\n");
        if (i >= 0) return i + 4;
        i = raw.indexOf("\n\n");
        if (i >= 0) return i + 2;
        return -1;
    }

    private Map<String, String> parseHeaders(String headerPart) {
        List<String> unfolded = new ArrayList<>();
        String[] lines = headerPart.split("\\R", -1);

        StringBuilder current = new StringBuilder();
        for (String line : lines) {
            if (line.isEmpty()) continue;

            boolean isContinuation = line.startsWith(" ") || line.startsWith("\t");
            if (isContinuation && current.length() > 0) {
                current.append(" ").append(line.trim());
            } else {
                if (current.length() > 0) unfolded.add(current.toString());
                current.setLength(0);
                current.append(line);
            }
        }
        if (current.length() > 0) unfolded.add(current.toString());

        Map<String, String> map = new HashMap<>();
        for (String l : unfolded) {
            int idx = l.indexOf(':');
            if (idx <= 0) continue;
            String key = l.substring(0, idx).trim();
            String val = l.substring(idx + 1).trim();
            map.put(key, val);
        }
        return map;
    }

    private String get(Map<String, String> headers, String key) {
        return headers.getOrDefault(key, null);
    }

    private OffsetDateTime parseDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) return null;

        String cleaned = rawDate.replaceAll("\\s*\\(.*\\)$", "").trim();

        try {

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
            return ZonedDateTime.parse(cleaned, fmt).toOffsetDateTime();
        } catch (Exception ignored) { }

        return null;
    }
}
