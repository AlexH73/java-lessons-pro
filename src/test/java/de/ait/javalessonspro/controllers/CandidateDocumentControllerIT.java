package de.ait.javalessonspro.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.javalessonspro.enums.CandidateDocType;
import de.ait.javalessonspro.model.CandidateDocumentOs;
import de.ait.javalessonspro.repositories.CandidateDocumentOsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
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
import java.util.List;

import static java.nio.file.Files.exists;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 08.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
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

    @BeforeEach
    void setUp() throws Exception {
        repository.deleteAll();

        Path dir = Path.of(uploadDir);
        if (exists(dir)) {
            try (var walk = Files.walk(dir)) {
                walk.sorted((a, b) -> b.compareTo(a))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (Exception e) {
                                System.out.println("Error deleting file " + path);
                            }
                        });
            } catch (Exception e) {
                System.out.println("Error deleting dir " + dir);
            }
        }
        Files.createDirectories(dir);
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();

        try {
            Path testDir = Paths.get("./test-uploads");
            if (exists(testDir)) {
                deleteRecursively(testDir);
            }
        } catch (IOException e) {
            System.err.println("Failed to clean up test files: " + e.getMessage());
        }
    }

    private void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (var stream = Files.list(path)) {
                stream.forEach(p -> {
                    try {
                        deleteRecursively(p);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
        Files.deleteIfExists(path);
    }

    @Test
    @Order(1)
    @DisplayName("Test 1: Upload document → DB record + file on disk")
    void testUploadShouldSaveFileToOsAndSaveMetadataToDb() throws Exception {

        byte[] fileContent = "Test PDF content for candidate document".getBytes();

        MockMultipartFile multipartFile = new MockMultipartFile("file",
                "test-resume.pdf",
                "application/pdf",
                fileContent);

        String uploadResponse = mockMvc.perform(
                multipart("/api/candidates/documents/os")
                        .file(multipartFile)
                        .param("candidateEmail", "test.candidate@upload.com")
                        .param("docType", CandidateDocType.CV.name())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))


                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode uploadNode = objectMapper.readTree(uploadResponse);
        Long documentId = uploadNode.get("id").asLong();

        assertThat(documentId).isNotNull();

        List<CandidateDocumentOs> docs = repository.findAll();
        assertThat(docs.size()).isEqualTo(1);
        CandidateDocumentOs savedDoc = repository.findById(documentId).orElseThrow();
        assertThat(savedDoc.getCandidateEmail()).isEqualTo("test.candidate@upload.com");
        assertThat(savedDoc.getDocType()).isEqualTo(CandidateDocType.CV);
        assertThat(savedDoc.getOriginalFileName()).isEqualTo("test-resume.pdf");
        assertThat(savedDoc.getContentType()).isEqualTo("application/pdf");

        Path storedFilePath = Paths.get(savedDoc.getStoragePath());
        assertThat(Files.exists(storedFilePath)).isTrue();

        byte[] storedFileContent = Files.readAllBytes(storedFilePath);
        assertThat(storedFileContent).isEqualTo(fileContent);
        assertThat(savedDoc.getStoragePath()).isNotEmpty();



    }

/*    @Test
    @Order(2)
    @DisplayName("Test 2: Complete flow: Upload → List → Download → Delete → Verify removal")*/
}
