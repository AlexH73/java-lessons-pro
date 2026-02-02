package de.ait.javalessonspro.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 02.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@Service
@Slf4j
public class ChatService {

    private final OpenAiChatClient openAiChatClient;

    public ChatService(OpenAiChatClient openAiChatClient) {
        this.openAiChatClient = openAiChatClient;
    }

    public String getAnswer(String userMessage){
        if(userMessage == null || userMessage.isBlank()){
            log.warn("Invalid message: {}", userMessage);
            throw new IllegalArgumentException("Invalid message");
        }

        try {
            return openAiChatClient.ask(userMessage);
        } catch (JsonProcessingException e) {
            log.error("Error while processing JSON", e);
            throw new RuntimeException(e);
        }
    }

}
