package de.ait.javalessonspro.repository;

import de.ait.javalessonspro.enums.CarStatus;
import de.ait.javalessonspro.model.Car;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {

    // SELECT * FROM CARS WHERE brand = ?
    List<Car> findByBrandIgnoreCase(String brand);

    List<Car> findByStatus(CarStatus status);

    boolean existsById(@NonNull Long id);

    boolean existsByBrandIgnoreCase(@NonNull String brand);

    List<Car> findByPriceBetween(int min, int max);
}
