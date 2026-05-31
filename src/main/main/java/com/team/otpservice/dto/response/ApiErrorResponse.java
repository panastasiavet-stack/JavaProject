package main.main.java.com.team.otpservice.dto.response;

public record ApiErrorResponse(String error, String message, String path, long timestamp) {
}
