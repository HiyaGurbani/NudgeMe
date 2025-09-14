package com.nudgeme.nudgeme.service;
import com.nudgeme.nudgeme.model.UserStreak;
import com.nudgeme.nudgeme.repository.TaskRepository;
import com.nudgeme.nudgeme.repository.UserStreakRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
@Service
public class TaskService {

    @Autowired
    private UserStreakRepository userStreakRepository;

    @Autowired
    private TaskRepository taskRepository;

    public void updateStreakOnTaskCompletion(String userUuid) {
        LocalDate today = LocalDate.now();


        UserStreak streak = userStreakRepository.findByUserUuid(userUuid)
                .orElse(UserStreak.builder()
                        .userUuid(userUuid)
                        .streakCount(0)
                        .maxStreak(0)
                        .lastActive(today.minusDays(1)) // init to yesterday to start counting
                        .build());

        long daysBetween = streak.getLastActive() != null ?
                java.time.temporal.ChronoUnit.DAYS.between(streak.getLastActive(), today) : 0;

        if (daysBetween == 1) {
            // consecutive day
            streak.setStreakCount(streak.getStreakCount() + 1);
        } else if (daysBetween == 0) {
            // same day task, do nothing
        } else {
            // streak broken
            streak.setStreakCount(1);
        }

        streak.setLastActive(today);

        // update max streak if current streak is higher
        if (streak.getStreakCount() > streak.getMaxStreak()) {
            streak.setMaxStreak(streak.getStreakCount());
        }

        userStreakRepository.save(streak);
    }

    public UserStreak getUserStreak(String userUuid) {
        return userStreakRepository.findByUserUuid(userUuid)
                .orElse(UserStreak.builder()
                        .userUuid(userUuid)
                        .streakCount(0)
                        .maxStreak(0)
                        .lastActive(null) // or LocalDate.now().minusDays(1)
                        .build());
    }

    public boolean deleteTaskById(Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
