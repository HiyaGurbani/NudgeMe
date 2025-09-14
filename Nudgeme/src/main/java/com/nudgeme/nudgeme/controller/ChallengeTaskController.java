package com.nudgeme.nudgeme.controller;

import com.nudgeme.nudgeme.dto.ChallengeTaskRequestDTO;
import com.nudgeme.nudgeme.dto.ChallengeTaskResponseDTO;
import com.nudgeme.nudgeme.dto.ChallengeTaskUpdateDTO;
import com.nudgeme.nudgeme.dto.TaskResponseDTO;
import com.nudgeme.nudgeme.model.*;
import com.nudgeme.nudgeme.model.enums.ChallengeStatus;
import com.nudgeme.nudgeme.model.enums.ChallengeTaskStatus;
import com.nudgeme.nudgeme.repository.*;
import com.nudgeme.nudgeme.security.JwtUtil;
//import com.nudgeme.nudgeme.service.ChallengeTaskService;
import com.nudgeme.nudgeme.service.ChallengeTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/challenge-tasks")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:3001" })
public class ChallengeTaskController {

    @Autowired
    private ChallengeTaskRepository challengeTaskRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChallengeTaskService challengeTaskService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ChallengeParticipantRepository challengeParticipantRepository;

    @Autowired
    private ChallengeTaskProgressRepository  progressRepository;


    // ---------------- Create ChallengeTask ----------------
    @PostMapping("/create")
    public ResponseEntity<?> createChallengeTask(@RequestBody ChallengeTaskRequestDTO dto) {

        Challenge challenge = challengeRepository.findById(dto.getChallengeId())
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        ChallengeTask task = ChallengeTask.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .dueDate(dto.getDueDate())
                .status(dto.getStatus())
                .challenge(challenge)
                .build();

        ChallengeTask savedTask = challengeTaskRepository.save(task);

        if (savedTask.getStatus() != ChallengeTaskStatus.COMPLETED) {
            challenge.setStatus(ChallengeStatus.ONGOING);
            challengeRepository.save(challenge);
        }

        ChallengeTaskResponseDTO response = new ChallengeTaskResponseDTO();
        response.setId(savedTask.getId());
        response.setTitle(savedTask.getTitle());
        response.setDescription(savedTask.getDescription());
        response.setDueDate(savedTask.getDueDate());
        response.setStatus(savedTask.getStatus());

        return ResponseEntity.ok(response);
    }

//    // ---------------- Update ChallengeTask ----------------
//    @PutMapping("/update")
//    public ResponseEntity<?> updateChallengeTask(@RequestBody ChallengeTaskUpdateDTO dto) {
//        ChallengeTask task = challengeTaskRepository.findById(dto.getTaskId())
//                .orElseThrow(() -> new RuntimeException("ChallengeTask not found"));
//
//        if (dto.getTitle() != null) task.setTitle(dto.getTitle());
//        if (dto.getDescription() != null) task.setDescription(dto.getDescription());
//        if (dto.getDueDate() != null) task.setDueDate(dto.getDueDate());
//        if (dto.getStatus() != null) task.setStatus(dto.getStatus());
//
//        challengeTaskRepository.save(task);
//
//        ChallengeTaskResponseDTO response = new ChallengeTaskResponseDTO();
//        response.setId(task.getId());
//        response.setTitle(task.getTitle());
//        response.setDescription(task.getDescription());
//        response.setDueDate(task.getDueDate());
//        response.setStatus(task.getStatus());
//
//        return ResponseEntity.ok(response);
//    }


