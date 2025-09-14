package com.nudgeme.nudgeme.dto;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter @AllArgsConstructor
@Data
public class UserResponseDTO {
    private String username;
    private String email;
    private String fullName;
    private LocalDate joinedAt;
}
