package com.nudgeme.nudgeme.dto;

import com.nudgeme.nudgeme.model.Achievement;
import com.nudgeme.nudgeme.model.enums.ChallengeStatus;
import com.nudgeme.nudgeme.model.enums.CreatorType;
import com.nudgeme.nudgeme.dto.AchievementResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeResponseDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long participants;
    private Integer rewardPoints;
    private String difficulty;
    private String category;
    private ChallengeStatus status;
    private CreatorType creatorType;
    private Long creatorId;       // User ID of host/admin
    private String creatorName;   // Display name of host/admin
    private AchievementResponseDTO achievement;

}
