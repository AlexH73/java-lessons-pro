package de.ait.javalessonspro.controllers;

import de.ait.javalessonspro.dto.CarOfferEmailRequest;
import de.ait.javalessonspro.service.CarOfferEmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 29.01.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@Tag(
        name = "Email",
        description = "Endpoints for sending transactional emails related to car offers"
)
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final CarOfferEmailService carOfferEmailService;

    @Operation(
            summary = "Send car offer email",
            description = """
                Sends an email with a car offer based on the provided request data.
                
                The request must contain valid customer contact information and car offer details.
                If the request is valid, the email is accepted for processing and sent asynchronously.
                
                **Response behavior:**
                - 202 ACCEPTED — email request accepted and queued for sending
                - 400 BAD REQUEST — validation failed for the provided request body
                """
    )
    @PostMapping("/car-offer")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendCarOfferEmail(@RequestBody @Valid CarOfferEmailRequest carOfferEmailRequest) {
        log.info("Sending car offer email for request: {}", carOfferEmailRequest);
        carOfferEmailService.sendCarOfferEmail(carOfferEmailRequest);
    }
}
