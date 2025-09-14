package com.nudgeme.nudgeme.controller;

import com.nudgeme.nudgeme.dto.UserProfileResponseDTO;
import com.nudgeme.nudgeme.dto.UserProfileUpdateRequestDTO;
import com.nudgeme.nudgeme.dto.UserResponseDTO;
import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.model.UserProfile;
import com.nudgeme.nudgeme.repository.UserProfileRepository;
import com.nudgeme.nudgeme.repository.UserRepository;
import com.nudgeme.nudgeme.security.JwtUtil;
import com.nudgeme.nudgeme.service.CloudinaryService;
import com.nudgeme.nudgeme.service.UserProfileService;
import com.nudgeme.nudgeme.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/user-profile")
public class UserProfileController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    UserProfileRepository userProfileRepository;
    @Autowired
    UserProfileService userProfileService;

    @GetMapping("/fetch")
    public ResponseEntity<?> fetchUserProfile(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
            }

            String token = authHeader.substring(7); // remove "Bearer "
            String userUuid = jwtUtil.extractUuid(token);

            User user = userRepository.findByUuid(userUuid)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserProfile userProfile = userProfileRepository.findByUser(user).orElse(null);

            UserProfileResponseDTO response;
            if (userProfile == null) {
                response = new UserProfileResponseDTO(null, null, null, null);
            } else {
                response = new UserProfileResponseDTO(
                        userProfile.getLocation(),
                        userProfile.getBio(),
                        userProfile.getImage(),
                        userProfile.getDob()
                );
            }
            return ResponseEntity.ok(response);


        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }
    }



    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(
            HttpServletRequest httpRequest,
            @RequestBody UserProfileUpdateRequestDTO profileUpdateDTO) {
        try {
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Missing or invalid token");
            }

            String token = authHeader.substring(7); // remove "Bearer "
            String userUuid = jwtUtil.extractUuid(token);

            User user = userRepository.findByUuid(userUuid)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserProfile updatedProfile =
                    userProfileService.updateUserProfile(user.getId(), profileUpdateDTO);

            return ResponseEntity.ok(updatedProfile);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token");
        }
    }




    /// -------------------Image Upload---------------------------------------///
    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadProfileImage(HttpServletRequest request,
                                                @RequestParam("image") MultipartFile file) {
        try {
            // 🔹 Validate JWT
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
            }

            String token = authHeader.substring(7);
            String userUuid = jwtUtil.extractUuid(token);

            User user = userRepository.findByUuid(userUuid)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 🔹 Upload file to Cloudinary
            String imageUrl = cloudinaryService.uploadFile(file);

            // 🔹 Save Cloudinary URL in UserProfile
            UserProfile userProfile = userProfileRepository.findByUser(user)
                    .orElse(new UserProfile()); // if profile doesn't exist, create new one
            userProfile.setUser(user);
            userProfile.setImage(imageUrl);
            userProfileRepository.save(userProfile);

            // 🔹 Return the new URL
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error uploading file: " + e.getMessage()));
        }
    }


}
