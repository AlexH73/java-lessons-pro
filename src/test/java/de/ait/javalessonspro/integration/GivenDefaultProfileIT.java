package de.ait.javalessonspro.integration;

import de.ait.javalessonspro.model.Car;
import de.ait.javalessonspro.repositories.CarRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
@DisplayName("Given application started without test profile")
class GivenDefaultProfileIT {

    @Autowired
    private CarRepository carRepository;

    @Test
    @DisplayName("When application starts, then test seed data is not loaded")
    void whenApplicationStarts_thenTestSeedIsNotLoaded() {

        List<Car> cars = carRepository.findAll();

        assertThat(cars).isNotNull();

        assertThat(cars)
                .extracting(Car::getBrand)
                .containsAnyOf("Toyota", "Honda", "Ford")
                .doesNotContain("BMW", "Audi");
    }
}
