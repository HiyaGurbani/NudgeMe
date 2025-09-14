package com.nudgeme.nudgeme.service;

import com.nudgeme.nudgeme.dto.AchievementResponseDTO;
import com.nudgeme.nudgeme.dto.ChallengeResponseDTO;
import com.nudgeme.nudgeme.model.Achievement;
import com.nudgeme.nudgeme.model.Challenge;
import com.nudgeme.nudgeme.model.User;
import com.nudgeme.nudgeme.model.enums.ChallengeStatus;
import com.nudgeme.nudgeme.repository.ChallengeRepository;
import com.nudgeme.nudgeme.repository.UserRepository;
import com.nudgeme.nudgeme.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    public ChallengeService(ChallengeRepository challengeRepository, UserRepository userRepository) {
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
    }

    public Challenge createChallenge(Challenge challenge, Long hostId) {
        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new RuntimeException("Host not found"));

        if (!"HOST".equalsIgnoreCase(host.getRole())) {
            throw new RuntimeException("Only hosts can create challenges!");
        }

        challenge.setUser(host);
        return challengeRepository.save(challenge);
    }

    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAll();
    }

    public List<Challenge> getChallengesByStatus(ChallengeStatus status) {
        return challengeRepository.findByStatus(status);
    }


    /// Host APIS
    @Autowired
    private JwtUtil jwtUtil;

        public List<ChallengeResponseDTO> getHostChallenges(String token) {
            String username = jwtUtil.extractUsername(token);
            User host = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Host not found"));

            return challengeRepository.findByUser(host)
                    .stream()
                    .map(this::mapToDTO)
                    .toList();
        }

        public List<ChallengeResponseDTO> getHostChallengesByStatus(String token, ChallengeStatus status) {
            String username = jwtUtil.extractUsername(token);
            User host = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Host not found"));

            return challengeRepository.findByUserAndStatus(host, status)
                    .stream()
                    .map(this::mapToDTO)
                    .toList();
        }

        public List<ChallengeResponseDTO> getHostRecentChallenges(String token) {
            String username = jwtUtil.extractUsername(token);
            User host = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Host not found"));

            return challengeRepository.findByUserOrderByStartDateDesc(host)
                    .stream()
                    .map(this::mapToDTO)
                    .toList();
        }

        private ChallengeResponseDTO mapToDTO(Challenge challenge) {
            return ChallengeResponseDTO.builder()
                    .id(challenge.getId())
                    .title(challenge.getTitle())
                    .description(challenge.getDescription())
                    .startDate(challenge.getStartDate())
                    .endDate(challenge.getEndDate())
                    .participants((long) challenge.getParticipants()) // Assuming Set<User> participants
                    .rewardPoints(challenge.getRewardPoints())
                    .difficulty(challenge.getDifficulty())
                    .category(challenge.getCategory())
                    .status(challenge.getStatus())
                    .creatorType(challenge.getCreatorType())
                    .creatorId(challenge.getUser().getId())
                    .creatorName(challenge.getUser().getUsername()) // or getFullName()
                    .achievement(mapAchievement(challenge.getAchievement()))
                    .build();
        }

        private AchievementResponseDTO mapAchievement(Achievement achievement) {
            if (achievement == null) return null;
            return AchievementResponseDTO.builder()
                    .id(achievement.getId())
                    .name(achievement.getName())
                    .subline(achievement.getSubline())
                    .bannerImage(achievement.getBannerImage())
                    .publicAchievement(achievement.isPublicAchievement())
                    .createdById(achievement.getCreatedBy().getId())
                    .createdByUsername(achievement.getCreatedBy().getUsername())
                    .build();
        }

}
