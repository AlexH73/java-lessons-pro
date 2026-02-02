package de.ait.javalessonspro.controllers;

import de.ait.javalessonspro.dto.AiAnswerResponse;
import de.ait.javalessonspro.dto.UserMessageRequest;
import de.ait.javalessonspro.service.ChatService;
import de.ait.javalessonspro.service.OpenAiChatClient;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 02.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@RestController
@RequestMapping("/api/chat")
@Slf4j
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody UserMessageRequest request) {

        String userMessage = request.getMessage();

        log.info("User message: {}", userMessage);

        String aiReply = chatService.getAnswer(userMessage);

        AiAnswerResponse response = new AiAnswerResponse(aiReply);

        return ResponseEntity.ok(response);

    }
}

