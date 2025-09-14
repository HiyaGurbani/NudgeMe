package com.nudgeme.nudgeme.repository;

import com.nudgeme.nudgeme.model.*;
import com.nudgeme.nudgeme.model.enums.ChallengeTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChallengeTaskRepository extends JpaRepository<ChallengeTask, Long> {
    List<ChallengeTask> findByChallengeId(Long challengeId);

    @Query("SELECT t FROM ChallengeTask t WHERE t.challenge.user = :user AND t.dueDate = :date")
    List<ChallengeTask> findByUserAndDueDate(@Param("user") User user, @Param("date") LocalDate date);

    @Query("SELECT t FROM ChallengeTask t WHERE t.challenge.user = :user AND t.dueDate = :date AND t.status = :status")
    List<ChallengeTask> findByUserAndDueDateAndStatus(@Param("user") User user,
                                             @Param("date") LocalDate date,
                                             @Param("status") ChallengeTaskStatus status);

    @Query("SELECT t FROM ChallengeTask t WHERE t.challenge.user.uuid = :userUuid AND t.status = 'COMPLETED' ORDER BY t.completedOn DESC")
    List<ChallengeTask> findCompletedTasksByUser(@Param("userUuid") String userUuid);

    @Query("SELECT COUNT(t) FROM ChallengeTask t WHERE t.challenge = :challenge AND t.status <> :completed")
    long countIncompleteTasksByChallenge(@Param("challenge") Challenge challenge,
                                    @Param("completed") ChallengeTaskStatus completed);

    List<ChallengeTask> findByChallengeIdInAndDueDate(
            List<Long> challengeIds, LocalDate dueDate);

    List<ChallengeTask> findByChallengeIdInAndDueDateAndStatus(
            List<Long> challengeIds, LocalDate dueDate, ChallengeTaskStatus status);


//    long countByChallengeAndStatus(Challenge challenge, TaskStatus status);



}
