package com.nudgeme.nudgeme.service;

import com.nudgeme.nudgeme.dto.UserProfileUpdateRequestDTO;
import com.nudgeme.nudgeme.dto.UserUpdateRequestDTO;
import com.nudgeme.nudgeme.model.ResetToken;
import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.model.UserProfile;
import com.nudgeme.nudgeme.repository.ResetTokenRepository;
import com.nudgeme.nudgeme.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResetTokenRepository resetTokenRepository;

    // Constructor injection ensures final fields are initialized
    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ResetTokenRepository resetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.resetTokenRepository = resetTokenRepository;
    }

    public boolean resetPassword(String email, String token, String newPassword) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) return false;

        User user = optionalUser.get();

        Optional<ResetToken> resetTokenOpt = resetTokenRepository.findByTokenAndUser(token, user);
        if (resetTokenOpt.isEmpty() || resetTokenOpt.get().isExpired()) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetTokenRepository.delete(resetTokenOpt.get()); // remove used token
        return true;
    }


    public User updateUser(Long userId, UserUpdateRequestDTO request) {
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if username is provided & different
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername()) && !request.getFullName().trim().isEmpty()) {
            // Check if username already exists
//            boolean exists = userRepository.existsByUsername(request.getUsername());
//            if (exists) {
//                throw new RuntimeException("Username already taken");
//            }
            user.setUsername(request.getUsername());
        }

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName());
        }

        // Save and return updated user
        return userRepository.save(user);
    }

}
