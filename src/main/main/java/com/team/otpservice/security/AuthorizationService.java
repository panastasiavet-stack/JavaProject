package main.main.java.com.team.otpservice.security;

import com.team.otpservice.exception.ForbiddenException;
import com.team.otpservice.exception.UnauthorizedException;

public class AuthorizationService {
    private final JwtService jwtService;

    public AuthorizationService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public AuthContext authenticate(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank() || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        try {
            return jwtService.parse(token);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid token");
        }
    }

    public void requireAdmin(AuthContext authContext) {
        if (!"ADMIN".equals(authContext.role())) {
            throw new ForbiddenException("Administrator access required");
        }
    }
}
