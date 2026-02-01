package de.ait.javalessonspro.dto;

import de.ait.javalessonspro.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 01.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@Data
public class TestDriveBookingResponse {
    private Long id;
    private String clientName;
    private String clientEmail;
    private String carBrand;
    private String carModel;
    private LocalDateTime testDriveDateTime;
    private BookingStatus status;
    private String confirmationId;
    private String reminderId;
    private Boolean reminderSent;
    private String cancellationReason;
    private LocalDateTime createdAt;
}
