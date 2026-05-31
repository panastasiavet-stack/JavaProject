package main.main.java.com.team.otpservice.dto.response;

public record OtpValidationResponse(boolean valid, String status, String message) {
}
