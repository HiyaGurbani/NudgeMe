package com.nudgeme.nudgeme.dto;

import com.nudgeme.nudgeme.model.Achievement;
import com.nudgeme.nudgeme.model.enums.ChallengeStatus;
import com.nudgeme.nudgeme.model.enums.CreatorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeRequestDTO {
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private ChallengeStatus status;
    private String category;
    private Integer rewardPoints;
    private String difficulty;
    private CreatorType creatorType;  // HOST or ADMIN
    private Long hostId;              // required if creatorType = HOST
    private Long achievementId;
}
