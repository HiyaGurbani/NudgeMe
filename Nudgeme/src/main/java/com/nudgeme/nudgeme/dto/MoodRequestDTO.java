package com.nudgeme.nudgeme.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MoodRequestDTO {

    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Emoji is required")
    private String emoji;

    private String description;
    private String color;
    private String hoverColor;
    private String glowColor;
}
