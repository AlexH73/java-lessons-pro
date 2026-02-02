package de.ait.javalessonspro.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 02.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
public class OpenAiMessage {

    @JsonProperty("content")
    String content;

    @JsonProperty("role")
    String role;

    public OpenAiMessage(String content, String role) {
        this.content = content;
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
