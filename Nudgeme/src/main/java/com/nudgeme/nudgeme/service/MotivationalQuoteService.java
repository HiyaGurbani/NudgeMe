package com.nudgeme.nudgeme.service;


import com.nudgeme.nudgeme.model.MotivationalQuote;
import com.nudgeme.nudgeme.repository.MotivationalQuoteRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.Data;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;

@Service
public class MotivationalQuoteService {

    @Autowired
    private MotivationalQuoteRepository quoteRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public void fetchAndSaveQuotes(int count) {
        for (int i = 0; i < count; i++) {
            fetchFromQuotes();
        }
    }


    private void fetchFromQuotes() {
        String zenUrl = "https://quotes-api-self.vercel.app/quote";
        ResponseEntity<QuoteDTO> response = restTemplate.getForEntity(zenUrl, QuoteDTO.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            QuoteDTO dto = response.getBody();
            if (!quoteRepository.existsByText(dto.getQuote())) {
                quoteRepository.save(MotivationalQuote.builder()
                        .text(dto.getQuote())
                        .author(dto.getAuthor() != null ? dto.getAuthor() : "Unknown")
                        .dateAdded(LocalDate.now())
                        .build());
            }
        }
    }

    public Optional<MotivationalQuote> getRandomQuote() {
        long count = quoteRepository.count();
        if (count == 0) {
            return Optional.empty();
        }
        int index = new Random().nextInt((int) count);
        return quoteRepository.findAll().stream().skip(index).findFirst();
    }




    @Data
    public static class QuoteDTO {
        private String quote; // quote
        private String author; // author
        // getters & setters
    }
}
