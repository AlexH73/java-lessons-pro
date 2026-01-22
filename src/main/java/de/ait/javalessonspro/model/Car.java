package de.ait.javalessonspro.model;


import de.ait.javalessonspro.enums.CarStatus;
import de.ait.javalessonspro.enums.FuelType;
import de.ait.javalessonspro.enums.Transmission;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "cars", indexes = {
        @Index(name = "idx_brand", columnList = "brand"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_price", columnList = "price"),
        @Index(name = "idx_brand_model", columnList = "brand, model"),
        @Index(name = "idx_fuel_type", columnList = "fuel_type")
})
@Getter
@Setter
@NoArgsConstructor

public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Brand must not be empty")
    @Size(min = 2, max = 50, message = "Brand must be between 2 and 50 characters")
    private String brand;

    @Column(nullable = false)
    @NotBlank(message = "Model must not be empty")
    @Size(min = 1, max = 50, message = "Model must be between 1 and 50 characters")
    private String model;

    @Column(name = "production_year")
    @Min(value = 1900, message = "Year must be greater than 1900")
    private int productionYear;

    @Min(value = 1, message = "Mileage must be greater than 0")
    private int mileage;

    @DecimalMin(value = "1.0", message = "Price must be at least 1")
    private double price;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is required")
    private CarStatus status;

    @Column(nullable = false)
    @NotBlank(message = "Color must not be empty")
    private String color;

    @Min(value = 1, message = "Horsepower must be at least 1")
    @Max(value = 1500, message = "Horsepower must be less than 1500")
    private int horsepower;

    @Column(name = "fuel_type")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Fuel type is required")
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Transmission is required")
    private Transmission transmission;


    public Car(String brand, String model, int productionYear, int mileage,
               double price, String status, String color, int horsepower,
               String fuelType, String transmission) {

        this.brand = brand;
        this.model = model;
        this.productionYear = productionYear;
        this.mileage = mileage;
        this.price = price;
        this.status = CarStatus.valueOf(status.toUpperCase());
        this.color = color;
        this.horsepower = horsepower;
        this.fuelType = FuelType.valueOf(fuelType.toUpperCase());
        this.transmission = Transmission.valueOf(transmission.toUpperCase());
    }
}
