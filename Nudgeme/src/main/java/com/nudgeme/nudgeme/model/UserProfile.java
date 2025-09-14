package com.nudgeme.nudgeme.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One-to-one mapping with User
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String location;

    @Column(length = 500)
    private String bio;

    private String image; // Can store URL or path to the image

    private LocalDate dob; // Date of Birth
}
