
package com.nudgeme.nudgeme.service;
import com.nudgeme.nudgeme.model.UserStreak;
import com.nudgeme.nudgeme.repository.ChallengeTaskRepository;
import com.nudgeme.nudgeme.repository.UserStreakRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
@Service
public class ChallengeTaskService {

    @Autowired
    private ChallengeTaskRepository challengeTaskRepository;

    public boolean deleteTaskById(Long id) {
        if (challengeTaskRepository.existsById(id)) {
            challengeTaskRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
