package de.ait.javalessonspro.dto;

import jakarta.validation.constraints.Email;
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
public class TestDriveReminderEmailRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String clientEmail;

    @NotBlank(message = "Client name is required")
    private String clientName;

    @NotNull(message = "Car ID is required")
    private Long carId;

    @NotNull(message = "Test drive date and time is required")
    private LocalDateTime testDriveDateTime;
}
