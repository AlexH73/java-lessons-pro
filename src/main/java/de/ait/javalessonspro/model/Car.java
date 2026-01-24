package de.ait.javalessonspro.model;


import de.ait.javalessonspro.enums.CarStatus;
import de.ait.javalessonspro.enums.FuelType;
import de.ait.javalessonspro.enums.Transmission;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    @NotNull(message = "Year must not be null")
    @Min(value = 1886, message = "Year must be no earlier than 1886")
    @Max(value = 2100, message = "Year must not be in the far future")
    private int productionYear;

    @Min(value = 1, message = "Mileage must be greater than 0")
    private long mileage;

    @NotNull(message = "Price must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price format is invalid")
    private BigDecimal price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is required")
    private CarStatus status;

    @Column(nullable = false)
    @NotBlank(message = "Color must not be empty")
    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;

    @Min(value = 1, message = "Horsepower must be at least 1")
    @Max(value = 1500, message = "Horsepower must be less than 1500")
    private int horsepower;

    @Column(name = "fuel_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Fuel type is required")
    private FuelType fuelType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Transmission is required")
    private Transmission transmission;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean deleted = false;


    public Car(String brand, String model, int productionYear, long mileage,
               BigDecimal price, String status, String color, int horsepower,
               String fuelType, String transmission, LocalDateTime createdAt, LocalDateTime updatedAt, boolean deleted) {

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
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
