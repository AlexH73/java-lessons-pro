package de.ait.javalessonspro.controllers;

import de.ait.javalessonspro.model.Car;
import de.ait.javalessonspro.repository.CarRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(name = "Car management API")
@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarRepository carRepository;

    @Value("${app.dealership.name: Welcome to AIT Gr.59 API}")
    private String dealerShipName;

    public CarController(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @Operation(
            summary = "Get application information",
            description = "Returns basic information about the car dealership application, " +
                    "including the dealership name."
    )
    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
        return ResponseEntity.ok("Welcome to the " + dealerShipName + " car dealership!");
    }

    @Operation(
            summary = "Get all cars",
            description = "Returns a list of all cars available in the system."
    )
    @GetMapping
    public ResponseEntity<List<Car>> getAllCars() {
        return ResponseEntity.ok(carRepository.findAll());
    }

    @Operation(
            summary = "Get a car by ID",
            description = "Returns a single car identified by its unique ID."
    )
    @GetMapping("/{id}")
    public ResponseEntity<Car> getCarById(@PathVariable Long id) {
        return carRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Search cars by brand",
            description = "Returns a list of cars filtered by brand name (case-insensitive). " +
                    "Example: /api/cars/search?brand=BMW"
    )
    @GetMapping("/search")
    public ResponseEntity<List<Car>> searchCars(@RequestParam String brand) {
        if (!carRepository.existsByBrandIgnoreCase(brand)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(carRepository.findByBrandIgnoreCase(brand));
    }

    @Operation(
            summary = "Delete a car by ID",
            description = "Deletes a car identified by its unique ID."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        if (!carRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        carRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Add a new car",
            description = "Creates a new car and stores it in the system."
    )
    @PostMapping
    public ResponseEntity<Car> addCar(@RequestBody Car car) {
        Car savedCar = carRepository.save(car);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCar);
    }

    @Operation(
            summary = "Update a car by ID",
            description = "Updates an existing car identified by its unique ID with new data."
    )
    @PutMapping("/{id}")
    public ResponseEntity<Car> updateCar(@PathVariable Long id, @RequestBody Car car) {
        Car carToUpdate = carRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Car with ID = " + id + " not found"
                ));

        carToUpdate.setBrand(car.getBrand());
        carToUpdate.setModel(car.getModel());
        carToUpdate.setProductionYear(car.getProductionYear());
        carToUpdate.setMileage(car.getMileage());
        carToUpdate.setPrice(car.getPrice());
        carToUpdate.setStatus(car.getStatus());

        return ResponseEntity.ok(carRepository.save(carToUpdate));
    }


    @Operation(
            summary = "Search cars by price range",
            description = "Returns a list of cars with prices between the specified minimum and maximum values. " +
                    "Example: /api/cars/by-price?min=10000&max=20000"
    )
    @GetMapping("/by-price")
    public ResponseEntity<List<Car>> searchByPriceBetween(
            @RequestParam int min, @RequestParam int max
    ) {
        return ResponseEntity.ok(carRepository.findByPriceBetween(min, max));
    }
}