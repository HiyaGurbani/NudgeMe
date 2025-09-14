package com.nudgeme.nudgeme.controller;


import com.nudgeme.nudgeme.dto.AchievementResponseDTO;
import com.nudgeme.nudgeme.dto.ChallengeRequestDTO;
import com.nudgeme.nudgeme.dto.ChallengeResponseDTO;
import com.nudgeme.nudgeme.model.Achievement;
import com.nudgeme.nudgeme.model.Challenge;
import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.model.enums.ChallengeStatus;
import com.nudgeme.nudgeme.model.enums.CreatorType;
import com.nudgeme.nudgeme.repository.AchievementRepository;
import com.nudgeme.nudgeme.repository.ChallengeRepository;
import com.nudgeme.nudgeme.repository.UserRepository;
import com.nudgeme.nudgeme.security.JwtUtil;
import com.nudgeme.nudgeme.service.ChallengeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:3001" })
@RequestMapping("/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AchievementRepository achievementRepository;


    @PostMapping("/create")
    public ResponseEntity<?> createChallenge(
            @RequestBody ChallengeRequestDTO requestDTO,
            HttpServletRequest request) {
        try {
            // ✅ Extract token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401)
                        .body("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);

            // ✅ Extract UUID from token
            String userUuid = jwtUtil.extractUuid(token);

            // ✅ Fetch user
            User user = userRepository.findByUuid(userUuid)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Check role
            if (!(user.getRole().equals("HOST") || user.getRole().equals("ADMIN"))) {
                return ResponseEntity.status(403)
                        .body("Only hosts or admins can create challenges");
            }

            // ✅ Fetch Achievement
            Achievement achievement = achievementRepository.findById(requestDTO.getAchievementId())
                    .orElseThrow(() -> new RuntimeException("Achievement not found"));

            // ✅ Map DTO → Entity
            Challenge challenge = Challenge.builder()
                    .title(requestDTO.getTitle())
                    .description(requestDTO.getDescription())
                    .startDate(requestDTO.getStartDate())
                    .endDate(requestDTO.getEndDate())
                    .status(requestDTO.getStatus())
                    .category(requestDTO.getCategory())
                    .achievement(achievement)  // ✅ use fetched achievement
                    .rewardPoints(requestDTO.getRewardPoints())
                    .difficulty(requestDTO.getDifficulty())
                    .creatorType(user.getRole().equals("HOST") ? CreatorType.HOST : CreatorType.ADMIN)
                    .user(user.getRole().equals("HOST") ? user : null) // only attach user if HOST
                    .build();

            // ✅ Save
            Challenge savedChallenge = challengeRepository.save(challenge);

            // ✅ Response DTO
            ChallengeResponseDTO responseDTO = new ChallengeResponseDTO();
            responseDTO.setId(savedChallenge.getId());
            responseDTO.setTitle(savedChallenge.getTitle());
            responseDTO.setDescription(savedChallenge.getDescription());
            responseDTO.setStatus(savedChallenge.getStatus());
            responseDTO.setCategory(savedChallenge.getCategory());
            responseDTO.setRewardPoints(savedChallenge.getRewardPoints());
            responseDTO.setCreatorType(savedChallenge.getCreatorType());
            responseDTO.setDifficulty(savedChallenge.getDifficulty());
            responseDTO.setCreatorId(savedChallenge.getUser() != null ? savedChallenge.getUser().getId() : null);
            responseDTO.setCreatorName(savedChallenge.getUser() != null ? savedChallenge.getUser().getUsername() : null);

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating challenge: " + e.getMessage());
        }
    }


    @GetMapping("/fetch")
    public ResponseEntity<List<ChallengeResponseDTO>> fetchAllChallenges() {
        List<Challenge> challenges = challengeRepository.findAll();

        List<ChallengeResponseDTO> response = challenges.stream()
                .map(this::mapToResponseDTO)
                .toList();

        return ResponseEntity.ok(response);
    }

    // ✅ Fetch by status
    @GetMapping("/fetch/{status}")
    public ResponseEntity<List<ChallengeResponseDTO>> fetchChallengesByStatus(@PathVariable String status) {
        ChallengeStatus challengeStatus;

        try {
            challengeStatus = ChallengeStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        List<Challenge> challenges = challengeRepository.findByStatus(challengeStatus);

        List<ChallengeResponseDTO> response = challenges.stream()
                .map(this::mapToResponseDTO)
                .toList();

        return ResponseEntity.ok(response);
    }

    //To fetch challenges user have not joined
    @GetMapping("/fetch/not-joined/{status}")
    public ResponseEntity<List<ChallengeResponseDTO>> fetchNotJoinedChallengesByStatus(
            @PathVariable String status,
            @RequestHeader("Authorization") String authHeader) {

        ChallengeStatus challengeStatus;
        try {
            challengeStatus = ChallengeStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        // 1️⃣ Extract token from "Bearer ..."
        String token = authHeader.replace("Bearer ", "");

        // 2️⃣ Extract UUID from token
        String uuid = jwtUtil.extractUuid(token);  // 👈 assumes JwtUtil has extractUuid()

        // 3️⃣ Get User by UUID
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long userId = user.getId();

        // 4️⃣ Fetch challenges by status NOT joined by this user
        List<Challenge> challenges = challengeRepository.findByStatusNotJoinedByUser(challengeStatus, userId);

        // 5️⃣ Auto-update statuses if startDate == today
        LocalDate today = LocalDate.now();
        challenges.forEach(challenge -> {
            if (challenge.getStatus() == ChallengeStatus.UPCOMING &&
                    challenge.getStartDate().isEqual(today)) {

                challenge.setStatus(ChallengeStatus.ONGOING);
                challengeRepository.save(challenge); // persist change
            }
        });

        // 6️⃣ Map to DTOs
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
                .startDate(challenge.getStartDate())   // include
                .endDate(challenge.getEndDate())       // include
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


    /// Host Side APIs

    @Autowired
    private ChallengeService challengeService;

    @GetMapping("/host/fetch")
    public ResponseEntity<List<ChallengeResponseDTO>> getAllChallenges(
            @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7); // remove "Bearer "
        return ResponseEntity.ok(challengeService.getHostChallenges(jwt));
    }

    // ✅ Fetch host challenges by status
    @GetMapping("/host/fetch/{status}")
    public ResponseEntity<List<ChallengeResponseDTO>> getChallengesByStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable ChallengeStatus status) {
        String jwt = token.substring(7);
        return ResponseEntity.ok(challengeService.getHostChallengesByStatus(jwt, status));
    }

    @GetMapping("/host/fetch/recent")
    public ResponseEntity<List<ChallengeResponseDTO>> getRecentChallenges(
            @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7); // remove "Bearer "
        return ResponseEntity.ok(challengeService.getHostRecentChallenges(jwt));
    }


}
