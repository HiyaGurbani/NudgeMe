package com.nudgeme.nudgeme.controller;

import com.nudgeme.nudgeme.dto.UserStatsResponseDTO;
import com.nudgeme.nudgeme.dto.UserStatsUpdateRequestDTO;
import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.model.UserStats;
import com.nudgeme.nudgeme.repository.UserRepository;
import com.nudgeme.nudgeme.repository.UserStatsRepository;
import com.nudgeme.nudgeme.security.JwtUtil;
import com.nudgeme.nudgeme.service.UserStatsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-stats")
public class UserStatsController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserStatsRepository userStatsRepository;
//    @Autowired
//    private UserStatsService userStatsService;

    // ---------- FETCH USER STATS ----------
    @GetMapping("/fetch")
    public ResponseEntity<?> fetchUserStats(HttpServletRequest request) {
        try {
            // 1. Check JWT token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Missing or invalid token");
            }

            // 2. Extract userUuid
            String token = authHeader.substring(7);
            String userUuid = jwtUtil.extractUuid(token);

            // 3. Fetch user
            User user = userRepository.findByUuid(userUuid)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 4. Fetch stats by user
            UserStats userStats = userStatsRepository.findByUser(user).orElse(null);

            // 5. Build response
            UserStatsResponseDTO response = (userStats == null)
                    ? new UserStatsResponseDTO(0)
                    : new UserStatsResponseDTO(userStats.getRewardPoints());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token");
        }
    }

//    // ---------- UPDATE USER STATS ----------
//    @PutMapping("/update")
//    public ResponseEntity<?> updateUserStats(
//            HttpServletRequest request,
//            @RequestBody UserStatsUpdateRequestDTO updateDTO) {
//        try {
//            String authHeader = request.getHeader("Authorization");
//            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
//            }
//
//            String token = authHeader.substring(7);
//            String userUuid = jwtUtil.extractUuid(token);
//
//            User user = userRepository.findByUuid(userUuid)
//                    .orElseThrow(() -> new RuntimeException("User not found"));
//
//            UserStats updatedStats = userStatsService.updateUserStats(user.getId(), updateDTO);
//
//            return ResponseEntity.ok(updatedStats);
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
//        }
//    }
}
