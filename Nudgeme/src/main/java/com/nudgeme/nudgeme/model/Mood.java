package com.nudgeme.nudgeme.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mood {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;        // e.g. "Happy"
    private String emoji;       // e.g. "😃"
    private String description; // e.g. "Feeling joyful & upbeat"
    private String color;      // base color (e.g. #FFD700)
    private String hoverColor; // optional
    private String glowColor;  // optiona
}