    // ---------------- Update Status ----------------
    @PostMapping("/updateStatus")
    public ResponseEntity<?> updateChallengeTaskStatus(
            @RequestBody ChallengeTaskUpdateDTO dto,
            @RequestHeader("Authorization") String authHeader) {

        try {
            // 1. Validate task
            ChallengeTask task = challengeTaskRepository.findById(dto.getTaskId())
                    .orElseThrow(() -> new RuntimeException("ChallengeTask not found"));

            if (dto.getStatus() == null) {
                return ResponseEntity.badRequest().body("Status is required");
            }

            // 2. Get user from token
            String token = authHeader.replace("Bearer ", "");
            String userUuid = jwtUtil.extractUuid(token);
            User user = userRepository.findByUuid(userUuid)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 3. Check if progress already exists
            Optional<ChallengeTaskProgress> existingProgressOpt =
                    progressRepository.findByUserAndChallengeTaskId(user, dto.getTaskId());

            ChallengeTaskProgress progress;
            if (existingProgressOpt.isPresent()) {
                // Update existing progress
                progress = existingProgressOpt.get();
                progress.setStatus(dto.getStatus());

                if (dto.getStatus() == ChallengeTaskStatus.COMPLETED) {
                    progress.setCompletedOn(LocalDate.now());
                } else {
                    progress.setCompletedOn(null);
                }

            } else {
                // Create new progress record
                progress = new ChallengeTaskProgress();
                progress.setUser(user);
                progress.setChallengeTask(task);
                progress.setStatus(dto.getStatus());

                if (dto.getStatus() == ChallengeTaskStatus.COMPLETED) {
                    progress.setCompletedOn(LocalDate.now());
                }
            }

            progressRepository.save(progress);

            // 5. Response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "ChallengeTaskProgress updated");
            response.put("taskId", task.getId());
            response.put("status", progress.getStatus());
            response.put("completedOn", progress.getCompletedOn());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }


    //Fetch Tasks Challenge Id
    @GetMapping("/fetch/{challengeId}")
    public ResponseEntity<?> getTasksByGoal(@PathVariable Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        List<ChallengeTask> tasks = challengeTaskRepository.findByChallengeId(challengeId);

        List<ChallengeTaskResponseDTO> response = tasks.stream().map( task -> {
            ChallengeTaskResponseDTO dto = new ChallengeTaskResponseDTO();
            dto.setId(task.getId());
            dto.setTitle(task.getTitle());
            dto.setDescription(task.getDescription());
            dto.setDueDate(task.getDueDate());
            dto.setStatus(task.getStatus());
            return dto;
        }).toList();

        return ResponseEntity.ok(response);
    }


    // ---------------- Fetch Today's ChallengeTasks ----------------
    @GetMapping({"/today", "/today/{status}"})
    public ResponseEntity<?> getTodaysChallengeTasks(
            @PathVariable(required = false) ChallengeTaskStatus status,
            HttpServletRequest request) {

        String token = request.getHeader("Authorization").substring(7);
        String userUuid = jwtUtil.extractUuid(token);
        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();

        // Get user's ongoing challenges
        List<Long> challengeIds = challengeParticipantRepository.findByUser(user).stream()
                .map(ChallengeParticipant::getChallenge)
                .filter(c -> c.getStatus() == ChallengeStatus.ONGOING)
                .map(Challenge::getId)
                .toList();

        if (challengeIds.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // Fetch today's tasks
        List<ChallengeTask> tasks = (status != null)
                ? challengeTaskRepository.findByChallengeIdInAndDueDateAndStatus(challengeIds, today, status)
                : challengeTaskRepository.findByChallengeIdInAndDueDate(challengeIds, today);

        // Fetch progress for these tasks for the logged-in user
        List<Long> taskIds = tasks.stream().map(ChallengeTask::getId).toList();
        Map<Long, ChallengeTaskProgress> progressMap = progressRepository.findByUserAndChallengeTaskIdIn(user, taskIds)
                .stream()
                .collect(Collectors.toMap(p -> p.getChallengeTask().getId(), p -> p));

        // Map to DTO
        List<ChallengeTaskResponseDTO> response = tasks.stream().map(task -> {
            ChallengeTaskResponseDTO dto = new ChallengeTaskResponseDTO();
            dto.setId(task.getId());
            dto.setTitle(task.getTitle());
            dto.setDescription(task.getDescription());
            dto.setDueDate(task.getDueDate());
            dto.setChallengeId(task.getChallenge().getId());
            dto.setChallengeTitle(task.getChallenge().getTitle());
            ChallengeTaskProgress progress = progressMap.get(task.getId());
            dto.setStatus(progress != null ? progress.getStatus() : ChallengeTaskStatus.PENDING);
//            dto.setCompletedOn(progress != null ? progress.getCompletedOn() : null);
            return dto;
        }).filter(dto -> status == null || dto.getStatus() == status)
                .toList();


        return ResponseEntity.ok(response);
    }


//    @DeleteMapping("/{id}")
////    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
//    public ResponseEntity<String> deleteTask(@PathVariable("id") Long id) {
//        boolean deleted = challengeTaskService.deleteTaskById(id);
//
//        if (deleted) {
//            return ResponseEntity
//                    .status(HttpStatus.ACCEPTED) // 202
//                    .body("Task deleted successfully");
//        } else {
//            return ResponseEntity
//                    .status(HttpStatus.NOT_FOUND) // 404
//                    .body("Task not found");
//        }
//    }


}
