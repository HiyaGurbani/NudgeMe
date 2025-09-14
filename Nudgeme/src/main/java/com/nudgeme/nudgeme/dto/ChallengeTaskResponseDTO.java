package com.nudgeme.nudgeme.dto;

import com.nudgeme.nudgeme.model.enums.ChallengeTaskStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ChallengeTaskResponseDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private ChallengeTaskStatus status;
    private Long challengeId;
    private String challengeTitle;
}
