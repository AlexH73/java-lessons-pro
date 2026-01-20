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
import org.junit.jupiter.params.shadow.com.univocity.parsers.common.input.LineSeparatorDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Year;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        car.setPrice(30000.00);
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

        carValidate(car);

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
        assertEquals(car.getPrice(), cars.getFirst().getPrice());
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

    public static void carValidate(Car car) {

        assertNotNull(car, "Car must not be null");

        // Brand
        assertNotNull(car.getBrand(), "Brand must not be null");
        assertFalse(car.getBrand().isBlank(), "Brand must not be blank");
        assertTrue(car.getBrand().length() <= 50, "Brand length must be <= 50");
        assertTrue(car.getBrand().length() >= 2, "Brand length must be >= 2");

        // Model
        assertNotNull(car.getModel(), "Model must not be null");
        assertFalse(car.getModel().isBlank(), "Model must not be blank");
        assertTrue(car.getModel().length() <= 50, "Model length must be <= 50");

        // Production year
        int currentYear = Year.now().getValue();
        assertTrue(
                car.getProductionYear() >= 1900 && car.getProductionYear() <= currentYear,
                "Production year must be between 1900 and " + currentYear
        );

        // Mileage
        assertTrue(car.getMileage() >= 0, "Mileage must be >= 0");

        // Price
        assertTrue(car.getPrice() > 0, "Price must be > 0");

        // Horsepower
        assertTrue(
                car.getHorsepower() >= 1 && car.getHorsepower() <= 1500,
                "Horsepower must be between 1 and 1500"
        );

        // Color
        assertNotNull(car.getColor(), "Color must not be null");
        assertFalse(car.getColor().isBlank(), "Color must not be blank");

        // Enums
        assertNotNull(car.getFuelType(), "FuelType must not be null");
        assertNotNull(car.getTransmission(), "Transmission must not be null");
        assertNotNull(car.getStatus(), "CarStatus must not be null");
    }
}
