package main.main.java.com.team.otpservice.service;

import com.team.otpservice.dto.request.UpdateOtpConfigRequest;
import com.team.otpservice.dto.response.OtpConfigResponse;
import com.team.otpservice.dto.response.UserResponse;
import com.team.otpservice.exception.ValidationException;
import com.team.otpservice.model.OtpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final UserService userService;
    private final OtpConfigService otpConfigService;

    public AdminService(UserService userService, OtpConfigService otpConfigService) {
        this.userService = userService;
        this.otpConfigService = otpConfigService;
    }

    public List<UserResponse> getUsers() {
        return userService.findAllNonAdminUsers();
    }

    public OtpConfigResponse getOtpConfig() {
        return toResponse(otpConfigService.getConfig());
    }

    public OtpConfigResponse updateOtpConfig(UpdateOtpConfigRequest request) {
        if (request == null) {
            throw new ValidationException("Request body is required");
        }
        if (request.codeLength < 4 || request.codeLength > 10) {
            throw new ValidationException("codeLength must be in range 4..10");
        }
        if (request.ttlSeconds < 30 || request.ttlSeconds > 3600) {
            throw new ValidationException("ttlSeconds must be in range 30..3600");
        }

        OtpConfig updated = otpConfigService.update(request.codeLength, request.ttlSeconds);

        logger.info("OTP config updated in service: codeLength={}, ttlSeconds={}",
                updated.codeLength(),
                updated.ttlSeconds());

        return toResponse(updated);
    }

    public void deleteUser(Long id) {
        userService.deleteUser(id);
        logger.info("User deleted in service: deletedUserId={}", id);
    }

    private OtpConfigResponse toResponse(OtpConfig config) {
        return new OtpConfigResponse(
                config.codeLength(),
                config.ttlSeconds(),
                config.updatedAt().atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
        );
    }
}