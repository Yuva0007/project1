package com.example.demo.controller;

import com.example.demo.services.AICategorizationService;
import com.example.demo.services.TranslationService;
import com.example.demo.services.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AICategorizationService aiCategorizationService;
    private final TranslationService translationService;
    private final ChatService chatService;

    public AIController(AICategorizationService aiCategorizationService, TranslationService translationService, ChatService chatService) {
        this.aiCategorizationService = aiCategorizationService;
        this.translationService = translationService;
        this.chatService = chatService;
    }

    public static class SuggestRequest {
        public String title;
        public String description;
        public String language; // e.g., hi, ta, te
    }

    public static class ChatRequest {
        public java.util.List<java.util.Map<String, String>> messages; // [{role:'user'|'assistant', content:'...'}]
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody ChatRequest body) {
        String reply = chatService.chat(body != null ? body.messages : java.util.List.of());
        return ResponseEntity.ok(Map.of("reply", reply));
    }

    @PostMapping("/suggest")
    public ResponseEntity<Map<String, Object>> suggest(@RequestBody SuggestRequest body) {
        String title = body != null ? body.title : null;
        String description = body != null ? body.description : null;
        String lang = body != null ? body.language : null;

        if (lang != null && !lang.isBlank()) {
            title = translationService.translateToEnglish(title, lang);
            description = translationService.translateToEnglish(description, lang);
        }

        var result = aiCategorizationService.categorizeGrievance(title, description);
        return ResponseEntity.ok(Map.of(
                "category", result.category(),
                "confidence", result.confidence()
        ));
    }

    @PostMapping("/suggest/ranked")
    public ResponseEntity<Map<String, Object>> suggestRanked(@RequestBody SuggestRequest body) {
        String title = body != null ? body.title : null;
        String description = body != null ? body.description : null;
        String lang = body != null ? body.language : null;

        if (lang != null && !lang.isBlank()) {
            title = translationService.translateToEnglish(title, lang);
            description = translationService.translateToEnglish(description, lang);
        }

        var ranked = aiCategorizationService.rankCategories(title, description);
        return ResponseEntity.ok(Map.of(
                "suggestions", ranked
        ));
    }
}


