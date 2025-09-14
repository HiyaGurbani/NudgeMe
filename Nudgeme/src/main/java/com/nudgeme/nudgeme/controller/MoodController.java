package com.nudgeme.nudgeme.controller;

import com.nudgeme.nudgeme.dto.MoodRequestDTO;
import com.nudgeme.nudgeme.dto.MoodResponseDTO;
import com.nudgeme.nudgeme.service.MoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/mood")
@RequiredArgsConstructor
public class MoodController {

    private final MoodService moodService;

    @PostMapping("/create")
    public ResponseEntity<MoodResponseDTO> createMood(@Valid @RequestBody MoodRequestDTO moodRequest) {
        MoodResponseDTO createdMood = moodService.createMood(moodRequest);
        return ResponseEntity.ok(createdMood);
    }

    @GetMapping("/fetch")
    public ResponseEntity<List<MoodResponseDTO>> getAllMoods() {
        List<MoodResponseDTO> moods = moodService.getAllMoods();
        return ResponseEntity.ok(moods);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MoodResponseDTO> getMoodById(@PathVariable Long id) {
        MoodResponseDTO mood = moodService.getMoodById(id);
        return ResponseEntity.ok(mood);
    }

}
