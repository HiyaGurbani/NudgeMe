package com.nudgeme.nudgeme.repository;


import com.nudgeme.nudgeme.model.Challenge;
import com.nudgeme.nudgeme.model.ChallengeParticipant;
import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.model.enums.ChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChallengeParticipantRepository extends JpaRepository<ChallengeParticipant, Long> {
    boolean existsByChallengeAndUser(Challenge challenge, User user);
    List<ChallengeParticipant> findByChallenge(Challenge challenge);
    Optional<ChallengeParticipant> findByChallengeAndUser(Challenge challenge, User user);
    @Query("SELECT cp.challenge FROM ChallengeParticipant cp WHERE cp.user.id = :userId")
    List<Challenge> findChallengesByUserId(@Param("userId") Long userId);
    List<ChallengeParticipant> findByUser(User user);
    List<ChallengeParticipant> findByUserAndChallengeStatus(User user, ChallengeStatus status);

}
