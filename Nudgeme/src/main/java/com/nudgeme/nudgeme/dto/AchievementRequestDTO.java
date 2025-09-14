package com.nudgeme.nudgeme.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nudgeme.nudgeme.model.User;
import lombok.*;

@Data
public class AchievementRequestDTO {
    private String name;
    private String subline;
    private String bannerImage;
    private boolean publicAchievement;
    //No id for user we'll take it from jwt
}
