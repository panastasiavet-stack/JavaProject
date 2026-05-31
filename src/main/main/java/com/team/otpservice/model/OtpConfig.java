package main.main.java.com.team.otpservice.model;

import java.time.LocalDateTime;

public record OtpConfig(
        Long id,
        int codeLength,
        int ttlSeconds,
        LocalDateTime updatedAt
) {
}
