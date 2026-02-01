package de.ait.javalessonspro.repositories;

import de.ait.javalessonspro.model.TestDriveBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 01.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
public interface TestDriveBookingRepository extends JpaRepository<TestDriveBooking, Long> {

    Optional<TestDriveBooking> findByConfirmationId(String confirmationId);

    Optional<TestDriveBooking> findByReminderId(String reminderId);

    Optional<TestDriveBooking> findByClientEmailAndCarIdAndTestDriveDateTime(
            String clientEmail, Long carId, LocalDateTime testDriveDateTime);

    boolean existsByClientEmailAndCarIdAndTestDriveDateTime(
            String clientEmail, Long carId, LocalDateTime testDriveDateTime);

    long countByCarIdAndTestDriveDateTimeBetween(
            Long carId, LocalDateTime start, LocalDateTime end);
}
