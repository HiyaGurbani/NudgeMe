package com.nudgeme.nudgeme.model;

import com.nudgeme.nudgeme.model.enums.ChallengeTaskStatus;
import com.nudgeme.nudgeme.model.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private ChallengeTaskStatus status; // e.g. PENDING, COMPLETED

    private LocalDate completedOn;

    @ManyToOne
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;
}
