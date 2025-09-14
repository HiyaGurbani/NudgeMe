package com.nudgeme.nudgeme.controller;
import com.nudgeme.nudgeme.dto.TaskRequestDTO;
import com.nudgeme.nudgeme.dto.TaskResponseDTO;
import com.nudgeme.nudgeme.dto.TaskUpdateDTO;
import com.nudgeme.nudgeme.model.Goal;
import com.nudgeme.nudgeme.model.Task;
import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.model.UserStreak;
import com.nudgeme.nudgeme.model.enums.GoalStatus;
import com.nudgeme.nudgeme.model.enums.TaskStatus;
import com.nudgeme.nudgeme.repository.GoalRepository;
import com.nudgeme.nudgeme.repository.TaskRepository;
import com.nudgeme.nudgeme.repository.UserRepository;
import com.nudgeme.nudgeme.security.JwtUtil;
import com.nudgeme.nudgeme.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:3001" })
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TaskService taskService;


    @PostMapping("/create")
    public ResponseEntity<?> createTask(@RequestBody TaskRequestDTO dto) {

        Goal goal = goalRepository.findById(dto.getGoalId())
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        Task task = Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .dueDate(dto.getDueDate())
                .status(dto.getStatus()) // convert string to enum
                .goal(goal)
                .build();

        Task savedTask = taskRepository.save(task);

        // 🔹 Update goal status if new task is NOT completed
        if (savedTask.getStatus() != TaskStatus.COMPLETED) {
            goal.setStatus(GoalStatus.IN_PROGRESS);
            goalRepository.save(goal);
        }


        TaskResponseDTO response = new TaskResponseDTO();
        response.setId(savedTask.getId());
        response.setTitle(savedTask.getTitle());
        response.setDescription(savedTask.getDescription());
        response.setDueDate(savedTask.getDueDate());
        response.setStatus(savedTask.getStatus());
        response.setGoalId(savedTask.getGoal().getId());

        return ResponseEntity.ok(response);
    }

    // Full update of task (title, description, dueDate, status)
    @PutMapping("/update")
    public ResponseEntity<?> updateTask(@RequestBody TaskUpdateDTO dto) {
        Task task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (dto.getTitle() != null) task.setTitle(dto.getTitle());
        if (dto.getDescription() != null) task.setDescription(dto.getDescription());
        if (dto.getDueDate() != null) task.setDueDate(dto.getDueDate());
        if (dto.getStatus() != null) task.setStatus(dto.getStatus());

        taskRepository.save(task);

        TaskResponseDTO response = new TaskResponseDTO();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setDueDate(task.getDueDate());
        response.setStatus(task.getStatus());

        return ResponseEntity.ok(response);
    }

    // Partial update of task — only status
    @PatchMapping("/updateStatus")
    public ResponseEntity<?> updateTaskStatus(@RequestBody TaskUpdateDTO dto,
                                              @RequestHeader("Authorization") String authHeader) {
        try {
            // Fetch task
            Task task = taskRepository.findById(dto.getTaskId())
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            // Check status
            if (dto.getStatus() == null) {
                System.err.println("Bad Request: status is null. Incoming DTO: " + dto);
                return ResponseEntity.status(400).body("Status is required");
            }

            TaskStatus status = dto.getStatus();
            task.setStatus(status);

            // Extract user UUID from token
            String token = authHeader.replace("Bearer ", "");
            String userUuid = jwtUtil.extractUuid(token);

            // Handle COMPLETED
            if (status == TaskStatus.COMPLETED) {
                task.setCompletedOn(LocalDate.now());
                taskService.updateStreakOnTaskCompletion(userUuid);
            }
            // Handle PENDING
            else if (status == TaskStatus.PENDING) {
                task.setCompletedOn(null);
                // taskService.revertStreakOnTaskPending(userUuid); // optional
            }

            // Save task
            taskRepository.save(task);

            // Update goal status if needed
            Goal goal = task.getGoal();
            if (goal != null) {
                long incomplete = taskRepository.countIncompleteTasksByGoal(goal, TaskStatus.COMPLETED);
                if (incomplete == 0) {
                    goal.setStatus(GoalStatus.COMPLETED);
                } else {
                    goal.setStatus(GoalStatus.IN_PROGRESS);
                }
                goalRepository.save(goal);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Task status updated");
            response.put("taskId", task.getId());
            response.put("status", task.getStatus());
            if (task.getCompletedOn() != null) {
                response.put("completedOn", task.getCompletedOn());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Log full stack trace for debugging
            e.printStackTrace();
            System.err.println("Error updating task status. DTO: " + dto);
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/current-streak")
    public ResponseEntity<?> getCurrentStreak(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String userUuid = jwtUtil.extractUuid(token); // extract UUID from JWT

            UserStreak userStreak = taskService.getUserStreak(userUuid); // fetch from DB
            LocalDate today = LocalDate.now();

            if (userStreak == null) {
                return ResponseEntity.ok(Map.of(
                        "currentStreak", 0,
                        "maxStreak", 0
                ));
            }

            // Check if streak is broken due to inactivity
            if (userStreak.getLastActive() == null ||
                    java.time.temporal.ChronoUnit.DAYS.between(userStreak.getLastActive(), today) > 1) {
                return ResponseEntity.ok(Map.of(
                        "currentStreak", 0,
                        "maxStreak", userStreak.getMaxStreak() // keep max streak even if current is reset
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "currentStreak", userStreak.getStreakCount(),
                    "maxStreak", userStreak.getMaxStreak()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }



    //Fetch Tasks by goal Id
    @GetMapping("/fetch/{goalId}")
    public ResponseEntity<?> getTasksByGoal(@PathVariable Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        List<Task> tasks = taskRepository.findByGoalId(goalId);

        List<TaskResponseDTO> response = tasks.stream().map(task -> {
            TaskResponseDTO dto = new TaskResponseDTO();
            dto.setId(task.getId());
            dto.setTitle(task.getTitle());
            dto.setDescription(task.getDescription());
            dto.setDueDate(task.getDueDate());
            dto.setStatus(task.getStatus());
            return dto;
        }).toList();

        return ResponseEntity.ok(response);
    }


    //Fetch today's tasks
    @GetMapping({"/today", "/today/{status}"})
    public ResponseEntity<?> getTodaysTasks(
            @PathVariable(required = false) TaskStatus status,
            HttpServletRequest request) {

        // 1️⃣ Extract user from JWT
        String token = request.getHeader("Authorization").substring(7);
        String userUuid = jwtUtil.extractUuid(token);
        User user = userRepository.findByUuid(userUuid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        List<Task> tasks;

        // 2️⃣ Fetch tasks with or without status filter
        if (status != null) {
            tasks = taskRepository.findByUserAndDueDateAndStatus(user, today, status);
        } else {
            tasks = taskRepository.findByUserAndDueDate(user, today);
        }
        System.out.println("Found tasks: " + tasks.size());
        tasks.forEach(t -> System.out.println(t.getTitle() + " - " + t.getStatus() + " - " + t.getDueDate()));


        // 3️⃣ Map to DTO
        List<TaskResponseDTO> response = tasks.stream().map(task -> {
            TaskResponseDTO dto = new TaskResponseDTO();
            dto.setId(task.getId());
            dto.setTitle(task.getTitle());
            dto.setDescription(task.getDescription());
            dto.setDueDate(task.getDueDate());
            dto.setStatus(task.getStatus());
            dto.setGoalId(task.getGoal().getId());
            return dto;
        }).toList();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable("id") Long id) {
        boolean deleted = taskService.deleteTaskById(id);

        if (deleted) {
            return ResponseEntity
                    .status(HttpStatus.ACCEPTED) // 202
                    .body("Task deleted successfully");
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND) // 404
                    .body("Task not found");
        }
    }





}
