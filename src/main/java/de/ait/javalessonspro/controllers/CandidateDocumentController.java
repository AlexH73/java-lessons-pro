package de.ait.javalessonspro.controllers;

import de.ait.javalessonspro.enums.CandidateDocType;
import de.ait.javalessonspro.model.CandidateDocumentOs;
import de.ait.javalessonspro.service.CandidateDocumentOsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
public class CandidateDocumentController {
    private final CandidateDocumentOsService service;

    @PostMapping(value = "/documents/os", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CandidateDocumentOs> uploadCandidateDocument(
            @RequestParam String candidateEmail,
            @RequestParam CandidateDocType docType,
            @RequestParam("file") MultipartFile file) {

        if (file.getOriginalFilename() == null || file.getOriginalFilename().contains("..")) {
            return ResponseEntity.badRequest().build();
        }

        CandidateDocumentOs saved = service.uploadCandidateDocument(candidateEmail, docType, file);
        log.info("Candidate document with id {} saved for candidate {}", saved.getId(), candidateEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
