package com.nudgeme.nudgeme.dto;

import com.nudgeme.nudgeme.model.enums.TaskStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskRequestDTO {
    private String title;
    private String description;
    private LocalDate dueDate;
    private TaskStatus status; // TaskStatus as string
    private Long goalId;   // Link to parent goal

}
