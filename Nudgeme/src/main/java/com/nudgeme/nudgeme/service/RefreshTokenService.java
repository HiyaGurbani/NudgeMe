package com.nudgeme.nudgeme.service;

import com.nudgeme.nudgeme.model.RefreshToken;
import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.repository.RefreshTokenRepository;
import com.nudgeme.nudgeme.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private final long refreshTokenDurationMs = 60L * 24 * 60 * 60 * 1000; // 60 days

    public RefreshToken createOrUpdateRefreshToken(User user) {
        // Check if a token already exists for this user
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getId())
                .orElse(new RefreshToken());

        // Set/update user and token
        refreshToken.setUser(user);
        refreshToken.setToken(generateRandomToken()); // your method to generate token
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000);
        refreshToken.setExpiryDate(expiryDate);

        // Save to DB (insert or update)
        return refreshTokenRepository.save(refreshToken);
    }

    private String generateRandomToken() {
        // generate secure random token
        return UUID.randomUUID().toString();
    }

    public boolean isTokenExpired(RefreshToken token) {
        return token.getExpiryDate().isBefore(LocalDateTime.now());
    }
}