package de.ait.javalessonspro.util;

import de.ait.javalessonspro.enums.CarStatus;
import de.ait.javalessonspro.enums.FuelType;
import de.ait.javalessonspro.enums.Transmission;
import de.ait.javalessonspro.model.Car;
import de.ait.javalessonspro.repositories.CarRepository;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Configuration
public class InitData {

    @Bean
    CommandLineRunner initDatabase(CarRepository carRepository) {
        return args -> {
            if (carRepository.count() == 0) {
                Faker faker = new Faker();
                Random random = new Random();

                // Предопределенные значения для enum-полей
                String[] statuses = {"AVAILABLE", "RESERVED", "SOLD", "IN_REPAIR"};
                String[] fuelTypes = {"PETROL", "DIESEL", "HYBRID", "ELECTRIC"};
                String[] transmissions = {"AUTOMATIC", "MANUAL"};
                BigDecimal price = BigDecimal.valueOf(
                        faker.number().randomDouble(2, 15_000, 120_000)
                ).setScale(2, RoundingMode.HALF_UP);

                for (int i = 0; i < 20; i++) {
                    Car car = new Car();
                    car.setBrand(faker.vehicle().make());
                    car.setModel(faker.vehicle().model());
                    car.setProductionYear(faker.number().numberBetween(2018, 2024));
                    car.setMileage(faker.number().numberBetween(0, 60000));
                    car.setPrice(price);
                    car.setStatus(CarStatus.valueOf(statuses[random.nextInt(statuses.length)]));
                    car.setColor(faker.color().name());
                    car.setHorsepower(faker.number().numberBetween(80, 500));
                    car.setFuelType(FuelType.valueOf(fuelTypes[random.nextInt(fuelTypes.length)]));
                    car.setTransmission(Transmission.valueOf(transmissions[random.nextInt(transmissions.length)]));

                    carRepository.save(car);
                }

                System.out.println("Initial data loaded with DataFaker");
            }
        };
    }
}