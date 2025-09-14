package com.nudgeme.nudgeme.dto;

import com.nudgeme.nudgeme.model.enums.TaskStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskResponseDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private TaskStatus status;
    private Long goalId;
}
