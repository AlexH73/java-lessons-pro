package de.ait.javalessonspro.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 01.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@Data
public class CancelTestDriveRequest {

    @NotBlank(message = "Booking ID is required")
    private String bookingId;

    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}
