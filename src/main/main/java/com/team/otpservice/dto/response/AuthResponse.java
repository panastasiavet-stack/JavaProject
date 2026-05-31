package main.main.java.com.team.otpservice.dto.response;

public record AuthResponse(String token, long expiresAt, String role) {
}
