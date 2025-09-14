package com.nudgeme.nudgeme.dto;

import com.nudgeme.nudgeme.model.User;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @AllArgsConstructor
@Data
public class UserProfileResponseDTO {
    private String location;
    private String bio;
    private String image;
    private LocalDate dob;
}
