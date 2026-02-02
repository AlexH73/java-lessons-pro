package de.ait.javalessonspro.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ait.javalessonspro.dto.OpenAiChatRequest;
import de.ait.javalessonspro.dto.OpenAiChatResponse;
import de.ait.javalessonspro.dto.OpenAiMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author  : Alexander Hermann
 * Created : 02.02.2026
 * Project : JavaLessonsPro
 * ----------------------------------------------------------------------------
 */
@Service
@Slf4j
public class OpenAiChatClient {

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.timeout}")
    private int timeout;

    @Value("${openai.system-prompt}")
    private String systemPrompt;

    private final WebClient webClient;

    public OpenAiChatClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String ask(String userMessage) throws JsonProcessingException {
        OpenAiMessage systemMessage = new OpenAiMessage(systemPrompt, "system");

        OpenAiMessage user = new OpenAiMessage(userMessage, "user");

        OpenAiChatRequest request = new OpenAiChatRequest(model, List.of(systemMessage, user));

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(request));

        OpenAiChatResponse response = webClient.post()
                .uri(apiUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()

                .onStatus(
                        status -> !status.is2xxSuccessful(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(body -> {
                                    String message = "OpenaAi error, status: " + clientResponse.statusCode()
                                            + ", body: " + body;
                                    return Mono.error(new RuntimeException(message));
                                })

                )
                .bodyToMono(OpenAiChatResponse.class)
                .timeout(Duration.ofMillis(timeout))
                .block();
        if(response == null || response.getChoices() == null || response.getChoices().isEmpty()){
            throw new RuntimeException("OpenAi error, no response received");
        }
        if(response.getChoices().get(0) == null){
            throw new RuntimeException("OpenAi error, no choice received");
        }

        String content = response.getChoices().get(0).getMessage().getContent();

        if(content == null || content.isBlank()){
            throw new RuntimeException("OpenAi error, no content received");
        }
        log.info("OpenAi response: {}", content);

        return content;


    }
}
