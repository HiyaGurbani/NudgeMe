package com.nudgeme.nudgeme.controller;
import com.nudgeme.nudgeme.dto.UserAchievementResponseDTO;
import com.nudgeme.nudgeme.model.Achievement;
import com.nudgeme.nudgeme.model.ChallengeParticipant;
import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.model.UserAchievement;
import com.nudgeme.nudgeme.model.enums.ChallengeStatus;
import com.nudgeme.nudgeme.repository.ChallengeParticipantRepository;
import com.nudgeme.nudgeme.repository.UserRepository;
import com.nudgeme.nudgeme.repository.UserAchievementRepository;
import com.nudgeme.nudgeme.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user-achievements")
public class UserAchievementController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserAchievementRepository userAchievementRepository;
    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @GetMapping("/fetch")
    public ResponseEntity<?> fetchUserAchievements(HttpServletRequest request) {
        try {
            // 1. Validate token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Missing or invalid token");
            }

            String token = authHeader.substring(7);
            String userUuid = jwtUtil.extractUuid(token);

            // 2. Get User
            User user = userRepository.findByUuid(userUuid)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 3. Get user achievements
            List<UserAchievement> userAchievements = userAchievementRepository.findByUser(user);

            // 4. Build response
            List<UserAchievementResponseDTO> response = userAchievements.stream()
                    .map(ua -> new UserAchievementResponseDTO(
                            ua.getAchievement().getId(),
                            ua.getEarned_at(),
                            ua.getAchievement().getName(),
                            ua.getAchievement().getSubline(),
                            ua.getAchievement().getBannerImage()
                    ))
                    .toList();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token");
        }
    }

    @GetMapping("/locked")
    public ResponseEntity<?> fetchLockedAchievements(HttpServletRequest request) {
        try {
            // 1. Validate token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Missing or invalid token");
            }

            String token = authHeader.substring(7);
            String userUuid = jwtUtil.extractUuid(token);

            // 2. Get User
            User user = userRepository.findByUuid(userUuid)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 3. Already earned achievements
            List<UserAchievement> earned = userAchievementRepository.findByUser(user);
            Set<Long> earnedIds = earned.stream()
                    .map(ua -> ua.getAchievement().getId())
                    .collect(Collectors.toSet());

            // 4. Get ongoing challenges
            List<ChallengeParticipant> ongoing = challengeParticipantRepository
                    .findByUserAndChallengeStatus(user, ChallengeStatus.ONGOING);

            // 5. Collect challenge achievements
            List<Achievement> challengeAchievements = ongoing.stream()
                    .map(cp -> cp.getChallenge().getAchievement())
                    .filter(Objects::nonNull)
                    .toList();

            // 6. Filter out earned ones
            List<UserAchievementResponseDTO> locked = challengeAchievements.stream()
                    .filter(a -> !earnedIds.contains(a.getId()))
                    .map(a -> new UserAchievementResponseDTO(
                            a.getId(),
                            null, // not earned yet
                            a.getName(),
                            a.getSubline(),
                            a.getBannerImage()
                    ))
                    .toList();

            return ResponseEntity.ok(locked);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token");
        }
    }

}
