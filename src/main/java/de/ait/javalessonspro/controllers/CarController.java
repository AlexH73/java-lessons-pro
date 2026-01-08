package de.ait.javalessonspro.controllers;

import de.ait.javalessonspro.model.Car;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {
    private List<Car> allCars = new ArrayList<>(List.of(
            new Car(1L, "BMW", "X5", 2020, 30000, 35000, "AVAILABLE"),
            new Car(2L, "Audi", "A4", 2023, 20000, 25000, "SOLD"),
            new Car(3L, "BMW", "X3", 2021, 25000, 30000, "AVAILABLE"),
            new Car(4L, "Mercedes", "C200", 2022, 15000, 40000, "AVAILABLE"),
            new Car(5L, "Toyota", "Camry", 2019, 45000, 20000, "SOLD"),
            new Car(6L, "Audi", "Q5", 2022, 18000, 38000, "AVAILABLE"),
            new Car(7L, "BMW", "M5", 2023, 5000, 70000, "SOLD"),
            new Car(8L, "Mercedes", "E300", 2021, 22000, 45000, "AVAILABLE")
    ));

    @GetMapping
    public List<Car> getAllCars() {
        return allCars;
    }

    @GetMapping("/{id}")
    public Car getCarById(@PathVariable Long id) {
        for (Car car : allCars) {
            if (car.getId().equals(id)) {
                return car;
            }
        }
        return null;
    }

    @GetMapping("/brand/{brand}")
    public List<Car> getCarsByBrand(@PathVariable String brand) {
        List<Car> result = new ArrayList<>();

        for (Car car : allCars) {
            if (car.getBrand().equalsIgnoreCase(brand)) {
                result.add(car);
            }
        }

        return result;
    }

    @DeleteMapping("/{id}")
    public String deleteCar(@PathVariable Long id) {
        Car carToRemove = null;

        // Ищем машину по ID
        for (Car car : allCars) {
            if (car.getId().equals(id)) {
                carToRemove = car;
                break;
            }
        }

        // Если нашли - удаляем
        if (carToRemove != null) {
            allCars.remove(carToRemove);
            return "Car with ID = " + id + " deleted";
        }

        return "Car not found";
    }
}