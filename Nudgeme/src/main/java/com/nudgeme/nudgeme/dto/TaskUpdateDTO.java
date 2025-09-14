package com.nudgeme.nudgeme.dto;

import com.nudgeme.nudgeme.model.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskUpdateDTO {
    private Long taskId;           // Required for updates
    private String title;
    private String description;
    private LocalDate dueDate;
    private TaskStatus status;     // Optional, only if changing status
}
