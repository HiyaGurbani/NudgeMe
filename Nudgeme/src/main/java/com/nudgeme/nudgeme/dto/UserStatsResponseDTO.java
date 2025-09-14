package com.nudgeme.nudgeme.dto;

import com.nudgeme.nudgeme.model.User;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @AllArgsConstructor
@Data
public class UserStatsResponseDTO {
    private int RewardPoints;
}
