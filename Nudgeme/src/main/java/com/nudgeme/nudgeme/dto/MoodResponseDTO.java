package com.nudgeme.nudgeme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MoodResponseDTO {
    private Long id;
    private String type;
    private String emoji;
    private String description;
    private String color;
    private String hoverColor;
    private String glowColor;
}
