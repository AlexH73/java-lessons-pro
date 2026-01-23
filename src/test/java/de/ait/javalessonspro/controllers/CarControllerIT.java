package de.ait.javalessonspro.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.javalessonspro.enums.CarStatus;
import de.ait.javalessonspro.enums.FuelType;
import de.ait.javalessonspro.enums.Transmission;
import de.ait.javalessonspro.model.Car;
import de.ait.javalessonspro.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
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
        car.setPrice(BigDecimal.valueOf(30000.00));
        car.setStatus(CarStatus.AVAILABLE);
        car.setColor("Black");
        car.setHorsepower(200);
        car.setFuelType(FuelType.PETROL);
        car.setTransmission(Transmission.AUTOMATIC);

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

    @Test
    @DisplayName("POST /api/cars should save car in H2 return 201")
    void testCreateNewCarShouldReturn201() throws Exception {
        Car car = buildValidCar("Audi", "A6");

        String jsonBody = objectMapper.writeValueAsString(car);

        mockMvc.perform(
                        post("/api/cars")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                .andExpect(status().isCreated());

        List<Car> cars = carRepository.findAll();
        assertEquals(1, cars.size());

        assertEquals(car.getBrand(), cars.getFirst().getBrand());
        assertEquals(car.getModel(), cars.getFirst().getModel());
        assertEquals(0,
                car.getPrice().compareTo(cars.getFirst().getPrice()));
        assertEquals(car.getStatus(), cars.getFirst().getStatus());
        assertEquals(car.getColor(), cars.getFirst().getColor());
        assertEquals(car.getFuelType(), cars.getFirst().getFuelType());
        assertEquals(car.getHorsepower(), cars.getFirst().getHorsepower());
        assertEquals(car.getTransmission(), cars.getFirst().getTransmission());
        assertEquals(car.getProductionYear(), cars.getFirst().getProductionYear());
        assertEquals(car.getMileage(), cars.getFirst().getMileage());

    }

    @Test
    @DisplayName("PUT /api/cars should update car in H2 return 201")
    void testUpdateCarShouldReturn201() throws Exception {
        Car car = buildValidCar("Mercedes-Benz", "E-300 de");

        String jsonBody = objectMapper.writeValueAsString(car);

        mockMvc.perform(
                        post("/api/cars")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                .andExpect(status().isCreated());

        List<Car> cars = carRepository.findAll();
        assertEquals(1, cars.size());

        assertEquals(car.getBrand(), cars.getFirst().getBrand());
        assertEquals(car.getModel(), cars.getFirst().getModel());
        assertEquals(0,
                car.getPrice().compareTo(cars.getFirst().getPrice()));
        assertEquals(car.getStatus(), cars.getFirst().getStatus());
        assertEquals(car.getColor(), cars.getFirst().getColor());
        assertEquals(car.getFuelType(), cars.getFirst().getFuelType());
        assertEquals(car.getHorsepower(), cars.getFirst().getHorsepower());
        assertEquals(car.getTransmission(), cars.getFirst().getTransmission());
        assertEquals(car.getProductionYear(), cars.getFirst().getProductionYear());
        assertEquals(car.getMileage(), cars.getFirst().getMileage());

    }

    @Test
    @DisplayName("GET /api/cars should return all cars")
    void testGetAllCarsShouldReturnAllCars() throws Exception {
        Car carAudi = buildValidCar("Audi", "A6");
        Car carKia = buildValidCar("Kia", "Rio");

        carRepository.save(carAudi);
        carRepository.save(carKia);

        mockMvc.perform(get("/api/cars"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].brand", containsInAnyOrder("Audi", "Kia")))
                .andExpect(jsonPath("$[*].model", containsInAnyOrder("A6", "Rio")));

        List<Car> cars = carRepository.findAll();
        assertEquals(2, cars.size());
    }

    @Test
    @DisplayName("POST /api/cars should not save car in H2, invalid JSON return 400")
    void testCreateNewCarShouldReturn400() throws Exception {
        Car car = buildValidCar("Audi", "A6");
        car.setStatus(null);

        String jsonBody = objectMapper.writeValueAsString(car);

        mockMvc.perform(
                        post("/api/cars")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                .andExpect(status().isBadRequest());

        List<Car> cars = carRepository.findAll();
        assertTrue(cars.isEmpty());


    }

    @Test
    @DisplayName("PUT /api/cars should not update car in H2, invalid JSON return 400")
    void testUpdateCarShouldReturn400() throws Exception {
        Car car = buildValidCar("Mercedes-Benz", "E-300 de");
//        car.setBrand(" ");
//        car.setModel("");
//        car.setProductionYear(1800);
//        car.setMileage(-100);
//        car.setPrice(0);
//        car.setHorsepower(-2000);
//        car.setStatus(null);
//        car.setHorsepower(0);
//        car.setColor(null);
//        car.setFuelType(null);
        car.setTransmission(null);

        String jsonBody = objectMapper.writeValueAsString(car);

        mockMvc.perform(
                        post("/api/cars")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody))
                .andExpect(status().isBadRequest());

        List<Car> cars = carRepository.findAll();
        assertTrue(cars.isEmpty());


    }
}
