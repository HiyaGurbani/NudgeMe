package com.nudgeme.nudgeme.dto;

import lombok.Data;

@Data
public class ResetPasswordDTO {
    private String email;       // User email
    private String token;       // Reset token sent via email
    private String newPassword; // New password
}
