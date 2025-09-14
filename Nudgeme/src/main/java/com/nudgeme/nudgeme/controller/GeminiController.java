package com.nudgeme.nudgeme.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nudgeme.nudgeme.repository.MoodRepository;
import com.nudgeme.nudgeme.service.GeminiService;
import com.nudgeme.nudgeme.service.MoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:3001" })
@RequiredArgsConstructor
public class GeminiController {

    private final GeminiService geminiService;

    @Autowired
    private MoodService moodService;

    @GetMapping("/ask")
    public String askGeminiAPI(@RequestParam String prompt) {
        return geminiService.askGemini(prompt);
    }

    @GetMapping("/finalize")
    public ResponseEntity<String> finalizePlan(@RequestParam String conversation) {
        try {
            // 1. Get Gemini JSON as String
            String geminiResponse = geminiService.finalizePlan(conversation);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(geminiResponse);

            // 2. Extract mood string
            // After parsing Gemini’s JSON
            String moodType = root.path("goal").path("mood").asText();
            Long moodId = moodService.getMoodIdByType(moodType);

// Replace mood with moodId
            ((ObjectNode) root.path("goal")).put("moodId", moodId);
            ((ObjectNode) root.path("goal")).remove("mood");



            // 5. Return updated JSON as String (or DTO if you want)
            return ResponseEntity.ok(mapper.writeValueAsString(root));

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }


    @GetMapping("/ask-host")
    public String askHostGeminiAPI(@RequestParam String prompt) {
        return geminiService.askHostGemini(prompt);
    }
}
