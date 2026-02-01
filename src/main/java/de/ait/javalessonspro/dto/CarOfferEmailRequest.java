package de.ait.javalessonspro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 29.01.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@Getter
@Setter
public class CarOfferEmailRequest {

    @NotBlank(message = "Email is mandatory")
    private String email;

    @NotBlank(message = "Client name is mandatory")
    private String clientName;

    @NotNull(message = "Car ID is mandatory")
    private Long carId;

    @Positive(message = "Offer price must be positive")
    private Integer offerPrice;

}
