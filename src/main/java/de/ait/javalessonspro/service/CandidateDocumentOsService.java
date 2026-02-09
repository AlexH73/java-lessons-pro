package de.ait.javalessonspro.service;

import de.ait.javalessonspro.enums.CandidateDocType;
import de.ait.javalessonspro.model.CandidateDocumentOs;
import de.ait.javalessonspro.repositories.CandidateDocumentOsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Set;
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

    @Value("${app.upload.candidate-doc-max-count}")
    private int maxDocumentsPerCandidate;

    private String normalizeEmailForPath(String email) {
        return email.replace("@", "_at_").replace(".", "_dot_");
    }

    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "application/pdf");

    private Path getCandidateDocsRoot() {
        return Paths.get(candidateDocsDir).toAbsolutePath().normalize();
    }


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

        long existingDocsCount =
                repository.countByCandidateEmail(candidateEmail);

        if (existingDocsCount >= maxDocumentsPerCandidate) {
            log.warn(
                    "Upload rejected. Candidate {} already has {} documents (limit = {})",
                    candidateEmail,
                    existingDocsCount,
                    maxDocumentsPerCandidate
            );

            throw new IllegalStateException(
                    "Maximum number of documents (" +
                            maxDocumentsPerCandidate +
                            ") exceeded for candidate"
            );
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank() || originalFilename.contains("..")) {
            log.warn("Rejected candidate upload: email={}, docType={}, filename={}, reason={}",
                    candidateEmail, docType, originalFilename, "Filename is invalid");
            throw new IllegalArgumentException("Filename is invalid (null, empty, or contains '..')");
        }

        try {
            Path baseDir = Paths.get(candidateDocsDir);
            String normalizedEmail = normalizeEmailForPath(candidateEmail);
            Path targetDir = baseDir.resolve(normalizedEmail.toLowerCase()).resolve(docType.toString().toLowerCase());

            Files.createDirectories(targetDir);
            log.debug("Ensured directory exists: {}", targetDir);

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
                .orElseThrow(() ->
                        new IllegalArgumentException("Document not found. ID=" + id));
    }

    @Transactional
    protected void deleteDocuments(List<CandidateDocumentOs> documents) {

        if (documents == null || documents.isEmpty()) {
            throw new IllegalArgumentException("No documents to delete");
        }

        for (CandidateDocumentOs document : documents) {
            Path filePath = Paths.get(document.getStoragePath());
            try {
                Files.deleteIfExists(filePath);
                log.debug("Deleted file: {}", filePath);
            } catch (IOException e) {
                log.error("Failed to delete file {}", filePath, e);
                throw new RuntimeException("Failed to delete file " + filePath, e);
            }
        }

        // 2. Delete DB records
        repository.deleteAll(documents);
        log.info("Deleted {} document(s) from DB", documents.size());
    }

    public void deleteDocument(Long id) {
        CandidateDocumentOs document = getDocumentById(id);

        deleteDocuments(List.of(document));

        Path documentDir = Paths.get(document.getStoragePath()).getParent();
        try {
            cleanupEmptyDirectories(documentDir);
        } catch (IOException e) {
            log.warn("Directory cleanup failed for {}", documentDir, e);
        }
    }

    public void deleteAllDocumentsByCandidateEmail(String candidateEmail) {

        List<CandidateDocumentOs> documents =
                repository.findAllByCandidateEmail(candidateEmail);

        if (documents.isEmpty()) {
            throw new IllegalArgumentException(
                    "No documents found for candidate: " + candidateEmail);
        }

        deleteDocuments(documents);

        Path candidateDir = Paths.get(documents.get(0).getStoragePath()).getParent();
        try {
            cleanupEmptyDirectories(candidateDir);
        } catch (IOException e) {
            log.warn("Directory cleanup failed for candidate {}", candidateEmail, e);
        }
    }

    private void cleanupEmptyDirectories(Path startDir) throws IOException {

        Path rootDir = getCandidateDocsRoot();
        Path current = startDir.toAbsolutePath().normalize();

        while (current != null && !current.equals(rootDir)) {

            if (!Files.exists(current) || !Files.isDirectory(current)) {
                return;
            }

            try (var stream = Files.list(current)) {
                if (stream.findAny().isPresent()) {
                    return;
                }
            }

            Files.delete(current);
            log.debug("Deleted empty directory: {}", current);

            current = current.getParent();
        }
    }
}
