package de.ait.javalessonspro.service;

import de.ait.javalessonspro.dto.TestDriveConfirmationEmailRequest;
import de.ait.javalessonspro.dto.TestDriveReminderEmailRequest;
import de.ait.javalessonspro.enums.BookingStatus;
import de.ait.javalessonspro.model.TestDriveBooking;
import de.ait.javalessonspro.enums.Transmission;
import de.ait.javalessonspro.enums.FuelType;
import de.ait.javalessonspro.model.Car;
import de.ait.javalessonspro.repositories.CarRepository;
import de.ait.javalessonspro.repositories.TestDriveBookingRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 31.01.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@Service
@Slf4j
@Transactional
public class TestDriveEmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final CarRepository carRepository;
    private final TestDriveBookingRepository testDriveBookingRepository;

    public TestDriveEmailService(JavaMailSender javaMailSender,
                                 TemplateEngine templateEngine,
                                 CarRepository carRepository,
                                 TestDriveBookingRepository testDriveBookingRepository) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.carRepository = carRepository;
        this.testDriveBookingRepository = testDriveBookingRepository;
    }

    @Value("${app.mail.from}")
    private String emailSender;

    @Value("${app.public.base-url}")
    private String cancellationUrl;

    @Value("${app.dealership.name: AIT Gr.59 API}")
    private String dealerShipName;

    public void sendConfirmationEmail(TestDriveConfirmationEmailRequest request) {
        try {
            if (testDriveBookingRepository.existsByClientEmailAndCarIdAndTestDriveDateTime(
                    request.getClientEmail(), request.getCarId(), request.getTestDriveDateTime())) {
                throw new IllegalArgumentException("There is already a test drive record for this time.");
            }

            Car car = carRepository.findById(request.getCarId())
                    .orElseThrow(() -> new IllegalArgumentException("Car with id " + request.getCarId() + " not found"));


            String confirmationId = generateConfirmationId();

            TestDriveBooking booking = new TestDriveBooking();
            booking.setClientEmail(request.getClientEmail());
            booking.setClientName(request.getClientName());
            booking.setCarId(car.getId());
            booking.setCarBrand(car.getBrand());
            booking.setCarModel(car.getModel());
            booking.setCarYear(car.getProductionYear());
            booking.setCarColor(car.getColor());
            booking.setCarHorsepower(car.getHorsepower());
            booking.setCarTransmission(car.getTransmission());
            booking.setCarFuelType(car.getFuelType());
            booking.setCarMileage(car.getMileage());
            booking.setCarPrice(car.getPrice() != null ?
                    car.getPrice().setScale(2, RoundingMode.HALF_UP).toString() + " €" : "Цена не указана");
            booking.setTestDriveDateTime(request.getTestDriveDateTime());
            booking.setDealerAddress(request.getDealerAddress());
            booking.setDealerPhone(request.getDealerPhone());
            booking.setConfirmationId(confirmationId);
            booking.setStatus(BookingStatus.CONFIRMED);

            testDriveBookingRepository.save(booking);


            log.info("Test drive booking saved to DB - ID: {}, Confirmation: {}",
                    booking.getId(), confirmationId);

            Map<String, Object> templateData = prepareConfirmationTemplateData(request, car, confirmationId);
            String htmlContent = generateHtml("test-drive-confirmation-email", templateData);

            sendEmail(
                    request.getClientEmail(),
                    "Подтверждение записи на тест-драйв - " + car.getBrand() + " " + car.getModel(),
                    htmlContent
            );

            log.info("Test drive confirmation email sent successfully to: {}, Booking ID: {}, Confirmation: {}",
                    request.getClientEmail(), booking.getId(), confirmationId);

        } catch (MessagingException e) {
            log.error("Error sending email", e);
            throw new RuntimeException("Failed to send confirmation email", e);
        } catch (Exception exception) {
            log.error("Error sending test drive confirmation email to: {}",
                    request.getClientEmail(), exception);
            throw exception;
        }
    }

    public void sendReminderEmail(TestDriveReminderEmailRequest request) {
        try {
            // Ищем существующую запись
            TestDriveBooking booking = testDriveBookingRepository
                    .findByClientEmailAndCarIdAndTestDriveDateTime(
                            request.getClientEmail(),
                            request.getCarId(),
                            request.getTestDriveDateTime())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No test drive entry found for email: " + request.getClientEmail() +
                                    ", car: " + request.getCarId() +
                                    ", time: " + request.getTestDriveDateTime()));

            if (Boolean.TRUE.equals(booking.getReminderSent())) {
                log.warn("Reminder already sent for booking ID: {}", booking.getId());
                throw new IllegalArgumentException("A reminder has already been sent for this entry.");
            }

            Car car = carRepository.findById(request.getCarId())
                    .orElseThrow(() -> new IllegalArgumentException("Car with id " + request.getCarId() + " not found"));

            String reminderId = generateReminderId();
            booking.setReminderId(reminderId);
            booking.setReminderSent(true);
            booking.setStatus(BookingStatus.REMINDER_SENT);

            testDriveBookingRepository.save(booking);

            Map<String, Object> templateData = prepareReminderTemplateData(request, car, reminderId);
            String htmlContent = generateHtml("test-drive-reminder-email", templateData);

            sendEmail(
                    request.getClientEmail(),
                    "Напоминание о тест-драйве - " + car.getBrand() + " " + car.getModel(),
                    htmlContent
            );

            log.info("Test drive reminder email sent successfully to: {}, Booking ID: {}, Reminder: {}",
                    request.getClientEmail(), booking.getId(), reminderId);
        } catch (MessagingException e) {
            log.error("Error sending email", e);
            throw new RuntimeException("Failed to send reminder email", e);
        } catch (Exception exception) {
            log.error("Error sending test drive reminder email to: {}",
                    request.getClientEmail(), exception);
            throw exception;
        }
    }

    private Map<String, Object> prepareConfirmationTemplateData(
            TestDriveConfirmationEmailRequest request, Car car, String confirmationId) {

        Map<String, Object> data = new HashMap<>();
        data.put("clientName", request.getClientName());
        data.put("carBrand", car.getBrand());
        data.put("carModel", car.getModel());
        data.put("carYear", car.getProductionYear());
        data.put("carMileage", car.getMileage() + " км");
        data.put("carColor", car.getColor());
        data.put("carEngine", car.getHorsepower() + " л.с.");
        data.put("carPrice", car.getPrice() != null ?
                car.getPrice().setScale(2, RoundingMode.HALF_UP).toString() + " €" : "Цена не указана");
        data.put("carTransmission", getTransmissionDisplayName(car.getTransmission()));
        data.put("carFuelType", getFuelTypeDisplayName(car.getFuelType()));
        data.put("testDriveDateTime", formatDateTime(request.getTestDriveDateTime()));
        String carImageUrl = "data:image/png;base64,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";
        data.put("carImageUrl", carImageUrl);
        data.put("dealerName", dealerShipName);
        data.put("dealerAddress", request.getDealerAddress());
        data.put("dealerPhone", request.getDealerPhone());
        data.put("confirmationId", confirmationId);
        data.put("cancellationUrl", cancellationUrl);

        return data;
    }

    private Map<String, Object> prepareReminderTemplateData(
            TestDriveReminderEmailRequest request,
            Car car,
            String reminderId) {

        Map<String, Object> data = new HashMap<>();
        data.put("clientName", request.getClientName());
        data.put("carBrand", car.getBrand());
        data.put("carModel", car.getModel());
        data.put("carYear", car.getProductionYear());
        data.put("testDriveDateTime", formatDateTime(request.getTestDriveDateTime()));
        String carImageUrl = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAALQAAAC0CAIAAACyr5FlAAAAA3NCSVQICAjb4U/gAAAgAElEQVR4nO19d3wd1Zn2KXNm5t65TVdX3ZJsS+42LmCMOxhsIARIQrGBJBCSzabtft+SSrIhS/IlsIGElN0NZKlJ6IZgY4qNGwFsbMuWLctdsiVbsurtZeo55/vjSNdyuRBkA0l+8yQ/cSXPnTlzzjNvP+/AWDQKXLg4E9DHPQAXf7twyeGiIFxyuCgIlxwuCsIlh4uCcMnhoiBccrgoCJccLgrCJYeLgnDJ4aIgXHK4KAiXHC4KwiWHi4JwyeGiIFxyuCgIlxwuCsIlh4uCcMnhoiBccrgoCJccLgrCJYeLgnDJ4aIgXHK4KAiXHC4KwiWHi4JwyeGiIFxyuCgIlxwuCsIlh4uCcMnhoiBccrgoCJccLgrCJYeLgnDJ4aIgXHK4KAiXHC4KwiWHi4JwyeGiIFxyuCgIlxwuCsIlh4uCcMnhoiBccrgoCJccLgrCJYeLgnDJ4aIgXHK4KAiXHC4KwiWHi4JwyeGiIFxyuCgIlxwuCkL6uAdwTgHhwH/Pxck45+fiNH/H+LskB0IIwAEAzhljYiE55+IDG/x16Lc45wACwAEHAAIA80wa/CC+AyEUB0iSBCHknA8cIC4nzjN4JfH5I7vxjxh/q+SAEHA+VBJgCWOEEcaWaSYSSdM0stmsoRvZbLY/2p/L6aZppFLpbDaTy+VisZih66ZpJVJJxhiAHDCQ03MQQAABhghxCCDECDIAQqEgYxwAjhHUvB6JECJJvkBw+owZwVDINEzVoyKEVEVVVEWWFc3rVVVVUVWfz+f1ejnnlDHqOIKR/0hcgX9b75Ud8jRDCBBEApZltbe3t7a0dHf3RKPRaDRmWWYsFstkMggiAAFCiBDCOccYAwAkSQIQQAAhQgBwzjiAEEAOOASAQwDzwoYxzjgTgoQxbjsGRog6DEBomrptUywhzjjggHOKkCRJOFQU9ng8Xk0LBoPhoqKS0tKampr6+vpgMEgpZXxAlP0DsORvhhwQAs4RQoRItu2kUqlMJtty6FD70aNrVq95+ZWXp045LxIpIZIkEUlVFA6B5tUYYwghoUMcx4EAUkY555ZpAgAgQuKPAAKMECGyRCRCiCzLhBAiyx5VlYjk8XgABxBBjLFH9VDGMEaWZTkOBcJ8gcBxHNuyKaW2bdu2bVmWZVm2beuGoef0VDIZj8WKSyI3Lr1x6tSpZWVl4eJiCIBt2x/vpJ4l/ibIASGUJElRlO7u7r179xzYf2DP3n2tLS0XXHBBZWVldU11KFSkKLJpmpZlJRKJvXv3bW9oMC2zekS1aZqEENu2JUnSNC1cXKx5PWXl5X6f36t5i4qKNK8WDAYxkTA68T/AAWeUccA4hQDZtmUYBqWUMW7bFmc8k8syKsAA4JRSy7Zsy7Fty7Edh1LHtoXEyum6aZipZDKdTguNVlZeNmPatPOmTpty3pRsLsco/bgneJj4WMkBIcYYcK7renNz8yurVjU17ph03tSJEydOmDixpromk800Nzd3dXVFo1HTNH0+H4Soory8vr5e9Xqad+/eumVrNptFCKmq+m/fvGPsmDGWbQMAKKWO7ZiWaehGOpOOx2LZbDaVSmWzuXQ6lcvl0um0ZVq2bQvCMcYcx2GMMcYAAEIzYIwYG7B2EUKcc0mSGGMSlhhnAAAEIYAIAI4lCQozGQAAgGEYzLEZdc6/cNaNN94YKS1FCFHH+djmebj4OMgBIQAAQSjL8rFjx7Zv3/7CC8uzeu7Ky6+cPn0659w0zUOHDjU3Nx9tP2qZJmXM4/EoihIKhQgh1KGZTLp+7Njrrr/Op2l3/fAuAIBt2bd/8fZFl136zNPPNO3amUql0+l0Jp0eUDoQQAgRQhhjYcQI00QAY5z3axBCEELGGAAQoRMGUN4PEmaN+Cz+PuS2IKV0kCIQAG5k04aR+8wNy+bNn1dRUWla1t+XIfKRkwNCBCGEUNf1Z595duu2rQihyy67rK6uvr+/v6lp19Fjx5KJJHMcWVEIIYSQTCaDMZYkCQAgHmKEUHd3d7g4/J3vfFfTvHf98C5JknK57E/vuUcm8ne/822ZyByAoQw4Bacs6jm/SwYgAABxzjnTsymvpn36uhsWL1kiFNU5v9yHBPzd7373o7weIUTX9c2bN3/+c7eHi4vmL5g/68ILk8nkH/7wx60N21KplIgcaD4fAMDn8ymKYpqmYAYYXEjOuaqquq6vXPHSDTfcWDty5FtvvYUgam9ru/qaqyMlpWvWrAmFQpTSQgsPh+BDuU+IIYQAQgCBJEmSJK1d/Vpff7Syqqo4Evl7UTEfHTkgQl6Pp62t7Xf//d+vvb76ti/cOnr0qJZDLZve2bR7127Vo/p9PgThoAPCDcOor6+Px+OO45y+hBBCjDEhUjqVvuqTV+3atSuTzfT29EZKIosWLdq0eXMymSSEfCzk4AAAJA0E2hDECFuWVVJa0tba+u7mTeXlFTU1NfzvIQL7EZGDyETP6StXrLjvnp/Vjhp98cUL9+8/sGHdhlQqzRjzeD2SJFGHYoQlSRL2oE/zzZk7Z8uWLbIsn35Coe8VRW3e3VxXX3fxokveeGMtkeWGbdvmzJt70axZb/3lLYjQUGPi1K8DDgCA5ybUfvLYAAADJ2eAMsqorCimZcmKB0H4wnPPHu/umThxot/v/xtXMR8+OSBUFKWzs/Oxxx7buGHjvAULOedbt25LJpMej0dYiACAfJRaLGcqmbzplpt7enr279vv8XgKnZsQghDcs3ff0qVL0+lUS2sLBzwei33yk1c7jtPY2CjLsjA/30PFfCg3DQAEXHwSPBYhf8ZhaXlFa2vL7uY91dXV5RXl1Pnb5ceHSw6EMYZw3bp19/38PtM0x4wZ09HR0dbeXlFRTiRiGMYpD/TAtxDSNO36669/5ulnHMc5o+QAgw4nQqi/vz8UCi68+OK3336bELKjsXHihImzZs3a8u674hKMsfcwTj9KQAgdx9E0LZvJvPrKq47jTJ069RSv528HH2LKnhCSTqX/+Mc//PrXv4pEIl6v9/Dhw7lczqdp1SOq+/r6Ci1YNpNZtGgRY7ynu6cQM8CgpuCcl0Qir732GpGkJYsXJ5PJ0tLSF15YTgi5cdnSaH8/AAAiJAIYHzuEJ8wYI4SUlZUtf375o48+mk6n8xY3EGbs3wY+HHJAKMvy8ePHf/yTu9/YsK6yaoRpmplMVkQCxo4bqxu6oiin+5MYYwihbhjTZ8zo7urCGBNC3u9SkFLW19+/devWRYsuhRCqinrw0KE33lgza9aseQvmZzIZfCb5dPJZTozhlCGd/ndeAH/NxAhpJ07IGCsvL9+4YeNP/99P4vE4IQQhhMX/McaS9FHqwTPi3JMDIiRhvGnTpju/f2cylfR6NIwQgggABgDQc7np06cfbT96+oQyxkTaora2tra25sDBg4qqvM+1IAQAQAhkWX7hxRcJIctuXNoX7QsFg48/8US0P3r99Tf4fL5sLvceEw2F03mmA/56dyYfKHtviCAbGLSxOOd+vz8eT9757W8eOHDQMIydO3e+/vrqHdu3tx46pOv6x6sNz2nKHkIEIaXszy+++OKLz3k8PlXx2I4DAGCcAQ4M3Zg3f75hmNH+fhHJyPMjH5zOZrLTL50OITza3i5m8301AgecEJJKpjZv3jR3/rwVL6+wHQdh/Oqrr/zTl78888ILN6xfz2W50DIXeuiH/v19g2Z/PY3y0dX8T0VRbIf/z//8V1VV1fatWzxezWEsl0kvueLK27/4JfTxWSTniBwQEklilOmGvv/A/q7OzsmTpsVisWw2RyTCKFNVxTBMAMHV11x994/+A0BoWZa4Z/EwiSfJtm2E4OLFixPJ5NFjR2Uiv6+XIVbFYVRRlD+/9NK8efMuX3LFy6+87NO0N954Y968eTfffPOa1atP1Oyc+v3B0qDBs4HB9RsaMh+s7WEiGJ/na14kvEdABQzG74deAgzYTBCJtC+ClmUdaTsSLA5jLFHm+IP+A/v3pZLJoqKiv2NyQAgVRdm7d++ePXsOHz7MKJOIpKoqQiibzSqKIstEkkhlZXFNbU0qlZoydSpCiFKHMZbL6Yl4PKvnLMPMZLPJZGLatGllZWVNu5qOtrWFiyPQHsjPCXNEpEjEegiR6zgOQggCoKpqLBo9dKhl+vRpzz37rN/rQwitfeONr3zta5+57jMr/rzC5/eJLzLGHCrSbNyyTM6BiDcQiTjUARBghDFCEAKiKIQQmchEIgACn6apHpVS5vV6FUI4AEQmHo+HEJkzBpEYiNBSiDoOlrBjOwgjzrmu6+KfTMO0bZtzbhimZRmGaTLGHduijFmmCaAkIQlBzBnDmPx9qxWIEILw+eeff+rppzWPV5iQRUVFkUhkzpw5siIfPHBw3769fX196XS6p7dn185diqoUhYpCRaFAMCjLstfrDQWDmqaZlsUZi0RKsIQDwcDnPn9rKp0WyfRcNqvrhmEYuqGbhmGZFhvU2ZIkCdVDCAccbNiw/qtf+1pFRXk2m/NpvrVvrJu/YMFVV13V0NCQSqZ8Pp/Pp6kej8fj9Xq9iqoEAwFN01RVlWUZS5JP01RVVWRF9aiEEIgQhBBBKBEiLkcpBXwgx+Y4DqUOpdQ0TUM3LNuybcc0Tce2OeCmaXIOTNNwHMoZIxLhgHPGEUKSJTHOZEXWdQkhzBgzMXJshzNu2zYAkDNmGObYceNKS0sN0zwx2xACoWU+Ellydok3CDFCGzZsfPCh35VEStFgvNG2bepQyzKnnz9j6tSpmqa1tbWvWbPGsWyP1wMAYIwZugElxBjjgGOEFKJ4vN5IpLg4ElFVtay0tChU5PP7FEUlRFJkRQhzITYsy7ZMUzcM0zTj8VgsFkslU6lUUteNzs6Oe3/+801vv7P8+eUer8c0zOqa6nvuvSeRSOq67vV6vR4PliTKKJHI6QZvMpHo6++P9kfjiXgqkUimUpZtU8fRDV3P6rppmIaRzWYty87pOc4YdZhj2w51hFADoq4UAAljJvK3jCOMEEKinkgYJghjhBB1qJBSQrQIoSh+Ms5sy66oqFh207KqyqpwcVg8Oclkikg4HIlQx3EcR+i84S/f+y7v2ZADYRyLxb75zW8G/H5KGTzZWEMIZdIZiUhTp02dP29+SWnJn//8521btwEAhNJBGDHOh9qblmVRxiAfyEvYti3LsurxaF6vP+DXNE3TtOJwsebTPKonEAz4/X6/3y/EgCzLDnUM3QiFgh0dHf/2f/8tGAyKMMmtt902YcJ4x3HS6UxX1/FoNNrf1x9PJorD4WAwVFFRXlJSUlZW5vf7KWO2bRu6nkwmk6nU4dbWI0eOHDp48FhHp2mZHo9HlmV5iG1LsDTU4BCKjwMunhLhteZt6jy58zUiECKMB6I1eeRLAizLtKldWVG5aNGl8+fPV1W1vb39xRdf1HzaBTPOnzptGsaYCu344URxzoIcEKqK8uSTT7696R3TtAjCkoQZ5ZTRfFWEEMW5XC6bzVx66WWf/dxne/t6V7+2unn37kQyhTAkg3MtZk3EuRGElmkhhPJ5lnx4gHNuGAZjjFJqmZbjOBxwVVFlRQ6FQsFgsLKqKhgMGIbRsK0hHA4HAgHNpyXicSLLRaGiSEmkpKTE5/MVFRVpmpZOp+OJxOHDh7du3bpnT/OIyhEXXHjh2Pr6EdXVpWWlfr8/EAgAAADnPT09R9raWlpaWlta9uzdaxiG1+tVFRVBODQ/IpZ/6L2fYqgySuGQoMtQ+xcMcXkG/F6MKHMARIlkrK2t7ad3/3T2nDmhUOjNN9986MEHm7a+s/TWL15xxSdGjqwNFhXZgwb+OcTwyYEQSiaTjzzyCKXs0KGDikQQxoVGxxjLZrPlFeXLbrpp8uTJjm3vbGzcvmPHnr17e3t7EUQMcL/PJxEJQUQkwhkTMyumW9hlgzoI54MKA08q547jUEo559ShlFGMsaqqAABh+omg5KCVQBFC/kCgpqa6rr5+0sRJdXV1qkft7Ox86y9vPfmnP/VHoyNGVFVWVpaVl4+pH1NTU10/ZkxlZaUIx6XT6c6OzgMH9q96edWBA/tDoaJQKAQAoJRKhADOMcaWZQ01JOHJtULgZD/2vcEg54O5wf7evimTp9x2223jxo3r6Oj40Y9+FO3rA5CPHzfh4ksuXrDwEgjPcdXqcMkBoSxJ6zdsONLWZlnWju07CMJCGZzxcFFBjjHuOn585KhRc+fOXbBwQXlFBUY4Ho/19vZ2dnb29vZ2dXUnk8l4PJZMJC3LGigYhxAjDBGUTgQNB8q08kXkQvCc8iAOjhTml0cUCjHOGWeMMdM0TdOUiTx33pxrrr5m5KhR2Wy2oaHhjTVr9uzdK0KWyWRKlklJJHLBBTNnzJgxZkx9UTgsot2HDh3asvndTZs2HT9+XNM0MTzHcYTAGzqGU5jxgUKffCDJywmW0ulMNpv96le/smjRolQq9Yv7f9Hd280cO5tKhosjX/n6v0yaNMlxnHMlQoYvOSRCtjc0lJSUrF+/YcPGDSF/gHPO3nNYg6rUsiwLcDB2/LiJEyeMGze+vKK8vLycSBJlzLKsTDqdzmQM3YhG+xPxRDKZ7O3r7evrSyYSmUzWNAzDNG3bZowRScKSJFK7EAAsSWJhRLUHY0wE1k4JYUEExTgRRowyzrlpmoyyuXPnXHfd9bUjaxPx+IYNG1588cV0Nhvw+0X5lmVbtmUH/P45c+dOmDBh3LhxVVVVAICenp69zXv++Mc/dnd3h4IhWSanTgEf2P40NEL6VybbhpKIUkoIMU0zk0kvu/nm66+/vr29/evf+EYkEiYI5bLZ/p6uO+/6yUWzZ1N6bvgxfHKIJ6Crq+uXDzyQTqWIdFpljUhYgBNPs7DLMMaUMoyRaVq2YzkO9ft8JWWlZaWlo0aNqq2tjZSURIqLfT7f0E1movrXMsxUJp1Jpw3DyOZy6XQ6Fo2mksmcbuh6LplMGrphmqZpW45lm5bFHAdAyBlXPeqQMAkAgEMExbYp23YkIjFKDcMkMrl00aU33XST5tM6OzsfevChPXv3eL1eOLC5DbABo4eGi8Ljxo2bddGsmTNnejUtnUrv3Nm4fPnyY0c7VI+KJSymiDMm6knFdKCB7TRnkHBnnuQTHzmWiJAKEMDeWPTLX/riVVdd9fbbb99338+LIxEIOOQgk0xccdXVS2++mTF29o7MMMkBEUolk7/85QOHDhzUfJrf7zdN85SITZ4rZ0wuDAXnnFFmWZau52zbQQgVR4pLSkpqR9aOGDEiEikJBPwBf8CjeRVF0byaoipnlMyWaRqm6diOZVuGbliWZZqGbhimYfzlzb/s2bMnH0kTUyzWCSNkWpbwERxGU6lkXV39P/3TlyZPnmJb1oYNG3/9q1+FgkGJkLxe4IxzzkzbTqaSRaGiy6+4fP78+TXV1Y7jvPXWWy+9tKLzeGcgEEAI5SO8IocMz2K9Tpi3HDiMMkbvuOOOMWPH/ue99x5ua1MV1bZty8yVlZf/+O6fDK2aHjaGSQ6EUE9Pz9e/9vWqqqq8Lyok+YlTD0pOYc+fkRZ55K1Lx3Ecx8EYQwBNy7QsizOmKIrP5/cH/B6Pp7i42O/3e73e4kixpvmCwUAwGAwEAl6v16tp+Us4tiMRCQDg2PaKlSvXvbE2nU4PFeZDbRFxXVmWIYIejzedSQcCgZtvumnuvHkAgN1NTb964FepVMrj8YixgcF4FOOMUsoY44xPOW/K4ssumzZ9umEYf/rjH1eufLmoKKSoqpiTsyfH0ImlnJmmqXm03/z212+/887jjz1OZGJZVk1N9b/+y7+Gw2GEkGXZnJ+ViztccmB8tL39e9/9XlFRkfA5hbNwSgZB6AIhzN/3nPkjMcbCrRDrJ/xb27bFOR3bhggxyrLZzICusW3KGJFlj0ctCoYM0/jBD34wfcYMwMHOnY2/+92Dx9qPFkeK80EIzk54OoIWoVAIS1J3V5fq9di27VE9nDPHccrKykeMqJowYYJhGFve3eI4DkQwHo+nU2lhASAJI4Rsy5IVxTCM7p6eitLSpcuWzZ8/PxaLPfb4E817mv0+X34Gzgk5AAcAQQ5BX2/vZ2+5ZdEll/z7XXclEolQqOjn/3mvpmnNzc0vvvDCN7/1LYTx2SiXYVaCYUnat2/fti3bNE0TfxH7BE3TFFwRKy1qg//KBEGeWELSDKWUYAzGWMKYyDLGkiwTzaf5fL5AIBAsCgVDwWAwIElSKp2+8MILr77mGozwhg3rf/ub35qG4fP58pEoIhExtrziF+McP358MBQ8cPCg5vWK+jEOQCwea2lt3bat4cjhIwghWZFlWRk5cuS48ePKK8ohhPFkIpPJMM5FiicSiVi2vaOxsaGhQdO0Sy65uDhc3NTUJPbuIog451D4WiIWgqC436EPzynqQDhi+QPEyBmjnHOv1xvt71+4cGHz7mbDNL733e+UlZU1NTXdf/8v9jbtnD7j/KoRI86mTHWYkkOW5XXr1j3y8CN+v1+WZV3XK6sqKyoqAwH/sWPHWltaEMIQQsY5kaS8KP6QwDhHGHHGbMcpLSn54V13RSKR119//X8f+r0iK7ZjAwAcx5YkoijK6TKMc4AQ5Jxfc+01CKGG7dvb2tqy2SznnBAivFYEYN5gEgqouqa6qqqqtKQ0k8swyo4f7+ru6e7u7hYyz+PxZDLZqsqKSy65hBB548YN8UQCIQQ5dxyaN78YZWLz3ImWAqd5fHwwi5TPOEIAAEaUUohQd3f3f/32t42NjVOnTp163nm7du36+f2/xAhAAMpLS3/wwx8SWR52/HT4iTdxLwghwzBKSkoqK6uOHD4MABg/YUJFecXatWt9Ph8cTBkM+yp/DQZmDYBcLvfJq6+ORCI7tm9/9NFHiSLncrnx48fPnTvXH/D3dPe8s+md9iNt/kAgPyQx6eLZffKPT954042f++xnJUnq7+9PJBLHj3dFo/2JRDKdTOX0XC6btWyHcZbJZKL9/U27mrxe72WLFy9btlRWlFgs1tvbG4/FYrFYY2NjR0fH0aNHH3zoofHjx/t9fsfqKykrVSRSWlaGBjffyYqMEAIcapp3wLtGUPV48roZMM4BRxBxAIQBAQf8P0gZlTC2bTudTl++ZEkwGGSMLV/+Ynd3j0LIN77x1Xg0unPnzjlz51pDUncfCMOXHOvXr3v4fx8JhUKMsQkTJrS2thqGAQBwHHv2nDmKoqxftz4YDJqmCU7bGXCOAQHljFHm03z33Xef6lG//tWvxZIJDNGNN94wadKk5uY9jmNXj6geM2ZMQ0PDH/7wB7HRSHx7QGhzTjkzbbuspOSi2bNnnn9BdW0NIYSL3h2MGaaZy2Ydx7EsW1Qsq6qiaT6P1yMTmTHGOMVYemPNG93dXRMnTgyFikRwr729vbamZsb5MwLBoNiNOSgmThjoZ56fQSLkPeEzglIqUrUYoZ6e7r+89faiSy6Jx+P33Xf/9Knn3f6lL50elPtr5/Xs1MrDwUAoGAoqqtpx9JhEJJEtgxBe9cmrDuw/cOjgQSLL58Steg9QSrGEU6n05Zcv+cpXv/ri8hcefuRhRVFuu/U2VVUffvhh0zAY4xDC6trqb33rW/F4/De//i2lTl6qCYthIHjKWDqdyqTTxZGSiy9eOHbM2Lr6ukAgWFQUwtJJglb4w8JKRQjZtr1m9ZoHf/c7iZBMJqPI8tXXXDNhwvji4uJEIrFixcpdO3cGA4FQqCiVTgIOMZYYo1wIiEHJDyH0eDz52mlVUcPFYV03AgG/1+sVbUiQhAHniqJgjAOB4Jj6+rr6OsdxLMsKBAKKouzatevf//2uYCCQTiX+389+Nmr06OHtsRsmOQghW7du+eUvHwiHwpGSiGlayWRK9GAS0zRmzJjzpp730IMP1dXVpdNpMhgk+DAgtHI8Fv/2d749dfq0u//j7iOHD89fsGDsmDEP/u53wVBIPDrC5wQQ/PjuH0djsW/dcUd1TY2iKCKEelLWAwDGOGXUsR3DNEKhUHl5+ajRo0aOHDViRFV1dbXm851S+dxxrGP9+nUrXnrJ5/MLm1J4WJlMBgDAGQ8EAx6PR7hXCCIw0H/qxC2c8dbEmIX1mu8AIP5JhLkchzqUfv/7d866aFZXV9dTTz0dCAbeeesdhKAkSdFodNlNyz716U8PT7MMkxwY487jx//vN/61rLy8rLysu7sHDWwvGxh6NBr9wu1fOHLkyLub3g2Ggh/q1i6xtIZhPPjQg3ua99z3859X19Ze9Ykrn3nmGYyl/EMpdIHjOJyxX//2N+vXrX/22WfFxqrTzyluR5jSIj5rmWYqndZzOa/XW1pWVlVVFQj4PV4vY6yvr2/L5neDwWAwFBLmSz47L9J++dUdhnodwpvTNQsHAGCMu4533f/L+8ePH//EE0/8+YUXw8XF4p8ppZl05rnlz+VyuQ96XTBsg5RzrhBSXBKhnFm2TSmTsMShyK5wAEBRUdHq1auX3rh0Z+NO27Y/VJsDQkgpDfgDRJa379jOARw9evTRY8cc20EI88HSZcEMjHFW15/805O33nrr1q1be3p6Tk+sgyGRfj5YLaD5fD6fT9RPGIZx8ODBfBKHcz6iulpMi2gmp6qqOExEgPKR+/e4hfzn0xOH73HrAADbtoOh4JtvvlldXT1nzpwXlr8gblmkAHt7e/v7+30+3zB27gxzzTgAmEiRkgiC0LYshCAHgFEmy3K+V0IykUymklPOmyJmRzxPHygh+VdCRMlCRaFcNtvY2ChJuKysrHFHo9jLxAfricRPscyrXl51vLvrpptvSqdSYDD+NnBrgwAACENEURShmPjg5bxej8/nE6VGqqoK8TPQlAxhsWPPMAwRMh5aQT0UQy8HTuZEfjxDj8z/esrXJUkiEmlubs5kMqqqBoNBOCgmKWNezXvoUMsp1tJfieE/0BKWFFmhlNq2wxgHAHDAA4FAOElsotQAABuZSURBVBxmjKmqijHu6OgYO3asaZp5AfthWB4iOMs46+jo6O/rr66ujkajsVhM5NCF1qCU+ny+UCgkKh6KiopeeXlVfX392HHjhq5E/icfUmSU1/dDPpxY0cGoFMvHcCGEqqqKTlT5QeZD9WzwlKes+il7hvOX45wDCBFE+V+H0lcclslmJk+e4vP5U6lUJp0Bg5kBjJBHVVOp5PAk9zDJAQGwbau7u4sQseUVUkot2wkEA5GSiOM4I0eO9Hq9O7Y3jh41SthuA/fGzj05BO1y2dyxY8cghEVFRYcOHhQFgoQQcV3HoeUV5cWRYkYZhFBW5Obm5p6eniuvvDKRSOT32ucXRsgMwSTTNKnjiCIBOKS+6xRBPTTCixAqr6g4PZvDORf1yUIV5oWZZVmO49iWRSkVn03TpA51HGegQZ090KQql8tlMhnDMEzT1HU9m80mEwlN06644nJN8776yqsejzrAQsZs266prYlGo86wioCGaXMghLq7e/v7+0tLyzDGGGFMJMlEEMCKiorOjs6a2po9e/boun68q/uSRYveWL3a5/czxkQx7fAu+h6DQQiZptnU1OT3+3O5XDweV1VVNJITVR2UOiUlJZTS/Xv3GYYxefLkdDr9zNPPfOvb366rq4tGoxKShkapxSCLi4uLI8Ver2YYei6bi8fj8Xjc0HVJkiRCRDUQPFNlRldX18iRIwmRerp7DMMQe9fEkYwxCCDCyOPxAAgUWRGlwowxiRCPR1UU1at5hV6QJAkCiDEGEKiKijH2eL2yLMsywQOQ/H7f9OnTQ6HQunXrVr++euSokZwDxigAQJKk8RMmHjp4MJVK+X2+9662OR3DJAch5LXXXg0Ego7jBAKBTCZTHPCFR1QdP9550ezZ5RUVhBCEsFfT/vLmm7fccsuqlS9rPn7KVMJB6Sf8TM65LMucc0YZRDC/VO9LJqHXKaVHDh8hhKTTaRH5JjLJ5nKlpWU9PV2WZamqWllZuW7t+kDAP2funEceeTTa359MJBZevPDpp54BAOCT+wdBCNPpNIJQlpXSklJtpBYIBCKRSE7PdXR07N+7v/1oeywWMw3D5/d7PB4iSWBQO9i23dbWNnHSxIULF/r9ftEPBEmYSKIHriT0iKqqIs8AIRC5G9GhJO9ADbUwBL3EkyA+g8FWvrFodNWqVSteWlE7spZxzhiFEBm6PuW8KRijaCxmWRZACHxAn3GY5IAQdh7rFPpCdGDq6elZsHBBNpNRPerceXNzuVw0Ea0bXXe4vU039Onnz2g5eAhLeKgxlffgEUJer1eIUAihSEedSCW8nw2bF/XihLquS5JUVFTU09NjOXZVVWUgGNjV1HS8q2vxkiUVleWTp0wpLy/v6eslkrRl65YLZs5csWLFQNNSlF+VAbMjFo/H4nGEEARQtDGdv2D+zJkXfuITn4AQ5nS9r7evu7u7vb2t63h3Z2dHLpcTtfW2bW/bum1n404hIQZMGcbFHot8y9T8XXAmXD3mDwQQRgAAwE/UhHLGJSIJheXxeLCEhYKGEPb09vT19imK4vf7IUaUUkVR0un06LrR06ZNW/XKK5ZpGoYxDEdg+K5saXlp25E20R0FAGDb9o4dO25cujQej82dO/f11as543ouF/D5enp7z58xY8/u3RrxoZO77QhpYRiGCJSJqJ9wfU8x1N9zMEN/g5Q6mqbJMrFsSyak9Ujr0qXLYrFo8+7diqIsWLBQ82mSRADjgUBgR2PjZYsXjxkztnlPs6IoQ+JS4sk8Kcot9i898/Qzzzz9THG4eOy4sTU1NaPqRo8dN/aCC85XPZ54LP7KqpfXrl0fDAaIJAlvxTCMof5IXhicUr+Yv2o6lc5HNE5iT1ZUr/FEIjH0eIRQSUlEJLtEaXtXV9f8+fMvnDlz5cqVrYdbvarHtu1hdHYYLjkAkGWZMYYlyePxSJIUDAZfemnFlVd+Yvbs2YqqcsYXX3qZaZmxWOx4Z+dll12q+XzC8x4qOQgh5eXlJaUlAMD+/r7e3r54LMoZh4M+JIQIgPdRK0K45ofGAQiFQgAizjjGsD8aO955/I477rjn3nv37tmzZMmSZDJBGUUSJoQcOHCgt7d31qxZb296pyQSwfBEZvx0oSVsW6/H41CazqS3bd22fft227YJISWlpSWRyOw5s2//4hfLKyoef+zxUCikKEre98lLQQgHyhMLZarfQ43mz3bGvzPKDN3weD3//M9f9vn8L7305xFVIy67bPHBgweGFyEdblaW85KSkqZdTUGMFVXhnC9evPiWz97S398vGkssXLhgwoTx99xzT319fUtLy3XXXVdSWtrd1XXKaSzLOnz4cHd399hxY6dNmxYOFxMi6Tm983jnkcOHjxxpy2bTjkNFVF6SJFGGnm8WlTf7pcHCAGG/hEKhbDYLIRStP596+qlRo0f95Cc/adq50+/3F0eKD7ce7u/rLQqFfD7f/n37J0wYjwZ3PA+5xZO8Sj2nB4uCdXV1kZISjJFhGMlkMh6LJxIJwzD6enpj/dEtW949evTozTffYuj6888vF5GPvPGUP9tgcv7UzQqnq9G8+OScIwTZYJlSnh/iK6InOGNs7NgxS5YsOXTo0Et/funKK6/0+Xy7du2aOXOmz+8fhh8wTHJQxkaNGpXJZMrLyxGE6XR645sbr7766suvuFzU4hYXR1595dW+/v7p02c0725OpVIzZkx//rmWUCg0dJQQQkKIZVk7G3fu2L6juLh40qRJY8eNXbjw4uuvv95xaCwW7e/ri0ZjPT09x493plOZRCqZSaV0XReeCOecOpQQSeyPUlU1Y1lF4aL+/v6ioqJIaUlvb097e/u3v/3tPzzxxCWXXgoBZJQSIl266NLP33rrgf0HdjY2zp03Z/ToukQiPvQehzIDQbTspmVjx47hHNiO7TiOYRgiaUopjUVjra0tBw8eVFRl1curbNu+9bbbevr6tmze7PVqojcyBBAimM+P5Bkz1EjPU2EoP4bEM04ck4+UQIQkSbIsixASi8WuuuqqtevWHjp46Iorruju7vZ6vP3RaFdX1+zZs50PnsEYLjkcZ9y4caqqIowgwoqspFPpf/2X//PvP/zBZz/3Oc5Ye+ex9Rs3hIvCHo+aTqePHeu46KKL/ue//icYDJ4++xBCIYGz2eyGDRtWrlypqmpFecWSK5ZMmjypfuzYC0KhvBB2bCeXy9m2ZQ/EAGwOGGe8ra3ticefEEGCkpKSHdt3cM7LSssuX7KkqqoqFo/ncjpnXJIlRllpSdl999+/u6npod8/iDH5wu23z5k9+9lnny0KhQZukFIRFXUcp6ys7NLLLs2kM6+++lpFZUV/f/+a11djLHHOJSIRiaiq4vF6JYkYhllcXPzaK68F/IEv3Hbb3uY96XR68pTJZWVlgANZkTHG1KGWbVFKOeOWbZmG5Tg25zyXy4nNfJZlDXRdPzmVKoakKAohst/vE4a8JEmqR+3v629tab3hxhv27t3b2Ljz/Bkz1q5bV1VZWTtj5HPPPzd50iSROviIyMEB8Hq9EyZMSKVSgYA/nc0Uh8P33f/zJZdfDjhHkhQMBspKS9va2oSNuW/fvkmTJ40bP27obrABgSlK9wcdWq9X0zSNUhpPJB579HEIQU1t7ZgxY2pqa0aPHl1WWlpeXhEIBk4fEmVM2DSMUtHFUdf11WtWv/X2W4suWXThhTPHjx+PxPZlwGVF7u3pefKpJyORkkQisX//vvHjxw0NIwqP0TAM3TA+cdUnGhoatry7hRACdoAvfumL0Wj0SOsRWZHFmokErGjRTykNF4eXL19eU1vzta997Re/+MW1117bsK2ByEQcbFlWTs+ZhpnJZBzqWKblUEfP6aItP4QnqiTByak2Yd7qup7L5ZLJRN7Xq66uVhRF83nLK8ofe+xxr9fb0toajUYnjB/v2LbDqO04w0t8DrebIOcej0fTtJ1NjRUVVfFY7OJLLvb7A827d4+uqyMSUVUv52zbtoaxY8ceOnjQ0I1Fly7q6u5qO9KW7wYGAYeQYQghABBSzjghGCIAGJUwUmRJUWSFECOX6zjW3tS44/XXXl39+uq33vrL/n37Y/39lmXphg444JxBCDs6Oja/s4kQkkqlFl26qLm52aFUURXDMBobG/ft21dWXjaiegQUFZwIrXr55TfWrPUF/LZllZeVn3/BBVu3vGvbNhxseW5ZFoTols/dvL1hx/aGBp/Pp8iybdt19fWZTLavt0esTX67Q/5XAIDH41m5YuWnPv0pWVYQgq2tra+++mrX8a7Wltb29rbjx7t6e3rT6bR40YJwZ/jg1p68p3aK8TH0JwccSxLgAEJQWVXZ2dk5pn6MoiiNO3ZoXo+uGz7NZ5pGXV1dPBZXFHny5Mnv213tdAy/TNB2nJkzZ6qqZ+u2LYyz1Wteb2xs/NUDv8mk05pXa2zcDhGaPfsix7FlRT5w8EA2m50yZcr6teu9Xu/AbXMGGOOIQwA5BxhDIN6LI8qyOEAQYCJcX6TIxOfTGGWxvr6e450b166GEPn9gUh5RUVFRVE4nMvlAIYAQSRLpmn5g4F4MuGVPPPnzV206LKRI2t9Ph8EkHEGEaIOvWDmzNVrVpumBSH0er3BQKCyqurAvv0KVjngYvvQBTNndB7r3NGw3efTRLfkQDAYDhd1dnQwzjEA+TgNGDRg81ZFaUnpihUrFl2yaOXKlZ+8+urevt7Ojk5RyAMhHBTyAw7zUPceQihqz8AQzSu4AwdFGoDQsi2HOmIXQjKVCkeKjx3r8Kgexrgiy5zS/r7+Hdt3qIqSzWQ+aGx0YDDDJgfg3LbturrRwUBQJoQyeumll0UixQ0NDYlEvK+v74EHHhApN6EsW1pa6uvrQ0WhVDJlmqZt2xxAgAgHmAHMOeQAcQ4Zh4xjDjAHEgeY8YEPHEiMQwAxUVTNHwxHyooipUiS+/v6Ght3rlm7dvOWdyGCoqbLceyK8nJZJvPnzx9RXbN/376HH3nkzTffFG9IEdGt2trapUuXZbPZdCpdFArJsjJq9GjDMEXKAyGkqIplWdsaGnx+n2Xb4XDxvAULrv3Ute+++240GhXB3LzHcbov4PF6tm3bls1lHcfZsuXdm2++2ev1ZjIZx3F0XRfTQh3bcRzbtsRuHcuyxPZdyzRFobJpGCKNInrCWLZtGEY2mxUVmQF/YMK48T09PRBCPZdLJBNElsV4AIQej6elpaW3p6ekpDTwUXorA/QAAABQUVFeVBSORvsvumj2I4888ulPf2b16jViQ1EgEEhnMoqsEImsXLnyxz/+yR3fvOMvb/6lq6urs7MzHk86lEpYUjyqQuQhLf2G/hx6S5CLh1Xk0yGEGCMACGYqhjx/LAOMsYqKii1bt23YuDGZSum6Xj+67nOf+9zRo0fLy8oVVUlmEk899fTChQtuuOGGVS+vUlRl166do0aNyuVyPr8PACBKw/ft3SeCkoSQzs7ORCLeuGNHMpkMBALCQcjr8lMiImLtIYD9fVFZUVa/tpoz/qV/+tKuXbt2NzUriizEDJYwkQjGmDKKIMISFhWmEsZEJpIkSRIRkfX85Ij6MZEw4pzv3bs3nkjIshyPJ8RTke/calmWoiiZTKasrEyEFj/o+p5t2ydCSEmkpCRS0tvXc2D//kwmm8mkm5p2TZ48GSIUCAQOHjpIiAQ46DjW8dRTT15//fVf/ucv53I5kR6Lx+NtbUdbW1uamppjiYToo4WRJElYQlgiUn7HCkZ44LUpWAKAA8CZA0QPLgA4owPeP+ecSFI0GqusrEScS5JUFAoF/IE77rijtaX13c2bv/LVryQTCUrZE3944p133i4vr6isqAiFQk1NTeeffz6lDkIoX9mbtwCEJWGaFgBAFM6IGAYcErIYwo+B4hVVUQDkfb29kZLIpk2bdEOfPHnylClTKGWShPNeCWMsl805ovqBUtuxRaYJcGCYhtgQJLaLUkZty3ao4ziOaVmpVEq8dQRC2NnZWRQuEhEgobOEjtu6afNPf/ZTe1g1pGdHDs6RJEGEJk2etLVh66GWlpG1I/ft29fd3ePz+UpLIhUVFWvWvgEoFxGI1197/dmnn128ZHFJacns2bPr6kYzDmZeOBMjzAFwbFvX9WQimc1mDVO3LCubyYrd0pZlW5Zl6HoylTIN3bJs0X8nnU6LZkCccYQgliRZJpqmHTncOm3qeYFQ0DBNXde/ePsXDh48cOeddz7yyKMHDhzYt2/fRRddFAwEIUKth1tvvunmnp7eXU1Ni5csCQSDIuKZb0EzuPRnKBDP24yinHMIOQaOHz9h/N49e8X7GzweT9Oups3vbCYyoY4jSRLNb1rJb5YUu2MwAgAgiBBG4k1WjHPxkJyIfUFIqaMoiq7rAAChgxLJBKA8nU5XVFRoPi0cDms+3+dvvbWmttbQ9WEs79lKDs651+MxDXPunLnrN663LDORYDk9t2PHjqVLl7a0tOg53evxivBlMBjUNK2hoWHn9saHH3+ktrbWscWeIwAAABASWS4tKx1ovpE31zkQNTci9JR/2mzbskzLMC3HcSzLNAwjmUgcO3p0e0PDkcOHvV7v+HHj33rn7dGjR/f29K169ZVZs2aFQsGVK1dSSidMnGg5lojQ19WN3rp129GjR4kklZaWJhKJD1SUNDRYOXgfkHMmy0pfb5+wTsSfVUUR9czotA3WYNBwyTuonHEAOKXUMAzDMADnhmFwxkXM1+FsRPUIy7bqRo0uKyvzer2BYFBVlHA4HAqFVAFF9Xg9sqwMjxng7MnBKK2uqdmxo3Hy5Mlt7e1Hjx3z+X2O40yePCUcDv/pqadCoSBzmAhyi3io4zjXfObaCy644FRZJ0IdAJw5swwhQsiraYI3A38DJ2VWxFWwJL2+6pVNmzZfd911GzZuoJRufHMjkcmoUaM6Ojoatm8PBYPdXV2R4kgymbx8yZJQqGj7ju227bS3t3/y6k/+93/990m7WgbDMIXnAOYDNfn7EDES4fIMrSiDEFmmQRmDA7VCECIoYayoKpFlVVGwhBVZ8fl8gWBAUVSMYNWIEcXFxRKW/H4/UeTicFjspgEQitysUCI8Xw3JOaVUz+W8mhdCaNvWsBf3HLzGCyGUyWYff+ThBRdfsnz58s7jx6dOnbpwwcLVa1a3tLQijCSEMRqIdzFGDcO6664f1o4ceW5eWDQ07QahTMiWLVseuO8+WVXu+8Uvdzfv/t+H/jdcHLZte9q06YqibNq0iRCpekR1NBY73nX817/61Y4dO555+hkiy0Wh0F0/umvjxo1imyccaJqLOGMSRsKo4AAgiCCCkkQgEG9TYZQ6jIt3z+ZfKckljBmlkqwQgsPhSDAYDBWFvJo3EAgEAgFZlj2qKiuKqqqESKrqIYQosoIlrKqqYRh9fb0IYwAgAtChA46M4zjJZDKRSMiy8pnPfNo0zVdeebXj2DEAoWEYiUQ8k8kmE6nGbdu+84M7ly1bekpO8oPiHDSpZZwHAoFZF81etXLlDTfc6PV4YrHY66+9duDgoUDQb5kWQoAByjmg1DnWfvT7/35XbW3th7JZgXPbtmfOnAkxZhC89NKK22+/veVQy5sbNmqa1t/XByEkkgQBPHbsWCwa+48f/wel7LlnnwsGg5zz/v6+hx9+eOmyZWPGjOnu7pawBCAwDEMixNR1Sqll2bZt6bpu21YykeCMF4XDiqp6NU2VFUkmiqKoiiLLsqKqHo/H7/cLISTifBgjkQ0RjfoHtScAAJ6UhAPANM3f//73Xd2dCEiSRBhl9fV1SJIQghXlFeMnTph63nltbW3PPff8rp07VVWllArlJREZS/hT13/m05/+FML4LN9aeo5eAAghgvCdt9/+3je/Wz9hrOM4Xq9nwNSQJEYdxhmljkPZLbd8dsmSJScarw6P1ydLi1NOIstyQ8P2u3/8I7/Pf8P1N15z7TXPPfvca6++5lBH0zThI8gy+eFdd4XD4e/feaehmx6vR7is2UwWAL7wkosDgaBpmrNnXzRmzBiH0gHfWqizfPiSD/RzyuuUk+Iep0Q/+Alf+8S26QIzQAjp6e6++8d3m4alKArnbPTo+osXLVQUxbGdrq6unTt3vfXmX6prqkW4RThTnHNdNxCCP/3ZT0tLS8++edw5ezukeFnCtm3bXly+/O11a1XNg7GEEaacM9tMpxIXzJn/z1/7xqRJkxxKh24qH9bF3oscEELLsh76/e+3NWxBDF9/w/XXfurajs7ONatXHzlyRFGUi2ZdNH/B/GQy9eijjx48cEDTNGElWJYly7LIkug5HUv40ccewxL+kLp8nnHweRBZbm9ru/dn9/ZH+8PhsHj57YDhAqCiyB6vFw3uvYCDJcoYo29/5ztjx42zreGbGidGdy5fHQqhJEkyIelUOpPNAAgRAJRzhciyIvt8Ptu2T+QGz4YcJ1/01JNAKGHc19d/73/em0mms9nMiOrqG5feOGnSZFkmlNJMNrtx/YZHHn60tCzi9Wp84BXDWBgZhJBsNhsMBr//g++Hw+EP9lb6D6rj3/N4UaSydu3aAwcORPtjLS2HTMOQCBFFLaJ9gyRJjm1jLFVWVU6aNOnW227DGJ2rhpMfyntl8wkCMCTjeqrB/+GRAwzw4/Dhw/fec49tO5IkpdOp4uJIaVkpo+zo0aOcc5HmEKnOoSU24nUO37vze3V1dQNC7mxGcnZfgQiJepdkItnVddyxHcqEG2/n97BoXg1LuKKioryiwrHtYYTJC179Y3td+YdKDjDAj+NdXS8sX75tyzYAgWBDPuiZDzBABEUpr8huVFZVfv7zn580aZLtOOd8sYd3vDBj4ZAiBwBOTJ1I0nIOztL8PMN1/2HJAQCAECPEGNu7Z+/OXTvfWLMmk85ijBGCnAMkZBsEGCHDNL1e78SJE+fMmzt92rRgKOQMgxnvPZizP/5czdhfjX9ocgz8OxSFdCKZ2dfXJzZADGxfY5xxpqrqiBHViiJ7PB4OzuIRdMlxjq78EZEjf5hwPxHCZ+xkwCjNV1F86IMZ3vEfOTnO6Tve/pYxaBIz9hG9yeYfAB9uKzcXf9dwyeGiIFxyuCgIlxwuCsIlh4uCcMnhoiBccrgoCJccLgrCJYeLgnDJ4aIgXHK4KAiXHC4KwiWHi4JwyeGiIFxyuCgIlxwuCsIlh4uCcMnhoiBccrgoCJccLgrCJYeLgnDJ4aIgXHK4KAiXHC4KwiWHi4JwyeGiIFxyuCgIlxwuCsIlh4uCcMnhoiBccrgoCJccLgrCJYeLgnDJ4aIgXHK4KAiXHC4KwiWHi4JwyeGiIFxyuCgIlxwuCsIlh4uCcMnhoiBccrgoCJccLgrCJYeLgnDJ4aIgXHK4KAiXHC4KwiWHi4JwyeGiIFxyuCgIlxwuCsIlh4uCcMnhoiBccrgoCJccLgrCJYeLgvj/Hgomz2qBmVMAAAAASUVORK5CYII=";
        data.put("carImageUrl", carImageUrl);
        data.put("dealerName", dealerShipName);
        data.put("dealerAddress", "ш. Выборгское, 23 к1");
        data.put("dealerPhone", "+7 (812) 603-86-70");
        data.put("reminderId", reminderId);
        data.put("carColor", car.getColor());
        data.put("carTransmission", getTransmissionDisplayName(car.getTransmission()));
        data.put("carEngine", car.getHorsepower() + " л.с.");
        data.put("carFuelType", getFuelTypeDisplayName(car.getFuelType()));
        data.put("cancellationUrl", cancellationUrl);

        long hoursUntil = java.time.Duration.between(
                LocalDateTime.now(), request.getTestDriveDateTime()).toHours();
        data.put("hoursUntil", hoursUntil > 0 ? hoursUntil : "менее 1 часа");

        return data;
    }

    public void sendCancellationEmail(TestDriveBooking booking) {
        try {
            Map<String, Object> templateData = prepareCancellationTemplateData(booking);
            String htmlContent = generateHtml("test-drive-cancellation-email", templateData);

            sendEmail(
                    booking.getClientEmail(),
                    "Отмена записи на тест-драйв - " + booking.getCarBrand() + " " + booking.getCarModel(),
                    htmlContent
            );

            log.info("Cancellation email sent to: {}, Booking ID: {}",
                    booking.getClientEmail(), booking.getId());
        } catch (Exception e) {
            log.error("Error sending cancellation email", e);
            throw new RuntimeException("Failed to send cancellation email", e);
        }
    }

    private Map<String, Object> prepareCancellationTemplateData(TestDriveBooking booking) {
        Map<String, Object> data = new HashMap<>();
        data.put("clientName", booking.getClientName());
        data.put("carBrand", booking.getCarBrand());
        data.put("carModel", booking.getCarModel());
        data.put("testDriveDateTime", formatDateTime(booking.getTestDriveDateTime()));
        data.put("cancellationDateTime", formatDateTime(LocalDateTime.now()));
        data.put("cancellationReason", booking.getCancellationReason() != null ?
                booking.getCancellationReason() : "Причина не указана");
        data.put("dealerName", dealerShipName);
        data.put("dealerPhone", booking.getDealerPhone());
        data.put("bookingId", booking.getConfirmationId());

        return data;
    }

    private String getTransmissionDisplayName(Transmission transmission) {
        if (transmission == null) {
            return "Не указана";
        }
        return switch (transmission) {
            case MANUAL -> "Мануал";
            case AUTOMATIC -> "Автомат";
            default -> transmission.name();
        };
    }

    private String getFuelTypeDisplayName(FuelType fuelType) {
        if (fuelType == null) {
            return "Не указан";
        }
        return switch (fuelType) {
            case PETROL -> "Бензин";
            case DIESEL -> "Дизель";
            case ELECTRIC -> "Электрический";
            case HYBRID -> "Гибрид";
            default -> fuelType.name();
        };
    }

    private String generateHtml(String templateName, Map<String, Object> data) {
        Context context = new Context();
        context.setVariables(data);
        return templateEngine.process(templateName, context);
    }

    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_RELATED, StandardCharsets.UTF_8.name());

        helper.setFrom(emailSender);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        javaMailSender.send(message);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return dateTime.format(formatter);
    }

    private String generateConfirmationId() {
        return "TD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateReminderId() {
        return "REM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
