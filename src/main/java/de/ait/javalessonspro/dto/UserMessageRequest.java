package de.ait.javalessonspro.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 02.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
public class UserMessageRequest {

    @NotBlank( message = "Message can't be blank")
    private String message;

    public UserMessageRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
