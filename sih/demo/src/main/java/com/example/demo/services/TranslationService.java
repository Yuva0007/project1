package com.example.demo.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationService {

    @Value("${translate.enabled:false}")
    private boolean translateEnabled;

    @Value("${translate.provider:google}")
    private String provider;

    @Value("${translate.apiKey:}")
    private String apiKey;

    @Value("${translate.target:en}")
    private String defaultTarget;

    private final WebClient webClient = WebClient.builder().build();

    public String translateToEnglish(String text, String sourceLang) {
        if (!translateEnabled || text == null || text.isBlank()) return text;
        try {
            if ("google".equalsIgnoreCase(provider)) {
                return translateGoogle(text, sourceLang, "en");
            }
        } catch (Exception e) {
            log.warn("Translation failed, using original text: {}", e.getMessage());
        }
        return text;
    }

    private String translateGoogle(String text, String source, String target) {
        // Google Cloud Translation v2 REST simple endpoint
        // POST https://translation.googleapis.com/language/translate/v2?key=API_KEY
        // body: q, source, target, format
        var resp = webClient.post()
                .uri("https://translation.googleapis.com/language/translate/v2?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "q", List.of(text),
                        "source", source == null || source.isBlank() ? null : source,
                        "target", target,
                        "format", "text"
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        try {
            var data = (Map<String,Object>) resp.get("data");
            var translations = (List<Map<String,Object>>) data.get("translations");
            return (String) translations.get(0).get("translatedText");
        } catch (Exception e) {
            throw new RuntimeException("Unexpected translation response");
        }
    }
}


