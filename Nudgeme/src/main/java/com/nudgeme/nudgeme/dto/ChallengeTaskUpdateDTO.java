package com.nudgeme.nudgeme.dto;

import com.nudgeme.nudgeme.model.enums.ChallengeTaskStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ChallengeTaskUpdateDTO {
    private Long taskId;           // Required for updates
    private ChallengeTaskStatus status;
}
