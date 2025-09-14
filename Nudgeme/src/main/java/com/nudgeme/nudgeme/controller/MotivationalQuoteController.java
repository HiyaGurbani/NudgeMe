package com.nudgeme.nudgeme.controller;


import com.nudgeme.nudgeme.service.MotivationalQuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/quotes")

@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:3001" })
public class MotivationalQuoteController {

    @Autowired
    private MotivationalQuoteService quoteService;

    @PostMapping("/fetch/{count}")
    public ResponseEntity<?> fetchQuotes(@PathVariable int count) {
        try {
            quoteService.fetchAndSaveQuotes(count);
            return ResponseEntity.ok(Map.of("message",count+ " Quotes fetched and saved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/random")
    public ResponseEntity<?> getRandomQuote() {
        return quoteService.getRandomQuote()
                .map(quote -> ResponseEntity.ok(Map.of(
                        "text", quote.getText(),
                        "author", quote.getAuthor()
                )))
                .orElse(ResponseEntity.status(404).body(Map.of("message", "No quotes found in DB")));
    }

}

