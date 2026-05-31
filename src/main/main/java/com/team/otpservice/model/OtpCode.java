package main.main.java.com.team.otpservice.model;

import java.time.LocalDateTime;

public record OtpCode(
        Long id,
        Long userId,
        Long operationId,
        String code,
        OtpStatus status,
        DeliveryChannel channel,
        String destination,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime usedAt
) {
}
