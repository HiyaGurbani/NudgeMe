package com.nudgeme.nudgeme.repository;

import com.nudgeme.nudgeme.model.Challenge;
import com.nudgeme.nudgeme.model.Goal;
import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.model.enums.ChallengeStatus;
import com.nudgeme.nudgeme.model.enums.GoalStatus;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    List<Challenge> findByStatus(ChallengeStatus status);

    List<Challenge> findByUserAndStatus(User user, GoalStatus status);
    List<Challenge> findByUser(User user);

    long countByUserAndStatus(User user, GoalStatus status);
    long countByUser(User user);

    List<Challenge> findByUserAndStatus(User user, ChallengeStatus status);

    List<Challenge> findByUserAndStartDateBetween(User user, LocalDate start, LocalDate end);

    List<Challenge> findByUserOrderByStartDateDesc(User user);

    List<Challenge> findByUserAndStatusAndEndDateBetween(
            User user,
            ChallengeStatus status,
            LocalDate start,
            LocalDate end
    );

    @Query("""
    SELECT c FROM Challenge c
    WHERE c.status = :status
    AND c.id NOT IN (
        SELECT cp.challenge.id FROM ChallengeParticipant cp WHERE cp.user.id = :userId
    )
""")
    List<Challenge> findByStatusNotJoinedByUser(@Param("status") ChallengeStatus status,
                                                @Param("userId") Long userId);


}
