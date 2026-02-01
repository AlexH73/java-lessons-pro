package de.ait.javalessonspro.service;

import de.ait.javalessonspro.dto.CarOfferEmailRequest;
import de.ait.javalessonspro.model.Car;
import de.ait.javalessonspro.repositories.CarRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 29.01.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@Service
@Slf4j
public class CarOfferEmailService {

    private final JavaMailSender javaMailSender;

    private final TemplateEngine templateEngine;

    private final CarRepository carRepository;

    public CarOfferEmailService(JavaMailSender javaMailSender, TemplateEngine templateEngine, CarRepository carRepository) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.carRepository = carRepository;
    }

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.public.base-url}")
    private String baseUrl;


    public void sendCarOfferEmail(CarOfferEmailRequest carOfferEmailRequest) {
        Car car = carRepository.findById(carOfferEmailRequest.getCarId())
                .orElseThrow(() -> new IllegalArgumentException("Car with id " + carOfferEmailRequest.getCarId() + " not found"));

        String confirmUrl = baseUrl + "/confirm-offer?carId=" + carOfferEmailRequest.getCarId();

        Context context =  new Context();
        context.setVariable("clientName", carOfferEmailRequest.getClientName());
        context.setVariable("carBrand", car.getBrand());
        context.setVariable("carModel", car.getModel());
        context.setVariable("productionYear", car.getProductionYear());
        context.setVariable("mileage", car.getMileage());
        context.setVariable("color", car.getColor());
        context.setVariable("transmission", car.getTransmission());
        context.setVariable("fuelType", car.getFuelType());
        context.setVariable("offerPrice", carOfferEmailRequest.getOfferPrice());
        context.setVariable("confirmUrl", confirmUrl);

        String html = templateEngine.process("car-offer-mail", context);

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(carOfferEmailRequest.getEmail());
            mimeMessageHelper.setSubject("Car offer from AIT Gr.59");
            mimeMessageHelper.setText(html, true);

            log.info("Sending email to {}", carOfferEmailRequest.getEmail());

            javaMailSender.send(mimeMessage);

            log.info("Email sent to {}", carOfferEmailRequest.getEmail());
        }
        catch (MessagingException exception) {
            log.error("Error sending email", exception);
            throw new RuntimeException("Failed to send email", exception);
        }
        catch (Exception exception) {
            log.error("Error sending email", exception);
            throw exception;
        }

    }
}
