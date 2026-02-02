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
public class OpenAiChatRequest {
    @JsonProperty("model")
    private String model;
    @JsonProperty("messages")
    List<OpenAiMessage> messagesList;

    public OpenAiChatRequest(String model, List<OpenAiMessage> messagesList) {
        this.model = model;
        this.messagesList = messagesList;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<OpenAiMessage> getMessagesList() {
        return messagesList;
    }

    public void setMessagesList(List<OpenAiMessage> messagesList) {
        this.messagesList = messagesList;
    }
}
