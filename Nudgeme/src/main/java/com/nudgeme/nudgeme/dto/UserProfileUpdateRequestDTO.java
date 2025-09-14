package com.nudgeme.nudgeme.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileUpdateRequestDTO {
    private String location;
    private String bio;
    private String image;
    private LocalDate dob;
}
