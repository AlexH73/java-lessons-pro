package de.ait.javalessonspro.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 02.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
public class OpenAiChatResponse {

    @JsonProperty("choices")
    private List<Choise> choices;

    public OpenAiChatResponse(List<Choise> choices) {
        this.choices = choices;
    }

    public List<Choise> getChoices() {
        return choices;
    }

    public void setChoices(List<Choise> choices) {
        this.choices = choices;
    }
}
