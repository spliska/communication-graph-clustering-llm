package pliska.communicationgraphclusteringbackend.db.email;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "email")
public class EmailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 1024)
    private String sourcePath;

    @Column(length = 512)
    private String messageId;

    private OffsetDateTime date;

    @Column(name = "fromEmailAddress", length = 512)
    private String from;

    @Lob
    private String toEmailAddress;

    @Lob
    private String cc;

    @Lob
    private String bcc;

    @Lob
    private String subject;

    @Column(length = 64)
    private String mimeVersion;

    @Lob
    private String contentType;

    @Column(length = 128)
    private String contentTransferEncoding;

    @Lob
    private String xFrom;

    @Lob
    private String xTo;

    @Lob
    private String xCc;

    @Lob
    private String xBcc;

    @Lob
    private String xFolder;

    @Column(length = 256)
    private String xOrigin;

    @Column(length = 512)
    private String xFileName;

    @Lob
    private String rawHeaders;

    @Lob
    private String body;

    public Long getId() {
        return id;
    }

    public EmailEntity(Long id, String sourcePath, String messageId, OffsetDateTime date, String from, String toEmailAddress, String cc, String bcc, String subject, String mimeVersion, String contentType, String contentTransferEncoding, String xFrom, String xTo, String xCc, String xBcc, String xFolder, String xOrigin, String xFileName, String rawHeaders, String body) {
        this.id = id;
        this.sourcePath = sourcePath;
        this.messageId = messageId;
        this.date = date;
        this.from = from;
        this.toEmailAddress = toEmailAddress;
        this.cc = cc;
        this.bcc = bcc;
        this.subject = subject;
        this.mimeVersion = mimeVersion;
        this.contentType = contentType;
        this.contentTransferEncoding = contentTransferEncoding;
        this.xFrom = xFrom;
        this.xTo = xTo;
        this.xCc = xCc;
        this.xBcc = xBcc;
        this.xFolder = xFolder;
        this.xOrigin = xOrigin;
        this.xFileName = xFileName;
        this.rawHeaders = rawHeaders;
        this.body = body;
    }

    public EmailEntity() {

    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getToEmailAddress() {
        return toEmailAddress;
    }

    public void setToEmailAddress(String toAddr) {
        this.toEmailAddress = toAddr;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMimeVersion() {
        return mimeVersion;
    }

    public void setMimeVersion(String mimeVersion) {
        this.mimeVersion = mimeVersion;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentTransferEncoding() {
        return contentTransferEncoding;
    }

    public void setContentTransferEncoding(String contentTransferEncoding) {
        this.contentTransferEncoding = contentTransferEncoding;
    }

    public String getxFrom() {
        return xFrom;
    }

    public void setxFrom(String xFrom) {
        this.xFrom = xFrom;
    }

    public String getxTo() {
        return xTo;
    }

    public void setxTo(String xTo) {
        this.xTo = xTo;
    }

    public String getxCc() {
        return xCc;
    }

    public void setxCc(String xCc) {
        this.xCc = xCc;
    }

    public String getxBcc() {
        return xBcc;
    }

    public void setxBcc(String xBcc) {
        this.xBcc = xBcc;
    }

    public String getxFolder() {
        return xFolder;
    }

    public void setxFolder(String xFolder) {
        this.xFolder = xFolder;
    }

    public String getxOrigin() {
        return xOrigin;
    }

    public void setxOrigin(String xOrigin) {
        this.xOrigin = xOrigin;
    }

    public String getxFileName() {
        return xFileName;
    }

    public void setxFileName(String xFileName) {
        this.xFileName = xFileName;
    }

    public String getRawHeaders() {
        return rawHeaders;
    }

    public void setRawHeaders(String rawHeaders) {
        this.rawHeaders = rawHeaders;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
