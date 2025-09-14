package com.nudgeme.nudgeme.service;

import com.nudgeme.nudgeme.dto.MoodRequestDTO;
import com.nudgeme.nudgeme.dto.MoodResponseDTO;
import com.nudgeme.nudgeme.model.Mood;
import com.nudgeme.nudgeme.repository.MoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MoodService {

    private final MoodRepository moodRepository;

    public MoodResponseDTO createMood(MoodRequestDTO request) {
        Mood mood = Mood.builder()
                .type(request.getType())
                .emoji(request.getEmoji())
                .description(request.getDescription())
                .color(request.getColor())
                .hoverColor(request.getHoverColor())
                .glowColor(request.getGlowColor())
                .build();

        Mood saved = moodRepository.save(mood);

        return new MoodResponseDTO(
                saved.getId(),
                saved.getType(),
                saved.getEmoji(),
                saved.getDescription(),
                saved.getColor(),
                saved.getHoverColor(),
                saved.getGlowColor()
        );
    }

    public List<MoodResponseDTO> getAllMoods() {
        return moodRepository.findAll()
                .stream()
                .map(mood -> new MoodResponseDTO(
                        mood.getId(),
                        mood.getType(),
                        mood.getEmoji(),
                        mood.getDescription(),
                        mood.getColor(),
                        mood.getHoverColor(), // if you added hoverColor
                        mood.getGlowColor()   // if you added glowColor
                ))
                .collect(Collectors.toList());
    }

    public MoodResponseDTO getMoodById(Long id) {
        Mood mood = moodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mood not found with id: " + id));

        return new MoodResponseDTO(
                mood.getId(),
                mood.getType(),
                mood.getEmoji(),
                mood.getDescription(),
                mood.getColor(),
                mood.getHoverColor(),
                mood.getGlowColor()
        );
    }

    public Long getMoodIdByType(String type) {
        Mood mood = moodRepository.findByType(type);
        return (mood != null) ? mood.getId() : 1L; // fallback default to ID 1
    }

}
