package com.nudgeme.nudgeme.service;


import com.nudgeme.nudgeme.model.Challenge;
import com.nudgeme.nudgeme.model.ChallengeParticipant;
import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.model.enums.ChallengeStatus;
import com.nudgeme.nudgeme.repository.ChallengeParticipantRepository;
import com.nudgeme.nudgeme.repository.ChallengeRepository;
import com.nudgeme.nudgeme.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChallengeParticipantService {

    private final ChallengeParticipantRepository participantRepository;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    public String joinChallenge(Long challengeId, String username) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (challenge.getStatus() != ChallengeStatus.UPCOMING) {
            return "You can only join upcoming challenges.";
        }


        if (participantRepository.existsByChallengeAndUser(challenge, user)) {
            return "Already joined this challenge!";
        }

        ChallengeParticipant participant = ChallengeParticipant.builder()
                .challenge(challenge)
                .user(user)
                .joinedAt(LocalDate.now())
                .build();

        participantRepository.save(participant);

        // 👇 increment participant count in Challenge
        challenge.setParticipants(challenge.getParticipants() + 1);
        challengeRepository.save(challenge);

        return "Joined successfully!";
    }

    @Transactional
    public String cancelChallenge(Long challengeId, String username) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        if (!challenge.getStatus().equals(ChallengeStatus.UPCOMING)) {
                return "Cannot cancel. Challenge already started or completed.";
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<ChallengeParticipant> participantOpt =
                participantRepository.findByChallengeAndUser(challenge, user);

        if (participantOpt.isEmpty()) {
            throw new RuntimeException("User is not part of this challenge");
        }

        participantRepository.delete(participantOpt.get());

        // 👇 increment participant count in Challenge
        challenge.setParticipants(challenge.getParticipants() - 1);
        challengeRepository.save(challenge);


        return "You have successfully cancelled your participation.";
    }


    public List<User> getParticipants(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        return participantRepository.findByChallenge(challenge)
                .stream()
                .map(ChallengeParticipant::getUser)
                .toList();
    }
}

