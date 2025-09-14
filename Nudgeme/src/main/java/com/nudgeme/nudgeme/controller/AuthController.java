package com.nudgeme.nudgeme.controller;

import com.nudgeme.nudgeme.dto.ResetPasswordDTO;
import com.nudgeme.nudgeme.model.RefreshToken;
import com.nudgeme.nudgeme.model.ResetToken;
import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.repository.RefreshTokenRepository;
import com.nudgeme.nudgeme.repository.ResetTokenRepository;
import com.nudgeme.nudgeme.repository.UserRepository;
import com.nudgeme.nudgeme.security.JwtUtil;
import com.nudgeme.nudgeme.service.EmailService;
import com.nudgeme.nudgeme.service.RefreshTokenService;
import com.nudgeme.nudgeme.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:3001" })
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @Autowired
    private ResetTokenRepository resetTokenRepository;


    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {

        // 1️⃣ Validate username uniqueness
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already taken"));
        }

        // 2️⃣ Validate email format
        String email = user.getEmail();
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";


        if (email == null || !email.matches(emailRegex)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid email address"));
        }

        // 3️⃣ Check email uniqueness
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }

        // 4️⃣ Validate password length
        String password = user.getPassword();
        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters long"));
        }

        // 5️⃣ Validate full name
        if (user.getFullName() == null || user.getFullName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Full name is required"));
        }

        // 6️⃣ Generate UUID and encode password
        user.setUuid(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(password));

        // 7️⃣ Handle role
        String requestedRole = user.getRole();
        if (requestedRole != null && !requestedRole.isBlank()) {
            user.setRole(requestedRole.toUpperCase());
        } else {
            user.setRole("MEMBER");
        }

        user.setJoined_at(LocalDate.now());

        // 8️⃣ Save user
        userRepository.save(user);

        // 9️⃣ Generate tokens (JWT now includes username, fullName, email, role)
        String accessToken = jwtUtil.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createOrUpdateRefreshToken(user);

        // 🔟 Return response
        return ResponseEntity.ok(Map.of(
                "message", "User registered successfully",
                "accessToken", accessToken,
                "refreshToken", refreshToken.getToken()
        ));
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String identifier = loginRequest.get("identifier").trim();
            String password = loginRequest.get("password");

            Optional<User> existing;

            // Check if identifier is an email
            if (identifier.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                existing = userRepository.findByEmail(identifier);
            } else {
                existing = userRepository.findByUsername(identifier);
            }

            // 1️⃣ Fetch user by username
            User user = existing.orElseThrow(() -> new RuntimeException("Invalid username or password"));

            // 2️⃣ Verify password
            if (!passwordEncoder.matches(password, user.getPassword())) {
                return ResponseEntity.status(401).body(Map.of("error", "Username or password is incorrect"));
            }

            // 3️⃣ Generate access token
            String accessToken = jwtUtil.generateToken(user);

            // 4️⃣ Generate refresh token
            RefreshToken refreshToken = refreshTokenService.createOrUpdateRefreshToken(user);

            // 5️⃣ Return both tokens
            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "accessToken", accessToken,
                    "refreshToken", refreshToken.getToken()
            ));
        } catch (RuntimeException e) {
            // Generic message for both user-not-found and wrong password
            return ResponseEntity.status(401).body(Map.of("error", "Username or password is incorrect"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String requestRefreshToken = request.get("refreshToken");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshTokenService.isTokenExpired(refreshToken)) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired. Please login again.");
        }

        String newAccessToken = jwtUtil.generateToken(refreshToken.getUser());

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email").trim();

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 1️⃣ Generate a password reset token (JWT, short-lived)
            String resetToken = jwtUtil.generateResetToken(user); // create a method in JwtUtil

            // 2️⃣ Save the token in DB
            ResetToken tokenEntity = new ResetToken();
            tokenEntity.setToken(resetToken);
            tokenEntity.setUser(user);
            tokenEntity.setExpiryDate(Instant.now().plus(15, ChronoUnit.MINUTES)); // e.g., 15 mins expiry
            resetTokenRepository.save(tokenEntity);

            // 2️⃣ Send reset email
            String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        }

        // 3️⃣ Always return generic message
        return ResponseEntity.ok(Map.of(
                "message", "If this email exists, a password reset link has been sent."
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO dto) {
        try {
            boolean success = userService.resetPassword(dto.getEmail(), dto.getToken(), dto.getNewPassword());
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Password reset successful"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid token or token expired"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Something went wrong"));
        }
    }

}
