package com.nudgeme.nudgeme.model;

import com.nudgeme.nudgeme.model.enums.ChallengeStatus;
import com.nudgeme.nudgeme.model.enums.CreatorType;
import jakarta.persistence.*;
        import lombok.*;
        import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000) // longer text for details
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;
    @Column(nullable = false)
    private LocalDate endDate;

    @Builder.Default
    private int rewardPoints=0; // optional gamification

    @Builder.Default
    private Long participants = 0L;

    private String difficulty;
    private String category;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeTask> challengeTasks;

    @Enumerated(EnumType.STRING)
    private ChallengeStatus status; // UPCOMING, ONGOING, COMPLETED

//    Who created the challenge? HOST or ADMIN
    @Enumerated(EnumType.STRING)
    private CreatorType creatorType;

    // The host who created the challenge
    @ManyToOne
    @JoinColumn(name = "host_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "achievement_id")
    private Achievement achievement;   // each challenge has one achievement

}
