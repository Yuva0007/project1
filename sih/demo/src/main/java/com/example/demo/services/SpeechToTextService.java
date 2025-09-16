package com.example.demo.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SpeechToTextService {

    @Value("${stt.enabled:false}")
    private boolean sttEnabled;

    @Value("${stt.provider:google}")
    private String provider;

    @Value("${stt.google.apiKey:}")
    private String googleApiKey;

    @Value("${stt.language:en-IN}")
    private String defaultLanguage;

    private final WebClient webClient = WebClient.builder().build();

    public String transcribe(byte[] audioBytes, String mimeType, String languageCode) {
        if (!sttEnabled || audioBytes == null || audioBytes.length == 0) {
            return null;
        }
        try {
            if ("google".equalsIgnoreCase(provider)) {
                return transcribeWithGoogle(audioBytes, mimeType, languageCode != null ? languageCode : defaultLanguage);
            }
        } catch (Exception e) {
            log.error("STT failed: {}", e.getMessage(), e);
        }
        return null;
    }

    private String transcribeWithGoogle(byte[] audioBytes, String mimeType, String languageCode) {
        String encoding = guessGoogleEncoding(mimeType);
        String contentB64 = Base64.getEncoder().encodeToString(audioBytes);
        Map<String, Object> payload = Map.of(
                "config", Map.of(
                        "languageCode", languageCode,
                        "encoding", encoding
                ),
                "audio", Map.of(
                        "content", contentB64
                )
        );
        Map resp = webClient.post()
                .uri("https://speech.googleapis.com/v1/speech:recognize?key=" + googleApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        try {
            List<Map<String, Object>> results = (List<Map<String, Object>>) resp.get("results");
            if (results == null || results.isEmpty()) return null;
            Map<String, Object> first = results.get(0);
            List<Map<String, Object>> alternatives = (List<Map<String, Object>>) first.get("alternatives");
            if (alternatives == null || alternatives.isEmpty()) return null;
            return (String) alternatives.get(0).get("transcript");
        } catch (Exception e) {
            throw new RuntimeException("Unexpected STT response");
        }
    }

    private String guessGoogleEncoding(String mimeType) {
        if (mimeType == null) return "ENCODING_UNSPECIFIED";
        String mt = mimeType.toLowerCase();
        if (mt.contains("webm")) return "WEBM_OPUS";
        if (mt.contains("wav")) return "LINEAR16";
        if (mt.contains("flac")) return "FLAC";
        if (mt.contains("mp3")) return "MP3";
        return "ENCODING_UNSPECIFIED";
    }
}


