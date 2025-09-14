package com.nudgeme.nudgeme.dto;

import com.nudgeme.nudgeme.model.Mood;
import com.nudgeme.nudgeme.model.enums.GoalStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class GoalUpdateDTO {
    private Long goalId;          // To identify the goal
    private String title;         // Optional: new title
    private String description;   // Optional: new description
    private LocalDate startDate;  // Optional: new start date
    private LocalDate endDate;    // Optional: new end date
    private GoalStatus status;    // Optional: for status update
    private Long moodId;
    private String priority;
    private String category;
}
