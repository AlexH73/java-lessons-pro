package de.ait.javalessonspro.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.javalessonspro.enums.CandidateDocType;
import de.ait.javalessonspro.model.CandidateDocumentOs;
import de.ait.javalessonspro.repositories.CandidateDocumentOsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 08.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 * Integration tests for Candidate Documents API
 * (Object Storage + Database metadata)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Integration tests for Candidate Documents API")
public class CandidateDocumentControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CandidateDocumentOsRepository repository;

    @Value("${app.upload.candidate-docs-dir}")
    private String uploadDir;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_EMAIL = "test.candidate@upload.com";
    private static final CandidateDocType TEST_DOC_TYPE = CandidateDocType.CV;
    private static final String TEST_FILENAME = "test-resume.pdf";
    private static final byte[] TEST_CONTENT = "Test PDF content for candidate document".getBytes();

    /**
     * Before each test:
     * - clean database
     * - clean upload directory
     */
    @BeforeEach
    void setUp() throws IOException {
        repository.deleteAll();
        cleanDirectory(Path.of(uploadDir));
    }

    /**
     * Deletes all files and subdirectories inside the given directory.
     * Creates the directory if it does not exist.
     */
    private void cleanDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            return;
        }

        try (var walk = Files.walk(dir)) {
            walk.sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    // -------------------------------------------------------------------------
    // TEST 1
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Test 1: Upload document → DB record + file on disk")
    void testUploadShouldSaveFileToOsAndSaveMetadataToDb() throws Exception {

        // 1. Prepare multipart file
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                TEST_FILENAME,
                "application/pdf",
                TEST_CONTENT
        );

        // 2. Upload file via API
        String uploadResponse = mockMvc.perform(
                        multipart("/api/candidates/documents/os")
                                .file(multipartFile)
                                .param("candidateEmail", TEST_EMAIL)
                                .param("docType", TEST_DOC_TYPE.name())
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(
                        MockMvcResultMatchers
                        .content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 3. Extract document ID from response
        JsonNode uploadNode = objectMapper.readTree(uploadResponse);
        Long documentId = uploadNode.get("id").asLong();
        assertThat(documentId).isNotNull();

        // 4. Verify record is saved in the database
        CandidateDocumentOs savedDoc =
                repository.findById(documentId).orElseThrow();

        assertThat(repository.count()).isEqualTo(1);
        assertThat(savedDoc.getCandidateEmail()).isEqualTo(TEST_EMAIL);
        assertThat(savedDoc.getDocType()).isEqualTo(TEST_DOC_TYPE);
        assertThat(savedDoc.getOriginalFileName()).isEqualTo(TEST_FILENAME);
        assertThat(savedDoc.getContentType()).isEqualTo("application/pdf");

        // 5. Verify file exists on disk
        Path storedFilePath = Paths.get(savedDoc.getStoragePath());
        assertThat(Files.exists(storedFilePath)).isTrue();

        // 6. Verify file content matches the uploaded content
        byte[] storedFileContent = Files.readAllBytes(storedFilePath);
        assertThat(storedFileContent).isEqualTo(TEST_CONTENT);
        assertThat(savedDoc.getStoragePath()).isNotEmpty();
    }

    // -------------------------------------------------------------------------
    // TEST 2
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Test 2: Complete flow: Upload → List → Download → Delete → Verify removal")
    void testCompleteDocumentFlow() throws Exception {

        // ---------- Step 1: Upload ----------
        MockMultipartFile uploadFile = new MockMultipartFile(
                "file",
                "flow-test.pdf",
                "application/pdf",
                "Flow test content for complete flow".getBytes()
        );

        String uploadResponse = mockMvc.perform(
                        multipart("/api/candidates/documents/os")
                                .file(uploadFile)
                                .param("candidateEmail", TEST_EMAIL)
                                .param("docType", CandidateDocType.CERTIFICATE.name())
                                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CandidateDocumentOs uploadedDocument = objectMapper.readValue(uploadResponse, CandidateDocumentOs.class);
        Long documentId = uploadedDocument.getId();
        Path filePath = Paths.get(uploadedDocument.getStoragePath());

        // ---------- Step 2: List ----------
        String listResponse = mockMvc.perform(
                        get("/api/candidates/documents/os")
                                .param("candidateEmail", TEST_EMAIL))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode listNode = objectMapper.readTree(listResponse);
        assertThat(listNode.size()).isEqualTo(1);
        assertThat(listNode.get(0).get("id").asLong()).isEqualTo(documentId);

        // ---------- Step 3: Download ----------
        byte[] downloadedBytes = mockMvc.perform(
                        get("/api/candidates/documents/os/{id}/download", documentId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("flow-test.pdf")))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertThat(downloadedBytes).isEqualTo("Flow test content for complete flow".getBytes());

        // ---------- Step 4: Verify file exists ----------
        assertThat(Files.exists(filePath)).isTrue();

        // ---------- Step 5: Delete ----------
        mockMvc.perform(delete("/api/candidates/documents/os/{documentId}", documentId))
                .andExpect(status().isNoContent());

        // ---------- Step 6: Verify deletion ----------
        assertThat(repository.findById(documentId)).isEmpty();
        assertThat(Files.exists(filePath)).isFalse();
    }

}
