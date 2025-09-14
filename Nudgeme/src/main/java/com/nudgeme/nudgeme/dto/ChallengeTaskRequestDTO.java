package com.nudgeme.nudgeme.dto;

import com.nudgeme.nudgeme.model.enums.ChallengeTaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeTaskRequestDTO {
    private String title;
    private String description;
    private LocalDate dueDate;
    private ChallengeTaskStatus status; // TaskStatus as string
    private Long challengeId;   // Link to parent goal
}
