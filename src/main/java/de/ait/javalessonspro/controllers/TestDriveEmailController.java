package de.ait.javalessonspro.controllers;

import de.ait.javalessonspro.dto.TestDriveConfirmationEmailRequest;
import de.ait.javalessonspro.dto.TestDriveReminderEmailRequest;
import de.ait.javalessonspro.service.TestDriveEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 31.01.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@Tag(
        name = "Test Drive Email Service",
        description = "Endpoints for sending transactional emails related to test drive bookings"
)
@Slf4j
@RestController
@RequestMapping("/api/email/test-drive")
@RequiredArgsConstructor
public class TestDriveEmailController {

    private final TestDriveEmailService testDriveEmailService;

    @Operation(
            summary = "Send test drive confirmation email",
            description = """
            Sends a confirmation email to the customer after test drive booking.
            
            This endpoint:
            1. Validates the booking request
            2. Saves the booking to the database
            3. Generates a unique confirmation ID
            4. Sends a confirmation email with booking details
            
            **Validation:**
            - All required fields must be provided and valid
            - Email address must be properly formatted
            - Test drive date/time must be in the future
            
            **Response codes:**
            - 202 ACCEPTED — email accepted for processing
            - 400 BAD REQUEST — validation errors in request data
            - 500 INTERNAL SERVER ERROR — email sending failed
            """
    )
    @PostMapping("/confirmation")
    public ResponseEntity<Void> sendConfirmationEmail(
            @Valid @RequestBody TestDriveConfirmationEmailRequest request, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            log.error("Validation errors while sending test drive confirmation email: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().build();
        }

        try {
            testDriveEmailService.sendConfirmationEmail(request);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error while sending test drive confirmation email", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
            summary = "Send test drive reminder email",
            description = """
            Sends a reminder email to the customer before scheduled test drive.
            
            This endpoint sends a reminder email 24 hours before the scheduled test drive time.
            The reminder includes booking details and any important information the customer
            needs to know before their test drive.
            
            **Validation:**
            - Valid booking ID must be provided
            - Test drive must be scheduled for the future
            - Customer email must be valid
            
            **Response codes:**
            - 202 ACCEPTED — reminder email accepted for processing
            - 400 BAD REQUEST — validation errors in request data
            - 500 INTERNAL SERVER ERROR — email sending failed
            """
    )
    @PostMapping("/reminder")
    public ResponseEntity<Void> sendReminderEmail(
            @Valid @RequestBody TestDriveReminderEmailRequest request, BindingResult bindingResult) {

        log.info("Attempting to send reminder for: email={}, carId={}, time={}",
                request.getClientEmail(), request.getCarId(), request.getTestDriveDateTime());

        if (bindingResult.hasErrors()) {
            log.error("Validation errors while sending test drive reminder email: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().build();
        }

        try {
            testDriveEmailService.sendReminderEmail(request);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error while sending test drive reminder email", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
