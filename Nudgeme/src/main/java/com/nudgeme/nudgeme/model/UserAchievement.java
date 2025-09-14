package com.nudgeme.nudgeme.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One-to-one mapping with User
     @ManyToOne  // ✅ many achievements per user
     @JoinColumn(name = "user_id", nullable = false)
     private User user;

     @ManyToOne  // ✅ many users can earn same achievement
     @JoinColumn(name = "achievement_id", nullable = false)
     private Achievement achievement;


    private LocalDate earned_at;

}
