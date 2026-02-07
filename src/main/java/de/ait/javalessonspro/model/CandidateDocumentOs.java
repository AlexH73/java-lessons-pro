package de.ait.javalessonspro.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 07.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@Entity
@Table(name = "candidate_documents")
@NoArgsConstructor
@Getter
@Setter
public class CandidateDocumentOs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "c_email", nullable = false, unique = true)
    private String candidateEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false)
    private String docType;

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

}
