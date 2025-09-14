package com.nudgeme.nudgeme.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("NudgeMe Password Reset");
        message.setText("Click the link below to reset your password:\n"
                + resetLink
                + "\n\nNote: This link is valid for the next 15 minutes.");

        mailSender.send(message);
    }
}

