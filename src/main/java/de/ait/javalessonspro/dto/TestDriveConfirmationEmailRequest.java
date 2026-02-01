package de.ait.javalessonspro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 31.01.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@Builder
@Data
public class TestDriveConfirmationEmailRequest {
    @NotBlank(message = "Client email is required")
    @Email(message = "Invalid email format")
    private String clientEmail;

    @NotBlank(message = "Client name is required")
    private String clientName;

    @NotNull(message = "Car ID is required")
    private Long carId;

    @NotNull(message = "Test drive date/time is required")
    @Future(message = "Test drive date must be in the future")
    private LocalDateTime testDriveDateTime;

    @NotBlank(message = "Dealer address is required")
    private String dealerAddress;

    @NotBlank(message = "Dealer phone is required")
    private String dealerPhone;
}
