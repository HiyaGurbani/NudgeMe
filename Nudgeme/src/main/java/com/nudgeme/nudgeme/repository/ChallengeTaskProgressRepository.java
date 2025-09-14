package com.nudgeme.nudgeme.repository;

import com.nudgeme.nudgeme.model.ChallengeTask;
import com.nudgeme.nudgeme.model.ChallengeTaskProgress;
import com.nudgeme.nudgeme.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeTaskProgressRepository extends JpaRepository<ChallengeTaskProgress, Long> {

    // find progress for a user across multiple tasks
    List<ChallengeTaskProgress> findByUserAndChallengeTaskIdIn(User user, List<Long> taskIds);

    // optionally if you want a single record
    Optional<ChallengeTaskProgress> findByUserAndChallengeTaskId(User user, Long taskId);

}

