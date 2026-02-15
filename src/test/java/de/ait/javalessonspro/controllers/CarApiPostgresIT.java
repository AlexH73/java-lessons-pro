package de.ait.javalessonspro.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.javalessonspro.repositories.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Car API using PostgreSQL (Testcontainers).
 * Check the correctness of CRUD operations via the REST controller.
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 15.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration tests for Car API with PostgreSQL")
@Tag("integration")
@Tag("postgres")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CarApiPostgresIT extends BasePostgresTestcontainersIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CarRepository carRepository;

    @BeforeEach
    void setUp() {
        // Clearing the table before each isolation test
        carRepository.deleteAll();
    }

    //create-->list-->getId-->update-->delete-->VerifyDB

    @Test
    @Order(1)
    @DisplayName("Full CRUD cycle: create → list → get by id → update → delete → verify")
    @Tag("crud")
    void testCrudCarOnPostgres() throws Exception {
        // 1. Preparing data for creating a car
        String jsonBody = """
                {
                    "brand": "Toyota",
                    "model": "Camry",
                    "productionYear": 2020,
                    "mileage": 15000,
                    "price": 25000.00,
                    "status": "AVAILABLE",
                    "color": "Red",
                    "horsepower": 200,
                    "fuelType": "PETROL",
                    "transmission": "AUTOMATIC"
                }
                """;

        // 2. POST – creation of a new car
        String response = mockMvc.perform(
                        post("/api/cars")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        int createdId = objectMapper.readTree(response).get("id").asInt();

        // 3. GET /api/cars – check that the list contains exactly one car
        mockMvc.perform(get("/api/cars"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        // 4. GET /api/cars/{id} – checking the fields of the created car
        mockMvc.perform(get("/api/cars/{id}", createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"))
                .andExpect(jsonPath("$.productionYear").value(2020))
                .andExpect(jsonPath("$.mileage").value(15000))
                .andExpect(jsonPath("$.price").value(25000.00))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.color").value("Red"))
                .andExpect(jsonPath("$.horsepower").value(200))
                .andExpect(jsonPath("$.fuelType").value("PETROL"))
                .andExpect(jsonPath("$.transmission").value("AUTOMATIC"));

        // 5. PUT /api/cars/{id} – status update to "SOLD"
        String updateJson = """
                {
                    "brand": "Toyota",
                    "model": "Camry",
                    "productionYear": 2020,
                    "mileage": 15000,
                    "price": 25000.00,
                    "status": "SOLD",
                    "color": "Red",
                    "horsepower": 200,
                    "fuelType": "PETROL",
                    "transmission": "AUTOMATIC"
                }
                """;

        mockMvc.perform(put("/api/cars/{id}", createdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.status").value("SOLD"));

        // 6. DELETE /api/cars/{id} – deleting a car
        mockMvc.perform(delete("/api/cars/{id}", createdId))
                .andExpect(status().isNoContent());

        // 7. Check that the record has indeed been deleted from the database
        assertEquals(0, carRepository.count());

        // 8. GET /api/cars/{id} – the remote car request should return 404
        mockMvc.perform(get("/api/cars/{id}", createdId))
                .andExpect(status().isNotFound());
    }
}
