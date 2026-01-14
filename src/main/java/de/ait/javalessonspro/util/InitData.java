package de.ait.javalessonspro.util;

import de.ait.javalessonspro.model.Car;
import de.ait.javalessonspro.repository.CarRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitData {
    @Bean
    CommandLineRunner initDatabase(CarRepository carRepository) {
        return args -> {
            if(carRepository.count() == 0){
                carRepository.save(new Car("BMW", "X5", 2020, 30000, 35000, "AVAILABLE"));
                carRepository.save(new Car("Audi", "A4", 2023, 20000, 25000, "SOLD"));
                carRepository.save(new Car("BMW", "X3", 2021, 25000, 30000, "AVAILABLE"));
                carRepository.save(new Car("Mercedes", "C200", 2022, 15000, 40000, "AVAILABLE"));
                carRepository.save(new Car("Toyota", "Camry", 2019, 45000, 20000, "SOLD"));
                carRepository.save(new Car("Audi", "Q5", 2022, 18000, 38000, "AVAILABLE"));
                carRepository.save(new Car("BMW", "M5", 2023, 5000, 70000, "SOLD"));
                carRepository.save(new Car("Mercedes", "E300", 2021, 22000, 45000, "AVAILABLE"));
            }
        };
    }
}
