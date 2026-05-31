package main.main.java.com.team.otpservice.config;

public record MailConfig(
        String username,
        String password,
        String from,
        String host,
        int port,
        boolean auth,
        boolean startTls
) {
    public boolean enabled() {
        return !host.isBlank() && !username.isBlank() && !password.isBlank() && !from.isBlank();
    }
}
