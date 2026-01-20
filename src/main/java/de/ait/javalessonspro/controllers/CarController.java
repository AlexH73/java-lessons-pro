package de.ait.javalessonspro.controllers;

import de.ait.javalessonspro.enums.CarStatus;
import de.ait.javalessonspro.enums.FuelType;
import de.ait.javalessonspro.model.Car;
import de.ait.javalessonspro.repository.CarRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

@Tag(name = "Car management API")
@RestController
@RequestMapping("/api/cars")
@Slf4j
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
        log.info("Fetching all cars from the repository");
        return ResponseEntity.ok(carRepository.findAll());
    }

    @Operation(
            summary = "Get a car by ID",
            description = "Returns a single car identified by its unique ID."
    )
    @GetMapping("/{id}")
    public ResponseEntity<Car> getCarById(@PathVariable Long id) {
        return carRepository.findById(id)
                .map(car -> {
                    log.info("Car with id {} found", id);
                    return ResponseEntity.ok(car);
                })
                .orElseGet(() -> {
                    log.warn("Car with id {} not found", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @Operation(
            summary = "Search cars by brand",
            description = """
                    Returns a list of cars filtered by brand name. The search is case-insensitive.
                    
                    **Automatic URL normalization:**
                    - If the brand parameter contains uppercase letters (e.g., `?brand=BMW`), 
                      the endpoint returns a 301 redirect to the lowercase version (`?brand=bmw`)
                    - If already lowercase (e.g., `?brand=bmw`), returns results directly
                    
                    **Examples:**
                    - `GET /api/cars/search?brand=BMW` → 301 Redirect → `GET /api/cars/search?brand=bmw`
                    - `GET /api/cars/search?brand=bmw` → 200 OK with results
                    - `GET /api/cars/search?brand=ToYoTa` → 301 Redirect → `GET /api/cars/search?brand=toyota`
                    
                    **Note:** Brand names in the database are stored in uppercase, but the API accepts 
                    any case and normalizes to lowercase in URLs for consistency.
                    """
    )
    @GetMapping("/search")
    public ResponseEntity<List<Car>> searchCars(@RequestParam String brand) {
        if (!carRepository.existsByBrandIgnoreCase(brand)) {
            log.warn("Search cars: brand '{}' not found", brand);
            return ResponseEntity.notFound().build();
        }

        if (!brand.equals(brand.toLowerCase())) {
            log.info("Search cars: redirecting brand '{}' to lowercase '{}'",
                    brand, brand.toLowerCase());

            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                    .location(URI.create("/api/cars/search?brand=" +
                            brand.toLowerCase()))
                    .build();
        }
        List<Car> cars = carRepository.findByBrandIgnoreCase(brand);
        log.info("Search cars: found {} cars for brand '{}'", cars.size(), brand);

        return ResponseEntity.ok(cars);
    }

    @Operation(
            summary = "Delete a car by ID",
            description = "Deletes a car identified by its unique ID."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        if (!carRepository.existsById(id)) {
            log.warn("Car with id {} not found", id);
            return ResponseEntity.notFound().build();
        }
        carRepository.deleteById(id);
        log.info("Car with id {} deleted", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Add a new car",
            description = "Creates a new car and stores it in the system."
    )
    @PostMapping
    public ResponseEntity<Car> addCar(@RequestBody Car car) {
        if (car.getStatus() == null) {
            log.error("Car status is null");
            return ResponseEntity.badRequest().build();
        }

        Car savedCar = carRepository.save(car);
        if (savedCar == null) {
            log.error("Car could not be saved");
            return ResponseEntity.badRequest().build();
        }

        log.info("Car with id {} saved", savedCar.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCar);
    }

    @Operation(
            summary = "Update a car by ID",
            description = "Updates an existing car identified by its unique ID with new data."
    )
    @PutMapping("/{id}")
    public ResponseEntity<Car> updateCar(@PathVariable Long id, @RequestBody Car car) {
        Car carToUpdate = carRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Car with id {} not found", id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Car with ID = " + id + " not found"
                    );
                });

        carToUpdate.setBrand(car.getBrand());
        carToUpdate.setModel(car.getModel());
        carToUpdate.setProductionYear(car.getProductionYear());
        carToUpdate.setMileage(car.getMileage());
        carToUpdate.setPrice(car.getPrice());
        carToUpdate.setStatus(car.getStatus());
        carToUpdate.setColor(car.getColor());
        carToUpdate.setHorsepower(car.getHorsepower());
        carToUpdate.setFuelType(car.getFuelType());
        carToUpdate.setTransmission(car.getTransmission());

        log.info("Car with id {} updated", id);

        return ResponseEntity.ok(carRepository.save(carToUpdate));
    }

    @Operation(
            summary = "Search cars by price range",
            description = "Returns a list of cars with prices between the specified minimum and maximum values. " +
                    "Example: /api/cars/by-price?min=10000&max=20000"
    )
    @GetMapping("/by-price")
    public ResponseEntity<List<Car>> searchByPriceBetween(
            @RequestParam double min, @RequestParam double max
    ) {

        List<Car> cars = carRepository.findByPriceBetween(min, max);
        log.info("Search cars by price: min={}, max={}, found={}", min, max, cars.size());
        return ResponseEntity.ok(cars);
    }

    @Operation(
            summary = "Search cars by color",
            description = "Returns a list of cars with the specified color (case-insensitive). " +
                    "Available colors include Black, White, Silver, Blue, Red, Gray, etc. " +
                    "Example: /api/cars/by-color?color=black"
    )
    @GetMapping("/by-color")
    public ResponseEntity<List<Car>> getCarByColor(@RequestParam String color) {
        if (!carRepository.existsByColorIgnoreCase(color)) {
            log.warn("Search cars by color: color '{}' not found", color);
            return ResponseEntity.notFound().build();
        }

        List<Car> cars = carRepository.findByColorIgnoreCase(color);
        log.info("Search cars by color: color='{}', found={}", color, cars.size());

        return ResponseEntity.ok(cars);
    }

    @Operation(
            summary = "Search cars by fuel type",
            description = "Returns a list of cars with the specified fuel type. " +
                    "Available fuel types: PETROL, DIESEL, ELECTRIC, HYBRID. " +
                    "Example: /api/cars/by-fuel?fuelType=DIESEL"
    )
    @GetMapping("/by-fuel")
    public ResponseEntity<List<Car>> getCarByFuelType(@RequestParam FuelType fuelType) {
        if (!carRepository.existsByFuelType(fuelType)) {
            log.warn("Search cars by fuel type: fuelType '{}' not found", fuelType);
            return ResponseEntity.notFound().build();
        }

        List<Car> cars = carRepository.findByFuelType(fuelType);
        log.info("Search cars by fuel type: fuelType='{}', found={}", fuelType, cars.size());

        return ResponseEntity.ok(cars);
    }

    @Operation(
            summary = "Search cars by horsepower range",
            description = "Returns a list of cars with horsepower between the specified values. " +
                    "Example: /api/cars/by-power?minHp=150&maxHp=300"
    )
    @GetMapping("/by-power")
    public ResponseEntity<List<Car>> searchByHorsepowerBetween(
            @RequestParam @Parameter(description = "Minimum horsepower", example = "150") int minHp,
            @RequestParam @Parameter(description = "Maximum horsepower", example = "300") int maxHp
    ) {
        if (minHp < 0 || maxHp < 0 || minHp > maxHp) {
            log.warn("Search cars by horsepower: invalid range minHp={}, maxHp={}", minHp, maxHp);
            return ResponseEntity.badRequest().build();
        }

        List<Car> cars = carRepository.findByHorsepowerBetween(minHp, maxHp);
        log.info("Search cars by horsepower: minHp={}, maxHp={}, found={}", minHp, maxHp, cars.size());

        return ResponseEntity.ok(cars);
    }

    @Operation(
            summary = "Search cars by status",
            description = "Returns a list of cars with the specified status. " +
                    "Available statuses: AVAILABLE, SOLD, RESERVED, IN_REPAIR. " +
                    "Example: /api/cars/by-status?status=AVAILABLE"
    )
    @GetMapping("/by-status")
    public ResponseEntity<List<Car>> getCarsByStatus(
            @RequestParam @Parameter(description = "Status of the car", example = "AVAILABLE")
            CarStatus status) {
        List<Car> cars = carRepository.findByStatus(status);

        if (cars.isEmpty()) {
            log.warn("Search cars by status: status '{}' not found", status);
        }

        log.info("Search cars by status: status='{}', found={}", status, cars.size());
        return ResponseEntity.ok(cars);
    }

}