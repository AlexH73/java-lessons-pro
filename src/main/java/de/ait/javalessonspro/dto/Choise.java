package de.ait.javalessonspro.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 02.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
public class Choise {

    @JsonProperty("message")
    private OpenAiMessage message;

    public Choise(OpenAiMessage message) {
        this.message = message;
    }

    public OpenAiMessage getMessage() {
        return message;
    }
}
