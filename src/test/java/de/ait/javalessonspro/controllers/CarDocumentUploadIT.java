package de.ait.javalessonspro.controllers;

import de.ait.javalessonspro.enums.CarDocumentType;
import de.ait.javalessonspro.enums.CarStatus;
import de.ait.javalessonspro.enums.FuelType;
import de.ait.javalessonspro.enums.Transmission;
import de.ait.javalessonspro.model.Car;
import de.ait.javalessonspro.model.CarDocumentOs;
import de.ait.javalessonspro.repositories.CarDocumentOsRepository;
import de.ait.javalessonspro.repositories.CarRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.file.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CarDocumentUploadIT {

    @Autowired
    private  MockMvc mockMvc;

    @Autowired
    private  CarRepository carRepository;

    @Autowired
    private  CarDocumentOsRepository carDocumentOsRepository;

    @Value("${app.upload.car-docs-dir}")
    private String uploadDir;
    @Autowired
    private ObjectMapper objectMapper;



    @BeforeEach
    void setUp() throws Exception{
        carRepository.deleteAll();
        carDocumentOsRepository.deleteAll();

        Path dir = Path.of(uploadDir);
        if(Files.exists(dir)){
            try(var walk = Files.walk(dir)) {
                walk.sorted((a,b) -> b.compareTo(a))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (Exception e) {
                                System.out.println("Error deleting file " + path);
                            }
                        });
            }
            catch (Exception e){
                System.out.println("Error deleting dir " + dir);
            }
        }
        Files.createDirectories(dir);
    }

    @Test
    void testUploadShouldSaveFileToOsAndSaveMetadataToDb() throws Exception{
        Car car = new Car();
        //car.setId(5L);
        car.setBrand("BMW");
        car.setModel("X5");
        car.setProductionYear(2020);
        car.setMileage(100000);
        car.setPrice(BigDecimal.valueOf(50000.00));
        car.setStatus(CarStatus.AVAILABLE);
        car.setColor("red");
        car.setFuelType(FuelType.DIESEL);
        car.setHorsepower(150);
        car.setTransmission(Transmission.AUTOMATIC);


        Car saved = carRepository.saveAndFlush(car);
        Long carId = saved.getId();

        byte[] fileContent = "test-file-content".getBytes();

        MockMultipartFile multipartFile = new MockMultipartFile("file",
                "test-file.pdf",
                "application/pdf",
                fileContent);


        String uploadResponseJson = mockMvc.perform(
                        multipart("/api/cars/{carId}/documents/os", carId)
                                .file(multipartFile)
                                .param("docType", CarDocumentType.CONTRACT.name())
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode uploadNode = objectMapper.readTree(uploadResponseJson);

        Long docId = uploadNode.get("id").asLong();

        assertThat(docId).isNotNull();

        List<CarDocumentOs> docs = carDocumentOsRepository.findAllByCarId(carId);

        assertThat(docs.size()).isEqualTo(1);

        CarDocumentOs doc = docs.getFirst();

        assertThat(doc.getDocType()).isEqualTo(CarDocumentType.CONTRACT);
        assertThat(doc.getId()).isEqualTo(1);
        assertThat(doc.getOriginalFileName()).isEqualTo("test-file.pdf");
        assertThat(doc.getContentType()).isEqualTo("application/pdf");

        Path storedPath = Paths.get(doc.getStoragePath());

        assertThat(Files.exists(storedPath)).isTrue();

        byte[] storedBytes = Files.readAllBytes(storedPath);

        assertThat(storedBytes).isEqualTo(fileContent);


    }
}
