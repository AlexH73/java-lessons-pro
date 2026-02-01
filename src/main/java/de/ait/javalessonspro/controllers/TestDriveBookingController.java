package de.ait.javalessonspro.controllers;

import de.ait.javalessonspro.dto.CancelTestDriveRequest;
import de.ait.javalessonspro.dto.TestDriveBookingResponse;
import de.ait.javalessonspro.service.TestDriveBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 01.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@Tag(
        name = "Test Drive Booking Management",
        description = "Endpoints for managing test drive bookings, including cancellation and retrieval"
)
@Slf4j
@RestController
@RequestMapping("/api/test-drive")
@RequiredArgsConstructor
public class TestDriveBookingController {

    private final TestDriveBookingService testDriveBookingService;

    @Operation(
            summary = "Cancel test drive booking",
            description = """
            Cancels an existing test drive booking by its confirmation or reminder ID.
            
            **Validation:**
            - Request body must contain valid booking ID and cancellation reason
            - Booking must exist and be in a cancellable state
            
            **Response codes:**
            - 200 OK — booking successfully cancelled
            - 400 BAD REQUEST — validation errors or booking cannot be cancelled
            - 404 NOT FOUND — booking with provided ID does not exist
            - 500 INTERNAL SERVER ERROR — unexpected server error
            """
    )
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelTestDrive(
            @Valid @RequestBody CancelTestDriveRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            log.warn("Invalid cancel request: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        try {
            testDriveBookingService.cancelBooking(request.getBookingId(), request.getReason());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Booking not found: {}", request.getBookingId());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.warn("Cannot cancel booking: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error cancelling booking: {}", request.getBookingId(), e);
            return ResponseEntity.internalServerError().body("Error cancelling booking");
        }
    }

    @Operation(
            summary = "Get test drive booking details",
            description = """
            Retrieves detailed information about a test drive booking by its ID.
            
            The ID can be either a confirmation ID or reminder ID associated with the booking.
            Returns comprehensive booking details including customer information, car details,
            scheduled time, and current booking status.
            
            **Response codes:**
            - 200 OK — booking found and returned
            - 404 NOT FOUND — booking with provided ID does not exist
            """
    )
    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBooking(@PathVariable String bookingId) {
        try {
            TestDriveBookingResponse booking = testDriveBookingService.findBooking(bookingId);
            return ResponseEntity.ok(booking);
        } catch (IllegalArgumentException e) {
            log.warn("Booking not found: {}", bookingId);
            return ResponseEntity.notFound().build();
        }
    }
}
