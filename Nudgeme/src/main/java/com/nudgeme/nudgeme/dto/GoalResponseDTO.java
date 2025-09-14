package com.nudgeme.nudgeme.dto;

import com.nudgeme.nudgeme.model.Goal;
import com.nudgeme.nudgeme.model.Mood;
import com.nudgeme.nudgeme.model.enums.GoalStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GoalResponseDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private GoalStatus status;
    private String priority;
    private String category;
    private Mood mood;
}
