package com.nudgeme.nudgeme.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementResponseDTO {
    private Long id;
    private String name;
    private String subline;
    private String bannerImage;
    private boolean publicAchievement ;
    private Long createdById;
    private String createdByUsername; // nice to include for frontend display
}
