package com.nudgeme.nudgeme.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserAchievementResponseDTO {
    private Long achievementId;

    @JsonProperty("earned_at")
    private LocalDate earnedAt;

    private String name;

    @JsonProperty("sub_line")
    private String subLine;

    @JsonProperty("banner_image")
    private String bannerImage;

    public UserAchievementResponseDTO(Long achievementId, LocalDate earnedAt,
                                      String name, String subLine, String bannerImage) {
        this.achievementId = achievementId;
        this.earnedAt = earnedAt;
        this.name = name;
        this.subLine = subLine;
        this.bannerImage = bannerImage;
    }
}
