package de.ait.javalessonspro.controllers.validation;

import de.ait.javalessonspro.model.Car;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public final class CarValidator {

    private static final Logger log =
            LoggerFactory.getLogger(CarValidator.class);

    private CarValidator() {
    }

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
        if (car.getProductionYear() < 1900 || car.getProductionYear() > currentYear) {

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
        if (car.getPrice() <= 0) {
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

        isValid(car); // Log if car is null

        List<String> errors = new ArrayList<>();

        if (car == null) {
            errors.add("Car must not be null");
            log.warn("Invalid car object received: car=null");
            return errors;
        }

        // Brand
        if (car.getBrand() == null || car.getBrand().isBlank()) {
            errors.add("Brand must not be empty");
        } else if (car.getBrand().length() < 2 || car.getBrand().length() > 50) {
            errors.add("Brand length must be between 2 and 50 characters");
        }

        // Model
        if (car.getModel() == null || car.getModel().isBlank()) {
            errors.add("Model must not be empty");
        } else if (car.getModel().length() > 50) {
            errors.add("Model length must not exceed 50 characters");
        }

        // Production year
        int currentYear = Year.now().getValue();
        if (car.getProductionYear() < 1900 || car.getProductionYear() > currentYear) {
            errors.add("Production year must be between 1900 and " + currentYear);
        }

        // Mileage
        if (car.getMileage() < 0) {
            errors.add("Mileage must be greater or equal to 0");
        }

        // Price
        if (car.getPrice() <= 0) {
            errors.add("Price must be greater than 0");
        }

        // Horsepower
        if (car.getHorsepower() < 1 || car.getHorsepower() > 1500) {
            errors.add("Horsepower must be between 1 and 1500");
        }

        // Color
        if (car.getColor() == null || car.getColor().isBlank()) {
            errors.add("Color must not be empty");
        }

        // Enums
        if (car.getFuelType() == null) {
            errors.add("Fuel type must not be null");
        }

        if (car.getTransmission() == null) {
            errors.add("Transmission must not be null");
        }

        if (car.getStatus() == null) {
            errors.add("Status must not be null");
        }

        if (!errors.isEmpty()) {
            log.warn("Invalid car object received: {}", errors);
        }

        return errors;
    }

}
