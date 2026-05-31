package main.main.java.com.team.otpservice.model;

import java.time.LocalDateTime;

public record User(
        Long id,
        String login,
        String passwordHash,
        Role role,
        String email,
        String phone,
        String telegramChatId,
        LocalDateTime createdAt
) {
}
