package com.nudgeme.nudgeme.service;

import com.nudgeme.nudgeme.model.Goal;
import com.nudgeme.nudgeme.repository.GoalRepository;
import org.springframework.stereotype.Service;

@Service
public class GoalService {

    private final GoalRepository goalRepository;

    public GoalService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    public Goal createGoal(Goal goal) {
        return goalRepository.save(goal);
    }
}
