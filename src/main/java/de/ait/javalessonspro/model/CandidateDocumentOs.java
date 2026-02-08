package de.ait.javalessonspro.model;

import de.ait.javalessonspro.enums.CandidateDocType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 07.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@Entity
@Table(name = "candidate_documents_os")
@Getter
@Setter
@NoArgsConstructor
public class CandidateDocumentOs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "c_email", nullable = false)
    private String candidateEmail;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false)
    private CandidateDocType docType;

    @Column(name = "orign_filename", nullable = false)
    private String originalFileName;

    @Column(name = "stored_filename", nullable = false)
    private String storedFileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public CandidateDocumentOs(String candidateEmail,
                               CandidateDocType docType,
                               String contentType,
                               String originalFileName,
                               Long size,
                               String storagePath,
                               String storedFileName) {
        this.candidateEmail = candidateEmail;
        this.docType = docType;
        this.contentType = contentType;
        this.createdAt = LocalDateTime.now();
        this.originalFileName = originalFileName;
        this.size = size;
        this.storagePath = storagePath;
        this.storedFileName = storedFileName;
    }
}
