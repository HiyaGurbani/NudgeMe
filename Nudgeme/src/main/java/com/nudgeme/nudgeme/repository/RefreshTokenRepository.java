package com.nudgeme.nudgeme.repository;

import com.nudgeme.nudgeme.model.RefreshToken;
import com.nudgeme.nudgeme.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUserId(Long userId);
    void deleteByUser(User user);
}
