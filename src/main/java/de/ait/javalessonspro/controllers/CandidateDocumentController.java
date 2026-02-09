package de.ait.javalessonspro.controllers;

import de.ait.javalessonspro.enums.CandidateDocType;
import de.ait.javalessonspro.model.CandidateDocumentOs;
import de.ait.javalessonspro.service.CandidateDocumentOsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 07.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Candidate Documents API",
        description = "API for managing candidate documents (CV, certificates, etc.) " +
                "with files stored in OS file system and metadata in database."
)
public class CandidateDocumentController {
    private final CandidateDocumentOsService service;

    @Operation(
            summary = "Upload candidate document",
            description = """
                    Uploads a candidate document (CV, certificate, etc.) to file system 
                    and saves metadata to database.
                    
                    **Limitations:**
                    - Maximum file size: 5MB
                    - Allowed file types: PDF, JPEG, PNG
                    - Candidate email must be valid
                    """
    )
    @PostMapping(value = "/documents/os",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> uploadCandidateDocument(
            @RequestParam String candidateEmail,
            @RequestParam CandidateDocType docType,
            @RequestParam("file") MultipartFile file) {

        try {
            CandidateDocumentOs saved = service.uploadCandidateDocument(candidateEmail, docType, file);
            log.info("Candidate document with id {} saved for candidate {}", saved.getId(), candidateEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            log.warn("Rejected candidate upload: email={}, docType={}, filename={}, reason={}",
                    candidateEmail, docType.toString(), file.getOriginalFilename(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
            summary = "Get candidate documents list",
            description = "Returns all documents for a candidate by email"
    )
    @GetMapping("/documents/os")
    public ResponseEntity<?> getCandidateDocuments(@RequestParam String candidateEmail) {
        if (candidateEmail == null || candidateEmail.isBlank()) {
            log.warn("Candidate document retrieval failed: email is null or blank");
            return ResponseEntity.badRequest().body("Email is required");
        }
        return ResponseEntity.ok(service.getDocumentsByCandidateEmail(candidateEmail));
    }

    @Operation(
            summary = "Download document",
            description = "Downloads a document by ID. Returns file with Content-Disposition header."
    )
    @GetMapping("/documents/os/{documentId}/download")
    public ResponseEntity<FileSystemResource> downloadCandidateDocument(@PathVariable Long documentId) {
        CandidateDocumentOs document = service.getDocumentById(documentId);
        Path filePath = Paths.get(document.getStoragePath());

        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(filePath);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @Operation(
            summary = "Delete document",
            description = """
                    Deletes a document by ID. Removes both database record and file from file system.
                    """
    )
    @DeleteMapping("/documents/os/{documentId}")
    public ResponseEntity<?> deleteCandidateDocument(@PathVariable Long documentId) {
        try {
            service.deleteDocument(documentId);
            log.info("Candidate document with id {} deleted", documentId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to delete candidate document with id {}: {}", documentId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Delete all candidate documents",
            description = """
                Deletes all documents of a candidate by email.
                Removes both database records and files from file system.
                """
    )
    @DeleteMapping("/documents/os")
    public ResponseEntity<Void> deleteCandidateDocuments(
            @RequestParam String email
    ) {
        try {
            service.deleteAllDocumentsByCandidateEmail(email);
            log.info("All documents deleted for candidate {}", email);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Failed to delete documents for candidate {}: {}", email, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
