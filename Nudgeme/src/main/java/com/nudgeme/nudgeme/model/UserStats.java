package com.nudgeme.nudgeme.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One-to-one mapping with User
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;


    @Builder.Default
    private int rewardPoints=0; // optional gamification

}
