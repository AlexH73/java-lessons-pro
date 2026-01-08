package de.ait.javalessonspro.controllers;

import de.ait.javalessonspro.model.Car;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {
    private List<Car> allCars = List.of(
            new Car(1L, "BMW", "X5", 2000, 30000, 35000, "AVAILABLE"),
            new Car(2L, "Audi", "A4", 2025, 2000, 25000, "SOLD")
    );

    @GetMapping
    public List<Car> getAllCars() {
        return allCars;
    }

    @GetMapping("/{id}")
    public Car getCarById(@PathVariable Long id) {
        if (id == 1L) {
            return new Car(1L, "BMW", "X5", 2000, 30000, 35000, "AVAILABLE");
        } else if (id == 2L) {
            return new Car(2L, "Audi", "A4", 2025, 2000, 25000, "SOLD");
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public String deleteCar(@PathVariable Long id) {
        if (id == 1L) {
            allCars.removeFirst();
            return "deleted ID = 1";
        }
        if (id == 2L){
            allCars.remove(1);
            return "deleted ID = 2";
        } else {return "Not Found";
        }
    }
}
