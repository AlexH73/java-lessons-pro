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
            if (carRepository.count() == 0) {
                carRepository.save(new Car("Toyota", "RAV4", 2022, 15000, 28000.0,
                        "AVAILABLE", "Gray", 203, "HYBRID", "AUTOMATIC"));

                carRepository.save(new Car("Honda", "Civic", 2021, 35000, 22000.0,
                        "AVAILABLE", "Red", 158, "PETROL", "MANUAL"));

                carRepository.save(new Car("Ford", "Mustang", 2023, 5000, 55000.0,
                        "RESERVED", "Yellow", 450, "PETROL", "AUTOMATIC"));

                carRepository.save(new Car("Volkswagen", "Golf", 2020, 40000, 18000.0,
                        "SOLD", "White", 150, "DIESEL", "MANUAL"));

                carRepository.save(new Car("Tesla", "Model Y", 2023, 12000, 60000.0,
                        "AVAILABLE", "Blue", 384, "ELECTRIC", "AUTOMATIC"));

                carRepository.save(new Car("Hyundai", "Tucson", 2022, 22000, 32000.0,
                        "AVAILABLE", "Silver", 187, "PETROL", "AUTOMATIC"));

                carRepository.save(new Car("Kia", "Sportage", 2021, 28000, 26000.0,
                        "IN_REPAIR", "Black", 175, "PETROL", "AUTOMATIC"));

                carRepository.save(new Car("Nissan", "Qashqai", 2020, 45000, 21000.0,
                        "AVAILABLE", "Green", 140, "DIESEL", "MANUAL"));

                carRepository.save(new Car("Mazda", "CX-5", 2023, 8000, 34000.0,
                        "AVAILABLE", "Red", 187, "PETROL", "AUTOMATIC"));

                carRepository.save(new Car("Subaru", "Outback", 2022, 18000, 38000.0,
                        "AVAILABLE", "Blue", 182, "PETROL", "AUTOMATIC"));

                carRepository.save(new Car("Lexus", "RX", 2023, 6000, 65000.0,
                        "RESERVED", "White", 295, "HYBRID", "AUTOMATIC"));

                carRepository.save(new Car("Porsche", "911", 2022, 12000, 120000.0,
                        "SOLD", "Black", 379, "PETROL", "AUTOMATIC"));

                carRepository.save(new Car("Jeep", "Wrangler", 2021, 25000, 42000.0,
                        "AVAILABLE", "Orange", 285, "PETROL", "MANUAL"));

                carRepository.save(new Car("Chevrolet", "Camaro", 2020, 30000, 35000.0,
                        "AVAILABLE", "Red", 335, "PETROL", "AUTOMATIC"));

                carRepository.save(new Car("Volvo", "XC60", 2023, 10000, 52000.0,
                        "AVAILABLE", "Gray", 247, "HYBRID", "AUTOMATIC"));

                carRepository.save(new Car("Land Rover", "Range Rover", 2022, 15000, 95000.0,
                        "AVAILABLE", "Black", 395, "DIESEL", "AUTOMATIC"));

                carRepository.save(new Car("Fiat", "500", 2021, 20000, 18000.0,
                        "AVAILABLE", "Yellow", 85, "PETROL", "MANUAL"));

                carRepository.save(new Car("Skoda", "Octavia", 2020, 55000, 19000.0,
                        "SOLD", "Silver", 150, "DIESEL", "AUTOMATIC"));

                carRepository.save(new Car("Renault", "Clio", 2022, 22000, 17000.0,
                        "AVAILABLE", "Blue", 100, "PETROL", "MANUAL"));

                carRepository.save(new Car("Peugeot", "3008", 2023, 9000, 36000.0,
                        "AVAILABLE", "White", 180, "PETROL", "AUTOMATIC"));
                System.out.println("Initial data loaded");
            }
        };
    }
}