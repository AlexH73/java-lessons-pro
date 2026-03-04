package de.ait.javalessonspro.repositories;

import de.ait.javalessonspro.enums.CarStatus;
import de.ait.javalessonspro.enums.FuelType;
import de.ait.javalessonspro.model.Car;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {


    Optional<Car> findById(Long id);

    // SELECT * FROM CARS WHERE brand = ?
    List<Car> findByBrandIgnoreCase(String brand);
    Page<Car> findByBrandIgnoreCase(String brand, Pageable pageable);

    List<Car> findByStatus(CarStatus status);
    Page<Car> findByStatus(CarStatus status, Pageable pageable);

    List<Car> findByColorIgnoreCase(String color);
    Page<Car> findByColorIgnoreCase(String color, Pageable pageable);

    List<Car> findByFuelType(FuelType fuelType);
    Page<Car> findByFuelType(FuelType fuelType, Pageable pageable);

    List<Car> findByPriceBetween(BigDecimal min, BigDecimal max);
    Page<Car> findByPriceBetween(int min, int max, Pageable pageable);

    List<Car> findByHorsepowerBetween(int minHp, int maxHp);
    Page<Car> findByHorsepowerBetween(int min, int max, Pageable pageable);

    boolean existsById(@NonNull Long id);

//    boolean existsByBrandIgnoreCase(String brand);
//
//    boolean existsByColorIgnoreCase(String color);
//
//    boolean existsByFuelType(FuelType fuelType);
}
