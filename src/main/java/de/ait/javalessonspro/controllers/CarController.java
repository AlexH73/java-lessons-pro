package de.ait.javalessonspro.controllers;

import de.ait.javalessonspro.model.Car;
import de.ait.javalessonspro.repository.CarRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
@AllArgsConstructor
public class CarController {

    private final CarRepository carRepository;

    @GetMapping
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    @GetMapping("/{id}")
    public Car getCarById(@PathVariable Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Car with ID = " + id + " not found"
                ));
    }

    @GetMapping("/brand/{brand}")
    public List<Car> getCarsByBrand(@PathVariable String brand) {
        return carRepository.findByBrandIgnoreCase(brand);
    }

    @DeleteMapping("/{id}")
    public String deleteCar(@PathVariable Long id) {
        if (carRepository.existsById(id)) {
            carRepository.deleteById(id);
            return "Car with ID = " + id + " deleted";
        }

        return "Car with ID = " + id + " not found";
    }

    @PostMapping
    public Long addCar(@RequestBody Car car) {
        return carRepository.save(car).getId();
    }

    @PutMapping("/{id}")
    public Car updateCar(@PathVariable Long id, @RequestBody Car car) {
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

        return carRepository.save(carToUpdate);
    }
}