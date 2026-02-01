package de.ait.javalessonspro.service;

import de.ait.javalessonspro.dto.TestDriveConfirmationEmailRequest;
import de.ait.javalessonspro.dto.TestDriveReminderEmailRequest;
import de.ait.javalessonspro.model.Car;
import de.ait.javalessonspro.model.TestDriveBooking;
import de.ait.javalessonspro.repositories.CarRepository;
import de.ait.javalessonspro.repositories.TestDriveBookingRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 01.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@ExtendWith(MockitoExtension.class)
class TestDriveEmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private CarRepository carRepository;

    @Mock
    private TestDriveBookingRepository testDriveBookingRepository;

    @InjectMocks
    private TestDriveEmailService testDriveEmailService;

    @Captor
    private ArgumentCaptor<TestDriveBooking> bookingCaptor;

    private TestDriveConfirmationEmailRequest confirmationRequest;
    private TestDriveReminderEmailRequest reminderRequest;
    private Car testCar;
    private TestDriveBooking existingBooking;
    private final LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1);

    @BeforeEach
    void setUp() {
        confirmationRequest = TestDriveConfirmationEmailRequest.builder()
                .clientEmail("test@example.com")
                .clientName("John Doe")
                .carId(1L)
                .testDriveDateTime(futureDateTime)
                .dealerAddress("123 Test St")
                .dealerPhone("+1234567890")
                .build();

        reminderRequest = TestDriveReminderEmailRequest.builder()
                .clientEmail("test@example.com")
                .clientName("John Doe")
                .carId(1L)
                .testDriveDateTime(futureDateTime)
                .build();

/*        testCar = Car.builder()
                .id(1L)
                .brand("TestBrand")
                .model("TestModel")
                .productionYear(2023)
                .color("Black")
                .horsepower(200)
                .price(new BigDecimal("50000.00"))
                .mileage(10000L)
                .build();*/

        existingBooking = TestDriveBooking.builder()
                .id(1L)
                .clientEmail("test@example.com")
                .carId(1L)
                .testDriveDateTime(futureDateTime)
                .confirmationId("TD-ABC123")
                .reminderSent(false)
                .build();
    }

    @Test
    void sendConfirmationEmail_Success() throws MessagingException {
        when(testDriveBookingRepository.existsByClientEmailAndCarIdAndTestDriveDateTime(
                any(), any(), any())).thenReturn(false);
        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));
        when(templateEngine.process(eq("test-drive-confirmation-email"), any(Context.class)))
                .thenReturn("<html>Email Content</html>");

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        testDriveEmailService.sendConfirmationEmail(confirmationRequest);

        verify(testDriveBookingRepository).save(bookingCaptor.capture());
        TestDriveBooking savedBooking = bookingCaptor.getValue();

        assertThat(savedBooking.getClientEmail()).isEqualTo("test@example.com");
        assertThat(savedBooking.getCarId()).isEqualTo(1L);
        assertThat(savedBooking.getConfirmationId()).isNotNull();
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendConfirmationEmail_DuplicateBooking_ThrowsException() {
        when(testDriveBookingRepository.existsByClientEmailAndCarIdAndTestDriveDateTime(
                any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() ->
                testDriveEmailService.sendConfirmationEmail(confirmationRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("There is already a test drive record for this time.");
    }

    @Test
    void sendConfirmationEmail_CarNotFound_ThrowsException() {
        when(testDriveBookingRepository.existsByClientEmailAndCarIdAndTestDriveDateTime(
                any(), any(), any())).thenReturn(false);
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                testDriveEmailService.sendConfirmationEmail(confirmationRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Car with id 1 not found");
    }

    @Test
    void sendReminderEmail_Success() throws MessagingException {
        when(testDriveBookingRepository.findByClientEmailAndCarIdAndTestDriveDateTime(
                any(), any(), any())).thenReturn(Optional.of(existingBooking));
        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));
        when(templateEngine.process(eq("test-drive-reminder-email"), any(Context.class)))
                .thenReturn("<html>Reminder Content</html>");

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        testDriveEmailService.sendReminderEmail(reminderRequest);

        assertThat(existingBooking.getReminderSent()).isTrue();
        assertThat(existingBooking.getReminderId()).isNotNull();
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendReminderEmail_BookingNotFound_ThrowsException() {
        when(testDriveBookingRepository.findByClientEmailAndCarIdAndTestDriveDateTime(
                any(), any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                testDriveEmailService.sendReminderEmail(reminderRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No test drive entry found");
    }

    @Test
    void sendReminderEmail_AlreadySent_ThrowsException() {
        existingBooking.setReminderSent(true);
        when(testDriveBookingRepository.findByClientEmailAndCarIdAndTestDriveDateTime(
                any(), any(), any())).thenReturn(Optional.of(existingBooking));

        assertThatThrownBy(() ->
                testDriveEmailService.sendReminderEmail(reminderRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A reminder has already been sent for this entry.");
    }

    @Test
    void sendCancellationEmail_Success() throws MessagingException {
        when(templateEngine.process(eq("test-drive-cancellation-email"), any(Context.class)))
                .thenReturn("<html>Cancellation Content</html>");

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        testDriveEmailService.sendCancellationEmail(existingBooking);

        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_MessagingException_WrappedInRuntimeException() throws MessagingException {
        when(testDriveBookingRepository.existsByClientEmailAndCarIdAndTestDriveDateTime(
                any(), any(), any())).thenReturn(false);
        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));
        when(templateEngine.process(eq("test-drive-confirmation-email"), any(Context.class)))
                .thenReturn("<html>Content</html>");

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MessagingException("SMTP error")).when(javaMailSender).send((MimeMessage) any());

        assertThatThrownBy(() ->
                testDriveEmailService.sendConfirmationEmail(confirmationRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to send confirmation email")
                .hasRootCauseInstanceOf(MessagingException.class);
    }

/*    @Test
    void getTransmissionDisplayName_ValidValues() {
        assertThat(testDriveEmailService.getTransmissionDisplayName(null))
                .isEqualTo("Не указана");
    }*/
}
