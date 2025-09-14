package com.nudgeme.nudgeme.model;

import com.nudgeme.nudgeme.model.enums.GoalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private GoalStatus status; // e.g. PLANNED, IN_PROGRESS, COMPLETED

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "goal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks;

    private String priority;
    private String category;

    @ManyToOne
    @JoinColumn(name = "mood_id")
    private Mood mood; // default value
}
