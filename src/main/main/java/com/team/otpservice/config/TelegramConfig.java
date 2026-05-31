package main.main.java.com.team.otpservice.config;

public record TelegramConfig(String botToken, String apiUrlTemplate) {
    public boolean enabled() {
        return !botToken.isBlank();
    }
}
