package com.nudgeme.nudgeme.controller;


import com.nudgeme.nudgeme.dto.ParticipantStatsResponseDTO;
import com.nudgeme.nudgeme.dto.RewardsStatsResponseDTO;
import com.nudgeme.nudgeme.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class HostAnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/participants/monthly")
    public ResponseEntity<ParticipantStatsResponseDTO> getMonthlyStats(
            @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        return ResponseEntity.ok(analyticsService.getMonthlyParticipantStats(jwt));
    }

    @GetMapping("/rewards/monthly")
    public ResponseEntity<RewardsStatsResponseDTO> getMonthlyRewards(
            @RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        return ResponseEntity.ok(analyticsService.getMonthlyRewards(jwt));
    }
}
