package com.nudgeme.nudgeme.repository;

import com.nudgeme.nudgeme.model.UserStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStreakRepository extends JpaRepository<UserStreak, Long> {
    Optional<UserStreak> findByUserUuid(String userUuid);
}
