package main.main.java.com.team.otpservice.dto.response;

public record UserResponse(Long id, String login, String role, String email, String phone, String telegramChatId) {
}
