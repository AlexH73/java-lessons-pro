package de.ait.javalessonspro.service;

import de.ait.javalessonspro.repositories.CarDocumentOsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    private final CarDocumentOsRepository carDocumentOsRepository;

    @Value("${app.upload.candidate-docs-dir}")
    private String candidateDocsDir;

    @Value("${app.upload.candidate-doc-max-size}")
    private int getMaxFileSize;

    private final int MAX_FILE_SIZE = getMaxFileSize  * 1024 * 1024; // 5 MB

    private String normalizeEmailForPath(String email) {
        return email.replace("@", "_at_").replace(".", "_dot_");
    }

    private final List<String> ALLOWED_TYPES =
                List.of("image/jpeg", "image/png", "application/pdf");

public uploadCandidateDocument(String candidateEmail, String docType, MultipartFile file) {

    if (candidateEmail == null || candidateEmail.isBlank() || !candidateEmail.contains("@")) {
        log.error("Invalid candidate email: {}", candidateEmail);
        throw new IllegalArgumentException("Invalid candidate email");
    }

    if (file == null || file.isEmpty()) {
        log.error("File is null or empty");
        throw new IllegalArgumentException("File is empty");
    }

    if (file.getSize() > MAX_FILE_SIZE) {
        log.error("The file is too large: {} bytes", file.getSize());
        throw new IllegalArgumentException("The file should not exceed " + MAX_FILE_SIZE + " MB");
    }

    if (!ALLOWED_TYPES.contains(file.getContentType())) {
        log.error("Unsupported file type: {}", file.getContentType());
        throw new IllegalArgumentException("Only JPG, PNG, and PDF files are allowed");
    }

        return null; // Placeholder return statement
    }

}
