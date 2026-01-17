package de.ait.javalessonspro.repository;

import de.ait.javalessonspro.enums.CarStatus;
import de.ait.javalessonspro.enums.FuelType;
import de.ait.javalessonspro.model.Car;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {

    // SELECT * FROM CARS WHERE brand = ?
    List<Car> findByBrandIgnoreCase(String brand);

    List<Car> findByStatus(CarStatus status);

    List<Car> findByColorIgnoreCase(String color);

    List<Car> findByFuelType(FuelType fuelType);

    List<Car> findByPriceBetween(double min, double max);

    List<Car> findByHorsepowerBetween(int minHp, int maxHp);

    boolean existsById(@NonNull Long id);

    boolean existsByBrandIgnoreCase(String brand);

    boolean existsByColorIgnoreCase(String color);

    boolean existsByFuelType(FuelType fuelType);
}
