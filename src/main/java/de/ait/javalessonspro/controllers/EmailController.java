package de.ait.javalessonspro.controllers;

import de.ait.javalessonspro.dto.CarOfferEmailRequest;
import de.ait.javalessonspro.service.CarOfferEmailService;
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
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final CarOfferEmailService carOfferEmailService;

    @PostMapping("/car-offer")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendCarOfferEmail(@RequestBody @Valid CarOfferEmailRequest carOfferEmailRequest) {
        log.info("Sending car offer email for request: {}", carOfferEmailRequest);
        carOfferEmailService.sendCarOfferEmail(carOfferEmailRequest);
    }
}
