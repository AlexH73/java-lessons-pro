package de.ait.javalessonspro.dto;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 02.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
public class AiAnswerResponse {
    private String reply;

    public AiAnswerResponse(String reply) {
        this.reply = reply;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
