package com.nudgeme.nudgeme.repository;

import com.nudgeme.nudgeme.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByPublicAchievementTrue();
    Achievement findById(long id);
}
