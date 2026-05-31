package main.main.java.com.team.otpservice.controller;

import com.team.otpservice.dto.request.LoginRequest;
import com.team.otpservice.dto.request.RegisterRequest;
import com.team.otpservice.dto.response.AuthResponse;
import com.team.otpservice.dto.response.UserResponse;
import com.team.otpservice.http.HttpMethod;
import com.team.otpservice.http.Router;
import com.team.otpservice.service.AuthService;
import com.team.otpservice.util.HttpResponseUtil;
import com.team.otpservice.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public void register(Router router) {
        router.add(HttpMethod.POST, "/auth/register", false, false, context -> {
            RegisterRequest request = JsonUtil.fromJson(context.body(), RegisterRequest.class);
            UserResponse response = authService.register(request);

            logger.info("User registered successfully: userId={}, login={}, role={}",
                    response.id(),
                    response.login(),
                    response.role());

            HttpResponseUtil.sendJson(context.exchange(), 201, response);
        });

        router.add(HttpMethod.POST, "/auth/login", false, false, context -> {
            LoginRequest request = JsonUtil.fromJson(context.body(), LoginRequest.class);
            AuthResponse response = authService.login(request);

            logger.info("User logged in successfully: login={}", request.login);

            HttpResponseUtil.sendJson(context.exchange(), 200, response);
        });
    }
}