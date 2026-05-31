package main.main.java.com.team.otpservice.model;

import java.time.LocalDateTime;

public record Operation(
        Long id,
        Long userId,
        String operationKey,
        String description,
        LocalDateTime createdAt
) {
}
