package com.nudgeme.nudgeme.controller;

import com.nudgeme.nudgeme.dto.AchievementRequestDTO;
import com.nudgeme.nudgeme.dto.AchievementResponseDTO;
import com.nudgeme.nudgeme.dto.GoalResponseDTO;
import com.nudgeme.nudgeme.model.Achievement;
import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.model.UserProfile;
import com.nudgeme.nudgeme.repository.AchievementRepository;
import com.nudgeme.nudgeme.repository.ChallengeRepository;
import com.nudgeme.nudgeme.repository.UserRepository;
import com.nudgeme.nudgeme.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.nudgeme.nudgeme.security.JwtUtil;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/achievement")
public class AchievementController {

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Create Achievement
     */

    @PostMapping("/create")
    public ResponseEntity<?> createAchievement(
            @RequestBody AchievementRequestDTO dto, HttpServletRequest request) {

        try {
            // ✅ Extract Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401)
                        .body("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);

            // ✅ Extract UUID from token
            String userUuid = jwtUtil.extractUuid(token); // UUID stored as subject

            // ✅ Fetch user
            com.nudgeme.nudgeme.model.User user = userRepository.findByUuid(userUuid)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Map DTO → Entity
            Achievement achievement = Achievement.builder()
                    .name(dto.getName())
                    .subline(dto.getSubline())
                    .bannerImage(dto.getBannerImage())
                    .publicAchievement(dto.isPublicAchievement())
                    .createdBy(user)
                    .build();

            Achievement saved = achievementRepository.save(achievement);

            AchievementResponseDTO responseDTO = AchievementResponseDTO.builder()
                    .id(saved.getId())
                    .name(saved.getName())
                    .subline(saved.getSubline())
                    .bannerImage(saved.getBannerImage())
                    .publicAchievement(saved.isPublicAchievement())
                    .createdById(saved.getCreatedBy().getId())
                    .createdByUsername(saved.getCreatedBy().getUsername())
                    .build();

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            // Catch invalid token or other errors
            return ResponseEntity.status(401).body("Invalid or expired token: " + e.getMessage());
        }
    }

//    Fetch Achievement for Particular Challenge
    @GetMapping("/fetch/{id}")
    public ResponseEntity<?> getAchievementById(@PathVariable Long id) {
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Achievement not found"));

        AchievementResponseDTO responseDTO = AchievementResponseDTO.builder()
                .id(achievement.getId())
                .name(achievement.getName())
                .subline(achievement.getSubline())
                .bannerImage(achievement.getBannerImage())
                .publicAchievement(achievement.isPublicAchievement())
                .createdById(achievement.getCreatedBy().getId())
                .createdByUsername(achievement.getCreatedBy().getUsername())
                .build();

        return ResponseEntity.ok(responseDTO);
    }

//    Fetch Achieveements for Host to choose one from
    @GetMapping("fetch/public")
    public ResponseEntity<List<AchievementResponseDTO>> getPublicAchievements() {
        List<Achievement> publicAchievements = achievementRepository.findByPublicAchievementTrue();

        List<AchievementResponseDTO> responseDTOs = publicAchievements.stream()
                .map(a -> AchievementResponseDTO.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .subline(a.getSubline())
                        .bannerImage(a.getBannerImage())
                        .publicAchievement(a.isPublicAchievement())
                        .createdById(a.getCreatedBy().getId())
                        .createdByUsername(a.getCreatedBy().getUsername())
                        .build()
                ).toList();

        return ResponseEntity.ok(responseDTOs);
    }




    /**
     * Update Achievement
     */
//    @PutMapping("/update/{id}")
//    public ResponseEntity<?> updateAchievement(
//            @PathVariable Long id,
//            @RequestBody AchievementRequestDTO dto) {
//
//        Achievement achievement = achievementRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Achievement not found"));
//
//        // Update only fields provided by user
//        if (dto.getName() != null && !dto.getName().isEmpty()) {
//            achievement.setName(dto.getName());
//        }
//        if (dto.getSubLine() != null && !dto.getSubLine().isEmpty()) {
//            achievement.setSubline(dto.getSubLine());
//        }
//        if (dto.getBannerImage() != null && !dto.getBannerImage().isEmpty()) {
//            achievement.setBanner_image(dto.getBannerImage());
//        }
//
//        Achievement updated = achievementRepository.save(achievement);
//
//        return ResponseEntity.ok(updated);
//    }


    /// -------------------Image Upload---------------------------------------///
    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(HttpServletRequest request,
                                         @RequestParam("image") MultipartFile file) {
        try {
            // 🔹 Validate JWT (still useful if only authenticated users can upload)
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Missing or invalid token"));
            }

            String token = authHeader.substring(7);
            String userUuid = jwtUtil.extractUuid(token);

            // (Optional) you can check if user exists here if needed
             User user = userRepository.findByUuid(userUuid)
                     .orElseThrow(() -> new RuntimeException("User not found"));

            // 🔹 Upload file to Cloudinary
            String imageUrl = cloudinaryService.uploadFile(file);

            // 🔹 Just return the URL (don’t save in DB)
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error uploading file: " + e.getMessage()));
        }
    }


}


