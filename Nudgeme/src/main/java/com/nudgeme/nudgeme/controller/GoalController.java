package com.nudgeme.nudgeme.controller;

import com.nudgeme.nudgeme.dto.GoalRequestDTO;
import com.nudgeme.nudgeme.dto.GoalResponseDTO;
import com.nudgeme.nudgeme.dto.GoalUpdateDTO;
import com.nudgeme.nudgeme.dto.MoodResponseDTO;
import com.nudgeme.nudgeme.model.Goal;
import com.nudgeme.nudgeme.model.Mood;
import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.model.enums.GoalStatus;
import com.nudgeme.nudgeme.repository.GoalRepository;
import com.nudgeme.nudgeme.repository.MoodRepository;
import com.nudgeme.nudgeme.repository.UserRepository;
import com.nudgeme.nudgeme.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/goals")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:3001" })
public class GoalController {

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MoodRepository moodRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createGoal(@RequestBody GoalRequestDTO requestDTO,
                                        HttpServletRequest request) {

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
            User user = userRepository.findByUuid(userUuid)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Handle moodId with default fallback
            Mood mood;
            if (requestDTO.getMoodId() == null) {
                // Not provided → default to 1
                mood = moodRepository.findById(6L)
                        .orElseThrow(() -> new RuntimeException("Default mood not found"));
            } else {
                // Provided → check if valid
                mood = moodRepository.findById(requestDTO.getMoodId())
                        .orElseGet(() -> moodRepository.findById(6L)
                                .orElseThrow(() -> new RuntimeException("Default mood not found")));
            }


            // ✅ Map DTO → Entity
            Goal goal = Goal.builder()
                    .title(requestDTO.getTitle())
                    .description(requestDTO.getDescription())
                    .startDate(requestDTO.getStartDate())
                    .endDate(requestDTO.getEndDate())
                    .status(requestDTO.getStatus())
                    .priority(requestDTO.getPriority())
                    .category(requestDTO.getCategory())
                    .mood(mood)
                    .user(user)
                    .build();

            // ✅ Save Goal
            Goal savedGoal = goalRepository.save(goal);

            // ✅ Map Entity → ResponseDTO
            GoalResponseDTO responseDTO = new GoalResponseDTO();
            responseDTO.setId(savedGoal.getId());
            responseDTO.setTitle(savedGoal.getTitle());
            responseDTO.setDescription(savedGoal.getDescription());
            responseDTO.setStartDate(savedGoal.getStartDate());
            responseDTO.setEndDate(savedGoal.getEndDate());
            responseDTO.setStatus(savedGoal.getStatus());
            responseDTO.setPriority(savedGoal.getPriority());
            responseDTO.setCategory(savedGoal.getCategory());
            responseDTO.setMood(savedGoal.getMood());

            return ResponseEntity.ok(responseDTO);

        } catch (Exception e) {
            // Catch invalid token or other errors
            return ResponseEntity.status(401).body("Invalid or expired token: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<GoalResponseDTO> updateGoal(@RequestBody GoalUpdateDTO dto) {
        Goal goal = goalRepository.findById(dto.getGoalId())
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        if (dto.getTitle() != null) goal.setTitle(dto.getTitle());
        if (dto.getDescription() != null) goal.setDescription(dto.getDescription());
        if (dto.getStartDate() != null) goal.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) goal.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null) goal.setStatus(dto.getStatus());
        if (dto.getPriority() != null) goal.setPriority(dto.getPriority());
        if (dto.getCategory() != null) goal.setCategory(dto.getCategory());

        if (dto.getMoodId() != null) {
            Mood mood = moodRepository.findById(dto.getMoodId())
                    .orElseThrow(() -> new RuntimeException("Mood not found"));
            goal.setMood(mood);
        }

        Goal updatedGoal = goalRepository.save(goal);

        GoalResponseDTO responseDTO = new GoalResponseDTO();
        responseDTO.setId(updatedGoal.getId());
        responseDTO.setTitle(updatedGoal.getTitle());
        responseDTO.setDescription(updatedGoal.getDescription());
        responseDTO.setStartDate(updatedGoal.getStartDate());
        responseDTO.setEndDate(updatedGoal.getEndDate());
        responseDTO.setStatus(updatedGoal.getStatus());
        responseDTO.setPriority(updatedGoal.getPriority());
        responseDTO.setCategory(updatedGoal.getCategory());
        responseDTO.setMood(updatedGoal.getMood()); // returns full mood object

        return ResponseEntity.ok(responseDTO);
    }


    // Partial update of goal — only status
    @PatchMapping("/updateStatus")
    public ResponseEntity<?> updateGoalStatus(@RequestBody GoalUpdateDTO dto) {
        try {
            Goal goal = goalRepository.findById(dto.getGoalId())
                    .orElseThrow(() -> new RuntimeException("Goal not found"));

            goal.setStatus(dto.getStatus()); // assuming dto.getStatus() returns GoalStatus
            goalRepository.save(goal);

            return ResponseEntity.ok(Map.of(
                    "message", "Goal status updated",
                    "goalId", goal.getId(),
                    "status", goal.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }



    @GetMapping({"/fetch","/fetch/{status}"})
    public ResponseEntity<?> getGoalStatus(@PathVariable(required = false) String status,
                                           HttpServletRequest request) {
        try{
            String token  = request.getHeader("Authorization").substring(7);
            String userUuid = jwtUtil.extractUuid(token);

            User user = userRepository.findByUuid(userUuid)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Goal> goals;

            if (status == null || status.isEmpty()) {
                // No status provided → fetch all goals
                goals = goalRepository.findByUser(user);
            } else {
                // Status provided → filter by it
                GoalStatus goalStatus = GoalStatus.valueOf(status.toUpperCase());
                goals = goalRepository.findByUserAndStatus(user, goalStatus);
            }

            // Map entities → DTOs
            List<GoalResponseDTO> goalDTOs = goals.stream().map(g -> {
                GoalResponseDTO dto = new GoalResponseDTO();
                dto.setId(g.getId());
                dto.setTitle(g.getTitle());
                dto.setDescription(g.getDescription());
                dto.setStartDate(g.getStartDate());
                dto.setEndDate(g.getEndDate());
                dto.setStatus(g.getStatus());
                dto.setCategory(g.getCategory());
                dto.setPriority(g.getPriority());
                dto.setMood(g.getMood());
                return dto;
            }).toList();

            return ResponseEntity.ok(goalDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    @GetMapping({"/count","count/{status}"})
    public ResponseEntity<?> countGoalsByStatus(@PathVariable(required = false) String status,
                                                HttpServletRequest request) {
        try{
            String token  = request.getHeader("Authorization").substring(7);
            String userUuid = jwtUtil.extractUuid(token);

            User user = userRepository.findByUuid(userUuid)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            long count;

            if (status == null || status.isEmpty()) {
                // No status → count all goals
                count = goalRepository.countByUser(user);
            } else {
                // Status provided → count by status
                GoalStatus goalStatus = GoalStatus.valueOf(status.toUpperCase());
                count = goalRepository.countByUserAndStatus(user, goalStatus);
            }

            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }
}
