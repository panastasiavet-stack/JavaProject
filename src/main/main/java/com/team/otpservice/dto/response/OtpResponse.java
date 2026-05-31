package main.main.java.com.team.otpservice.dto.response;

public record OtpResponse(Long otpId, String operationKey, String status, String channel, long expiresAt) {
}
