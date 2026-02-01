package de.ait.javalessonspro.service;

import de.ait.javalessonspro.dto.TestDriveBookingResponse;
import de.ait.javalessonspro.enums.BookingStatus;
import de.ait.javalessonspro.model.TestDriveBooking;
import de.ait.javalessonspro.repositories.TestDriveBookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 01.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TestDriveBookingService {

    private final TestDriveBookingRepository testDriveBookingRepository;
    private final TestDriveEmailService testDriveEmailService;

    public void cancelBooking(String bookingId, String reason) {
        TestDriveBooking booking = findBookingEntity(bookingId);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        if (booking.getTestDriveDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot cancel past bookings");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        testDriveBookingRepository.save(booking);

        log.info("Booking cancelled - ID: {}, Reason: {}", bookingId, reason);

        // Отправляем email об отмене (опционально)
        try {
            testDriveEmailService.sendCancellationEmail(booking);
        } catch (Exception e) {
            log.error("Failed to send cancellation email for booking: {}", bookingId, e);
            // Не бросаем исключение, т.к. отмена уже выполнена
        }
    }

    public TestDriveBookingResponse findBooking(String bookingId) {
        TestDriveBooking booking = findBookingEntity(bookingId);
        return mapToResponse(booking);
    }

    private TestDriveBooking findBookingEntity(String bookingId) {
        // Сначала ищем по confirmationId
        Optional<TestDriveBooking> booking = testDriveBookingRepository.findByConfirmationId(bookingId);

        // Если не нашли, ищем по reminderId
        if (booking.isEmpty()) {
            booking = testDriveBookingRepository.findByReminderId(bookingId);
        }

        return booking.orElseThrow(() ->
                new IllegalArgumentException("Booking not found: " + bookingId));
    }

    private TestDriveBookingResponse mapToResponse(TestDriveBooking booking) {
        TestDriveBookingResponse response = new TestDriveBookingResponse();
        response.setId(booking.getId());
        response.setClientName(booking.getClientName());
        response.setClientEmail(booking.getClientEmail());
        response.setCarBrand(booking.getCarBrand());
        response.setCarModel(booking.getCarModel());
        response.setTestDriveDateTime(booking.getTestDriveDateTime());
        response.setStatus(booking.getStatus());
        response.setConfirmationId(booking.getConfirmationId());
        response.setReminderId(booking.getReminderId());
        response.setReminderSent(booking.getReminderSent());
        response.setCreatedAt(booking.getCreatedAt());
        response.setCancellationReason(booking.getCancellationReason());
        return response;
    }
}
