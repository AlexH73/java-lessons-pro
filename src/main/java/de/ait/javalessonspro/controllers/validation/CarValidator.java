package de.ait.javalessonspro.controllers.validation;

import de.ait.javalessonspro.model.Car;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 21.01.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@Component
public final class CarValidator {

    private static final Logger log =
            LoggerFactory.getLogger(CarValidator.class);

    public static boolean isValid(Car car) {

        if (car == null) {
            log.warn("Invalid car object received: car=null");
            return false;
        }

        // Brand
        if (car.getBrand() == null || car.getBrand().isBlank()
                || car.getBrand().length() < 2
                || car.getBrand().length() > 50) {

            log.warn("Invalid car object received: brand='{}'", car.getBrand());
            return false;
        }

        // Model
        if (car.getModel() == null || car.getModel().isBlank()
                || car.getModel().length() > 50) {

            log.warn("Invalid car object received: model='{}'", car.getModel());
            return false;
        }

        // Production year
        int currentYear = Year.now().getValue();
        if (car.getProductionYear() < 1886 || car.getProductionYear() > currentYear) {

            log.warn("Invalid car object received: productionYear={}",
                    car.getProductionYear());
            return false;
        }

        // Mileage
        if (car.getMileage() < 0) {
            log.warn("Invalid car object received: mileage={}",
                    car.getMileage());
            return false;
        }

        // Price
        if (car.getPrice() == null) {
            log.warn("Price must not be null");
            return false;
        } else if (car.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid car object received: price={}",
                    car.getPrice());
            return false;
        }

        // Horsepower
        if (car.getHorsepower() < 1 || car.getHorsepower() > 1500) {
            log.warn("Invalid car object received: horsepower={}",
                    car.getHorsepower());
            return false;
        }

        // Color
        if (car.getColor() == null || car.getColor().isBlank()) {
            log.warn("Invalid car object received: color='{}'",
                    car.getColor());
            return false;
        }

        // Enums
        if (car.getFuelType() == null
                || car.getTransmission() == null
                || car.getStatus() == null) {

            log.warn(
                    "Invalid car object received: fuelType={}, transmission={}, status={}",
                    car.getFuelType(),
                    car.getTransmission(),
                    car.getStatus()
            );
            return false;
        }

        return true;
    }

    public static List<String> validateWithErrors(Car car) {

        List<String> errors = new ArrayList<>();

        if (car == null) {
            errors.add("Car must not be null");
            log.warn("Invalid car object received: car=null");
            return errors;
        }

        // Brand
        if (car.getBrand() == null || car.getBrand().isBlank()) {
            log.warn("Brand must not be empty: brand='{}'", car.getBrand());
            errors.add("Brand must not be empty");
        } else if (car.getBrand().length() < 2 || car.getBrand().length() > 50) {
            log.warn("Brand  length must be between 2 and 50 characters: brand length='{}' ", car.getBrand().length());
            errors.add("Brand length must be between 2 and 50 characters");
        }

        // Model
        if (car.getModel() == null || car.getModel().isBlank()) {
            log.warn("Model must not be empty: model='{}'", car.getModel());
            errors.add("Model must not be empty");
        } else if (car.getModel().length() > 50) {
            log.warn("Model length must not exceed 50 characters: model length='{}' ", car.getModel().length());
            errors.add("Model length must not exceed 50 characters");
        }

        // Production year
        int currentYear = Year.now().getValue();
        if (car.getProductionYear() < 1886 || car.getProductionYear() > currentYear) {
            log.warn("Production year must be between 1886 and {}: productionYear={} ",
                    currentYear, car.getProductionYear());
            errors.add("Production year must be between 1886 and " + currentYear);
        }

        // Mileage
        if (car.getMileage() < 0) {
            log.warn("Mileage must be greater or equal to 0: mileage={}",
                    car.getMileage());
            errors.add("Mileage must be greater or equal to 0");
        }

        // Price
        if (car.getPrice() == null) {
            log.warn("Price must not be null");
            errors.add("Price must not be null");
        } else if (car.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Price must be greater than 0: price={}", car.getPrice());
            errors.add("Price must be greater than 0");
        }

        // Horsepower
        if (car.getHorsepower() < 1 || car.getHorsepower() > 1500) {
            log.warn("Horsepower must be between 1 and 1500: horsepower={}",
                    car.getHorsepower());
            errors.add("Horsepower must be between 1 and 1500");
        }

        // Color
        if (car.getColor() == null || car.getColor().isBlank()) {
            log.warn("Color must not be empty: color='{}'",
                    car.getColor());
            errors.add("Color must not be empty");
        }

        // Enums
        if (car.getFuelType() == null) {
            log.warn("Fuel type must not be null");
            errors.add("Fuel type must not be null");
        }

        if (car.getTransmission() == null) {
            log.warn("Transmission must not be null");
            errors.add("Transmission must not be null");
        }

        if (car.getStatus() == null) {
            log.warn("Status must not be null");
            errors.add("Status must not be null");
        }

        if (!errors.isEmpty()) {
            log.warn("Invalid car object received: {}", errors);
        }

        return errors;
    }

}
