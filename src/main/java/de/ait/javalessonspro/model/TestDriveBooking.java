package de.ait.javalessonspro.model;

import de.ait.javalessonspro.enums.BookingStatus;
import de.ait.javalessonspro.enums.Transmission;
import de.ait.javalessonspro.enums.FuelType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 01.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "test_drive_bookings")
public class TestDriveBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_email", nullable = false)
    private String clientEmail;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "car_id", nullable = false)
    private Long carId;

    @Column(name = "car_brand", nullable = false)
    private String carBrand;

    @Column(name = "car_model", nullable = false)
    private String carModel;

    @Column(name = "car_year", nullable = false)
    private Integer carYear;

    @Column(name = "car_color", nullable = false)
    private String carColor;

    @Column(name = "car_horsepower", nullable = false)
    private Integer carHorsepower;

    @Enumerated(EnumType.STRING)
    @Column(name = "car_transmission", nullable = false)
    private Transmission carTransmission;

    @Enumerated(EnumType.STRING)
    @Column(name = "car_fuel_type", nullable = false)
    private FuelType carFuelType;

    @Column(name = "car_mileage", nullable = false)
    private Long carMileage;

    @Column(name="car_price", nullable = false)
    private String carPrice;

    @Column(nullable = false, name = "test_drive_date_time")
    private LocalDateTime testDriveDateTime;

    @Column(name="dealer_address", nullable = false)
    private String dealerAddress;

    @Column(name="dealer_phone", nullable = false)
    private String dealerPhone;

    @Column(name="confirmation_id", unique = true)
    private String confirmationId;

    @Column(name="reminder_id", unique = true)
    private String reminderId;

    @Column(name="reminder_sent")
    private Boolean reminderSent = false;

    @CreationTimestamp
    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(name = "cancellation_reason")
    private String cancellationReason;
}
