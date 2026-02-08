package de.ait.javalessonspro.service;

import de.ait.javalessonspro.enums.CandidateDocType;
import de.ait.javalessonspro.model.CandidateDocumentOs;
import de.ait.javalessonspro.repositories.CandidateDocumentOsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 07.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateDocumentOsService {

    private final CandidateDocumentOsRepository repository;

    @Value("${app.upload.candidate-docs-dir}")
    private String candidateDocsDir;

    @Value("${app.upload.candidate-doc-max-size}")
    private int maxFileSizeMb;

    private String normalizeEmailForPath(String email) {
        return email.replace("@", "_at_").replace(".", "_dot_");
    }

    private final List<String> ALLOWED_TYPES =
            List.of("image/jpeg", "image/png", "application/pdf");


    public CandidateDocumentOs uploadCandidateDocument(String candidateEmail, CandidateDocType docType, MultipartFile file) {

        List<String> errors = new ArrayList<>();
        if (candidateEmail == null || candidateEmail.isBlank()) {
            errors.add("Email is required");
        } else if (!candidateEmail.contains("@")) {
            errors.add("Invalid email format");
        }

        if (file == null || file.isEmpty()) {
            errors.add("File is empty");
        } else {
            if (file.getSize() > maxFileSizeMb * 1024 * 1024) {
                errors.add("File size exceeds " + maxFileSizeMb + "MB");
            }
        }

        if (file.getContentType() == null || !ALLOWED_TYPES.contains(file.getContentType())) {
            errors.add("File type not allowed. Allowed: PDF, JPEG, PNG");
        }

        if (!errors.isEmpty()) {
            String errorMessage = String.join(", ", errors);
            log.warn("Upload rejected. Email: {}, File: {}, Reasons: {}",
                    candidateEmail,
                    file != null ? file.getOriginalFilename() : "null",
                    errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        try {
            Path baseDir = Paths.get(candidateDocsDir);
            String normalizedEmail = normalizeEmailForPath(candidateEmail);
            Path targetDir = baseDir.resolve(normalizedEmail.toLowerCase()).resolve(docType.toString().toLowerCase());

            Files.createDirectories(targetDir);
            log.info("Created directory: {}", targetDir);

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                originalFilename = "unnamed";
            }

            String safeFilename = originalFilename
                    .replaceAll("[^a-zA-Z0-9._-]", "_");

            String storedFilename = UUID.randomUUID() + "_" + safeFilename.toLowerCase();
            Path filePath = targetDir.resolve(storedFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            CandidateDocumentOs documentOs = new CandidateDocumentOs(
                    candidateEmail,
                    docType,
                    file.getContentType(),
                    originalFilename,
                    file.getSize(),
                    filePath.toString(),
                    storedFilename
            );
            CandidateDocumentOs savedDoc = repository.save(documentOs);
            log.info("Candidate document with id {} saved for candidate {}", savedDoc.getId(), candidateEmail);

            return savedDoc;

        } catch (IOException exception) {
            log.error("Error creating directory {}", candidateDocsDir, exception);
            throw new RuntimeException("Error creating directory " + candidateDocsDir, exception);
        } catch (NullPointerException exception) {
            log.error("Error to LowerCase {}", candidateDocsDir, exception);
            throw new RuntimeException("Error creating directory " + candidateDocsDir, exception);
        }
    }

    public List<CandidateDocumentOs> getDocumentsByCandidateEmail(String candidateEmail) {
        return repository.findAllByCandidateEmail(candidateEmail);
    }

    public CandidateDocumentOs getDocumentById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }

    public void deleteDocument(Long id) {
        CandidateDocumentOs document = getDocumentById(id);

        try {
            Path filePath = Paths.get(document.getStoragePath());
            Files.deleteIfExists(filePath);

            repository.deleteById(id);

            log.info("Document deleted. ID: {}", id);

        } catch (IOException e) {
            log.error("Failed to delete file for document ID: {}", id, e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}
