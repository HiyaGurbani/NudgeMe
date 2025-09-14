package com.nudgeme.nudgeme.service;


import com.nudgeme.nudgeme.dto.ParticipantStatsResponseDTO;
import com.nudgeme.nudgeme.dto.RewardsStatsResponseDTO;
import com.nudgeme.nudgeme.model.Challenge;
import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.model.enums.ChallengeStatus;
import com.nudgeme.nudgeme.repository.ChallengeRepository;
import com.nudgeme.nudgeme.repository.UserRepository;
import com.nudgeme.nudgeme.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public ParticipantStatsResponseDTO getMonthlyParticipantStats(String token) {
        String username = jwtUtil.extractUsername(token);
        User host = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Host not found"));

        // ✅ Current month range
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        // ✅ Fetch all challenges of host in this month
        List<Challenge> challenges = challengeRepository.findByUserAndStartDateBetween(
                host, startOfMonth, endOfMonth);

        long total = 0L;
        long max = 0L;

        for (Challenge c : challenges) {
            Long participants = c.getParticipants(); // assuming participants is Long
            if (participants != null) {
                total += participants;
                max = Math.max(max, participants);
            }
        }

        return new ParticipantStatsResponseDTO(total, max);
    }

    public RewardsStatsResponseDTO getMonthlyRewards(String token) {
        String username = jwtUtil.extractUsername(token);
        User host = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Host not found"));

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        // ✅ Only COMPLETED challenges this month
        List<Challenge> completedChallenges = challengeRepository.findByUserAndStatusAndEndDateBetween(
                host, ChallengeStatus.COMPLETED, startOfMonth, endOfMonth);

        long totalRewards = 0L;

        for (Challenge c : completedChallenges) {
            Long participants = c.getParticipants();   // Assuming this is Long
            Integer rewardPoints = c.getRewardPoints();

            if (participants != null && rewardPoints != null) {
                totalRewards += participants * rewardPoints;
            }
        }

        return new RewardsStatsResponseDTO(totalRewards);
    }
}

