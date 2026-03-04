package de.ait.javalessonspro.util;

import de.ait.javalessonspro.enums.CarStatus;
import de.ait.javalessonspro.enums.FuelType;
import de.ait.javalessonspro.enums.Transmission;
import de.ait.javalessonspro.model.Car;
import de.ait.javalessonspro.repositories.CarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class InitData {

    @Value("${app.seed.cars.count}")
    private int carsCount;

    @Value("${app.seed.enabled}")
    private boolean seedEnabled;

    @Bean
    CommandLineRunner initDatabase(CarRepository carRepository) {
        return args -> {
            if (!seedEnabled) {
                log.info("Data seeding is disabled. Skipping initial data load.");
                return;
            }

            if (carRepository.count() != 0) {
                log.info("Database already contains data ({} cars). Skipping initial data load.", carRepository.count());
                carRepository.deleteAll();
            }

                Faker faker = new Faker();
                Random random = new Random();

                // Предопределенные значения для полей
                List<String> brands = List.of("BMW", "Audi", "Mercedes-Benz", "Volkswagen", "Porsche",
                        "Toyota", "Honda", "Mazda", "Nissan", "Hyundai",
                        "Kia", "Ford", "Chevrolet", "Tesla", "Volvo",
                        "Skoda", "Seat", "Peugeot", "Renault", "Opel");
                List<String> bmwModels = List.of("X1", "X3", "X5", "3 Series", "5 Series", "7 Series", "M3", "M5");
                List<String> audiModels = List.of("A3", "A4", "A6", "Q3", "Q5", "Q7", "S3", "S5");
                List<String> mercModels = List.of("A-Class", "C-Class", "E-Class", "S-Class", "GLA", "GLC", "GLE");
                List<String> vwModels = List.of("Golf", "Passat", "Tiguan", "Touareg", "Polo", "Arteon");
                List<String> toyotaModels = List.of("Corolla", "Camry", "RAV4", "Yaris", "Prius");
                List<String> colors = List.of("Black", "White", "Red", "Blue", "Green", "Yellow", "Orange", "Purple", "Brown", "Pink", "Silver", "Gold");

                for (int i = 0; i < carsCount; i++) {
                    Car car = new Car();
                    String brand = brands.get(random.nextInt(brands.size()));
                    car.setBrand(brand);

                    String model = pickModelForBrand(brand, random, bmwModels, audiModels, mercModels, vwModels, toyotaModels, faker);
                    car.setModel(model);
                    car.setProductionYear(faker.number().numberBetween(2018, 2024));
                    car.setMileage(faker.number().numberBetween(1, 60000));

                    BigDecimal price = BigDecimal.valueOf(
                            faker.number().randomDouble(2, 15_000, 120_000)
                    ).setScale(2, RoundingMode.HALF_UP);
                    car.setPrice(price);

                    String color = pickEnum(colors.toArray(new String[0]), random);
                    car.setColor(color);

                    car.setHorsepower(faker.number().numberBetween(80, 500));
                    car.setStatus(CarStatus.AVAILABLE);
                    car.setTransmission(pickEnum(Transmission.values(), random));
                    car.setFuelType(pickEnum(FuelType.values(), random));

                    carRepository.save(car);
                    log.info("{} cars have been generated and saved.", carsCount);
                }

            log.info("Initial data loaded with DataFaker");
        };
    }

    private String pickModelForBrand(String brand, Random random, List<String> bmwModels, List<String> audiModels, List<String> mercModels, List<String> vwModels, List<String> toyotaModels, Faker faker) {
        return switch (brand) {
            case "BMW" -> bmwModels.get(random.nextInt(bmwModels.size()));
            case "Audi" -> audiModels.get(random.nextInt(audiModels.size()));
            case "Mercedes-Benz" -> mercModels.get(random.nextInt(mercModels.size()));
            case "Volkswagen" -> vwModels.get(random.nextInt(vwModels.size()));
            case "Toyota" -> toyotaModels.get(random.nextInt(toyotaModels.size()));

            default -> "Model-" + String.valueOf(faker.number().numberBetween(100,999));
        };
    }

    private <T> T pickEnum(T[] values, Random random) {
        return values[random.nextInt(values.length)];
    }
}