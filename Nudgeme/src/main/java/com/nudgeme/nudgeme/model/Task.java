package com.nudgeme.nudgeme.model;

import com.nudgeme.nudgeme.model.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING; // e.g. PENDING, COMPLETED

    private LocalDate completedOn;

    @ManyToOne
    @JoinColumn(name = "goal_id", nullable = false)
    private Goal goal;
}
