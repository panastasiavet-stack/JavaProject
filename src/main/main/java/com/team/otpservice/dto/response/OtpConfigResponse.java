package main.main.java.com.team.otpservice.dto.response;

public record OtpConfigResponse(int codeLength, int ttlSeconds, long updatedAt) {
}
