package com.nudgeme.nudgeme.controller;


import com.nudgeme.nudgeme.dto.AchievementResponseDTO;
import com.nudgeme.nudgeme.dto.ChallengeResponseDTO;
import com.nudgeme.nudgeme.model.Challenge;
import com.nudgeme.nudgeme.model.enums.CreatorType;
import com.nudgeme.nudgeme.repository.ChallengeParticipantRepository;
import com.nudgeme.nudgeme.repository.UserRepository;
import com.nudgeme.nudgeme.security.JwtUtil;
import com.nudgeme.nudgeme.service.ChallengeParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.nudgeme.nudgeme.model.User;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/challenges")
@RequiredArgsConstructor
public class ChallengeParticipantController {

    private final ChallengeParticipantService participantService;
    private final JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @PostMapping("/{challengeId}/join")
    public ResponseEntity<?> joinChallenge(
            @PathVariable Long challengeId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        String message = participantService.joinChallenge(challengeId, username);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @DeleteMapping("/{challengeId}/cancel")
    public ResponseEntity<?> cancelChallenge(
            @PathVariable Long challengeId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        String message = participantService.cancelChallenge(challengeId, username);
        return ResponseEntity.ok(Map.of("message", message));
    }


    @GetMapping("/{challengeId}/participants")
    public ResponseEntity<?> getParticipants(@PathVariable Long challengeId) {
        return ResponseEntity.ok(participantService.getParticipants(challengeId));
    }


    @GetMapping("/my")
    public ResponseEntity<List<ChallengeResponseDTO>> getMyChallenges(
            @RequestHeader("Authorization") String authHeader) {

        // 1️⃣ Extract token
        String token = authHeader.replace("Bearer ", "");

        // 2️⃣ Extract UUID from token
        String uuid = jwtUtil.extractUuid(token);

        // 3️⃣ Find user
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 4️⃣ Fetch challenges for this user
        List<Challenge> challenges = challengeParticipantRepository.findChallengesByUserId(user.getId());

        // 5️⃣ Map to DTO
        List<ChallengeResponseDTO> response = challenges.stream()
                .map(this::mapToResponseDTO)
                .toList();

        return ResponseEntity.ok(response);
    }

    private ChallengeResponseDTO mapToResponseDTO(Challenge challenge) {
        return ChallengeResponseDTO.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .participants(challenge.getParticipants())
                .difficulty(challenge.getDifficulty())
                .category(challenge.getCategory())
                .rewardPoints(challenge.getRewardPoints())
                .achievement(challenge.getAchievement() != null ? AchievementResponseDTO.builder()
                        .id(challenge.getAchievement().getId())
                        .name(challenge.getAchievement().getName())
                        .subline(challenge.getAchievement().getSubline())
                        .bannerImage(challenge.getAchievement().getBannerImage())
                        .publicAchievement(challenge.getAchievement().isPublicAchievement())
                        .build() : null)
                .status(challenge.getStatus())
                .creatorType(challenge.getCreatorType())
                .creatorId(challenge.getUser() != null ? challenge.getUser().getId() : 0)
                .creatorName(
                        challenge.getCreatorType() == CreatorType.HOST && challenge.getUser() != null
                                ? challenge.getUser().getUsername()
                                : "Admin"
                )
                .build();
    }
}
