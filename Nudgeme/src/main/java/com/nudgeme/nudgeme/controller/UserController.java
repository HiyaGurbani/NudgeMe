package com.nudgeme.nudgeme.controller;

import com.nudgeme.nudgeme.dto.UserResponseDTO;
import com.nudgeme.nudgeme.dto.UserUpdateRequestDTO;
import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.repository.UserRepository;
import com.nudgeme.nudgeme.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.nudgeme.nudgeme.security.JwtUtil;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getUser(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
            }

            String token = authHeader.substring(7); // remove "Bearer "
            String userUuid = jwtUtil.extractUuid(token);

            User user = userRepository.findByUuid(userUuid)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserResponseDTO response = new UserResponseDTO(user.getUsername(), user.getEmail(), user.getFullName(), user.getJoined_at());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }
    }


    @PutMapping("/update")
    public ResponseEntity<?> updateUser(
            HttpServletRequest httpRequest,
            @RequestBody UserUpdateRequestDTO userUpdateDTO) {
        try {
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Missing or invalid token");
            }

            String token = authHeader.substring(7);
            String userUuid = jwtUtil.extractUuid(token);

            User user = userRepository.findByUuid(userUuid)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String newUsername = userUpdateDTO.getUsername();

            if (newUsername != null && !newUsername.equals(user.getUsername())) {
                boolean exists = userRepository.existsByUsername(newUsername);
                if (exists) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Username already taken"));
                }
            }

            User updatedUser = userService.updateUser(user.getId(), userUpdateDTO);

            return ResponseEntity.ok(updatedUser);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token");
        }
    }




}
