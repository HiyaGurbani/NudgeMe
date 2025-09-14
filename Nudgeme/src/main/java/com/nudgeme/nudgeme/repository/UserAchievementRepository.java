package com.nudgeme.nudgeme.repository;


import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUser(User user);
}

