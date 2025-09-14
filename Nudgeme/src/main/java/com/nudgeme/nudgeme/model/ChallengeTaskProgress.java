package com.nudgeme.nudgeme.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;
import com.nudgeme.nudgeme.model.enums.ChallengeTaskStatus;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeTaskProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private ChallengeTask challengeTask;

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private ChallengeTaskStatus status;

    private LocalDate completedOn;
}
