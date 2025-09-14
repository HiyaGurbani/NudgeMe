package com.nudgeme.nudgeme.repository;

import com.nudgeme.nudgeme.model.ResetToken;
import com.nudgeme.nudgeme.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {

    // Find a reset token for a specific user
    Optional<ResetToken> findByTokenAndUser(String token, User user);

    // Optionally, find by token only
    Optional<ResetToken> findByToken(String token);
}
