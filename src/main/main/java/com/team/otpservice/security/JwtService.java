package main.main.java.com.team.otpservice.security;

import com.team.otpservice.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

public class JwtService {
    private final JwtConfig config;
    private final SecretKey secretKey;

    public JwtService(JwtConfig config) {
        this.config = config;
        this.secretKey = Keys.hmacShaKeyFor(config.secret().getBytes(StandardCharsets.UTF_8));
    }

    public TokenData generateToken(Long userId, String login, String role) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(config.ttlSeconds());
        String token = Jwts.builder()
                .subject(login)
                .issuer(config.issuer())
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
        return new TokenData(token, expiresAt.getEpochSecond());
    }

    public AuthContext parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Long userId = ((Number) claims.get("userId")).longValue();
        String login = claims.getSubject();
        String role = String.valueOf(claims.get("role"));
        return new AuthContext(userId, login, role);
    }

    public record TokenData(String token, long expiresAtEpochSeconds) {
    }
}
