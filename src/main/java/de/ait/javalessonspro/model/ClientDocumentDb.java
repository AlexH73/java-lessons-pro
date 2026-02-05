package de.ait.javalessonspro.model;

import de.ait.javalessonspro.enums.ClientDocumentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "client_documents_db")
@Getter
@Setter
@NoArgsConstructor
public class ClientDocumentDb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (name = "client_email", nullable = false)
    private String clientEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false)
    private ClientDocumentType docType;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Column(name = "original_filename", nullable = false)
    private String originalFileName;

    //@Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "data", nullable = false)
    private byte[] data;

    public ClientDocumentDb(String clientEmail,
                            ClientDocumentType docType,
                            String contentType,
                            Long size,
                            String originalFileName,
                            byte[] data) {
        this.clientEmail = clientEmail;
        this.docType = docType;
        this.contentType = contentType;
        this.size = size;
        this.originalFileName = originalFileName;
        this.data = data;
    }
}
