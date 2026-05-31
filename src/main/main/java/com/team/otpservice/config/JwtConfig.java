package main.main.java.com.team.otpservice.config;

public record JwtConfig(String secret, long ttlSeconds, String issuer) {
}
