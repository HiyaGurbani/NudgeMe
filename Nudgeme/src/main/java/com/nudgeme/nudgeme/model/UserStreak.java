package com.nudgeme.nudgeme.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_streaks")
public class UserStreak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userUuid; // link to User.uuid

    private int streakCount;    // current streak
    private int maxStreak;      // maximum streak achieved
    private LocalDate lastActive; // date of last completed task
}
