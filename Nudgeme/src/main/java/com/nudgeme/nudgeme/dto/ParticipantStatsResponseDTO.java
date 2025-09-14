package com.nudgeme.nudgeme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantStatsResponseDTO {
    private Long totalParticipants;
    private Long maxParticipantsInChallenge;
}
