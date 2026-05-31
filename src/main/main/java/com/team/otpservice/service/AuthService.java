package main.main.java.com.team.otpservice.service;

import com.team.otpservice.dao.UserDao;
import com.team.otpservice.dto.request.LoginRequest;
import com.team.otpservice.dto.request.RegisterRequest;
import com.team.otpservice.dto.response.AuthResponse;
import com.team.otpservice.dto.response.UserResponse;
import com.team.otpservice.exception.ConflictException;
import com.team.otpservice.exception.UnauthorizedException;
import com.team.otpservice.exception.ValidationException;
import com.team.otpservice.model.Role;
import com.team.otpservice.model.User;
import com.team.otpservice.security.JwtService;
import com.team.otpservice.security.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserDao userDao;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public AuthService(UserDao userDao, PasswordService passwordService, JwtService jwtService) {
        this.userDao = userDao;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    public UserResponse register(RegisterRequest request) {
        validateRegister(request);

        Role role = Role.valueOf(request.role.trim().toUpperCase());
        if (role == Role.ADMIN && userDao.existsAdmin()) {
            throw new ConflictException("Administrator already exists");
        }

        userDao.findByLogin(request.login.trim()).ifPresent(u -> {
            throw new ConflictException("Login already exists");
        });

        User saved = userDao.save(new User(
                null,
                request.login.trim(),
                passwordService.hash(request.password),
                role,
                emptyToNull(request.email),
                emptyToNull(request.phone),
                emptyToNull(request.telegramChatId),
                LocalDateTime.now()
        ));

        logger.info("User saved in database: userId={}, login={}, role={}",
                saved.id(),
                saved.login(),
                saved.role().name());

        return new UserResponse(
                saved.id(),
                saved.login(),
                saved.role().name(),
                saved.email(),
                saved.phone(),
                saved.telegramChatId()
        );
    }

    public AuthResponse login(LoginRequest request) {
        if (request == null || isBlank(request.login) || isBlank(request.password)) {
            throw new ValidationException("Login and password are required");
        }

        User user = userDao.findByLogin(request.login.trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordService.matches(request.password, user.passwordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        JwtService.TokenData tokenData = jwtService.generateToken(user.id(), user.login(), user.role().name());

        logger.info("JWT token issued: userId={}, login={}, role={}",
                user.id(),
                user.login(),
                user.role().name());

        return new AuthResponse(tokenData.token(), tokenData.expiresAtEpochSeconds(), user.role().name());
    }

    private void validateRegister(RegisterRequest request) {
        if (request == null) {
            throw new ValidationException("Request body is required");
        }
        if (isBlank(request.login) || request.login.length() < 3) {
            throw new ValidationException("Login must contain at least 3 characters");
        }
        if (isBlank(request.password) || request.password.length() < 6) {
            throw new ValidationException("Password must contain at least 6 characters");
        }
        if (isBlank(request.role)) {
            throw new ValidationException("Role is required");
        }
        try {
            Role.valueOf(request.role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Role must be ADMIN or USER");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String emptyToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}