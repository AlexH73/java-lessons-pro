package de.ait.javalessonspro.integration;

import de.ait.javalessonspro.model.Car;
import de.ait.javalessonspro.repositories.CarRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 23.01.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Given application started with test profile")
class GivenTestProfileIT {

    @Autowired
    private CarRepository carRepository;

    @Test
    @DisplayName("When application starts, then test seed data is loaded")
    void whenApplicationStarts_thenTestSeedIsLoaded() {

        List<Car> cars = carRepository.findAll();
        assertThat(cars).isNotNull();
        assertThat(cars)
                .filteredOn(car -> car.getBrand().equals("BMW"))
                .singleElement()
                .extracting(Car::getModel)
                .isEqualTo("X5");
        assertThat(cars)
                .filteredOn(car -> car.getBrand().equals("Audi"))
                .singleElement()
                .extracting(Car::getModel)
                .isEqualTo("A4");
    }
}
