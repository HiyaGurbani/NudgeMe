package com.nudgeme.nudgeme.repository;

import com.nudgeme.nudgeme.model.Goal;
import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.model.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUserAndStatus(User user, GoalStatus status);
    List<Goal> findByUser(User user);

    long countByUserAndStatus(User user, GoalStatus status);
    long countByUser(User user);
}
