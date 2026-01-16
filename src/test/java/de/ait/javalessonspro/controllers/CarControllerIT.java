package de.ait.javalessonspro.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.javalessonspro.enums.CarStatus;
import de.ait.javalessonspro.model.Car;
import de.ait.javalessonspro.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CarControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        carRepository.deleteAll();
    }

    private Car buildValidCar(String brand, String model) {
        Car car = new Car();
        car.setBrand(brand);
        car.setModel(model);
        car.setProductionYear(2020);
        car.setMileage(30000);
        car.setPrice(30000);
        car.setStatus(CarStatus.AVAILABLE);

        return car;
    }

    @Test
    @DisplayName("GET /cars/{id} should return car if exist")
    void testGetCarByIdShouldReturnCar() throws Exception {
        Car saved = carRepository.save(buildValidCar("BMW", "X5"));

        mockMvc.perform(get("/api/cars/{id}", saved.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /cars/{id} should not return car")
    void testGetCarByIdShouldNotReturnCar() throws Exception {

        mockMvc.perform(get("/api/cars/{id}", 1L))
                .andExpect(status().isNotFound());
    }
}
