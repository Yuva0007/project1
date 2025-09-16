package com.example.demo.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${chat.model:gpt-3.5-turbo}")
    private String model;

    private final WebClient webClient = WebClient.builder().build();

    public String chat(List<Map<String, String>> messages) {
        if (openaiApiKey != null && !openaiApiKey.isBlank()) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("model", model);
                payload.put("messages", messages);
                Map resp = webClient.post()
                        .uri("https://api.openai.com/v1/chat/completions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(payload)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            } catch (Exception e) {
                log.warn("OpenAI chat failed: {}", e.getMessage());
            }
        }
        // Fallback heuristic assistant
        String last = messages.isEmpty() ? "" : messages.get(messages.size()-1).getOrDefault("content","");
        return "Thanks for sharing. I understood: " + last + "\nYou can provide more details or ask me to categorize, route, or draft the complaint.";
    }
}


