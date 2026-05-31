package main.main.java.com.team.otpservice.service;

import com.team.otpservice.dao.OperationDao;
import com.team.otpservice.dao.OtpCodeDao;
import com.team.otpservice.dao.UserDao;
import com.team.otpservice.dto.request.GenerateOtpRequest;
import com.team.otpservice.dto.request.ValidateOtpRequest;
import com.team.otpservice.dto.response.OtpResponse;
import com.team.otpservice.dto.response.OtpValidationResponse;
import com.team.otpservice.exception.NotFoundException;
import com.team.otpservice.exception.OtpValidationException;
import com.team.otpservice.exception.ValidationException;
import com.team.otpservice.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    private final OperationDao operationDao;
    private final OtpCodeDao otpCodeDao;
    private final OtpConfigService otpConfigService;
    private final NotificationRouter notificationRouter;
    private final UserDao userDao;

    public OtpService(OperationDao operationDao,
                      OtpCodeDao otpCodeDao,
                      OtpConfigService otpConfigService,
                      NotificationRouter notificationRouter,
                      UserDao userDao) {
        this.operationDao = operationDao;
        this.otpCodeDao = otpCodeDao;
        this.otpConfigService = otpConfigService;
        this.notificationRouter = notificationRouter;
        this.userDao = userDao;
    }

    public OtpResponse generate(Long userId, GenerateOtpRequest request) {
        if (request == null) {
            throw new ValidationException("Request body is required");
        }
        if (request.operationKey == null || request.operationKey.isBlank()) {
            throw new ValidationException("operationKey is required");
        }
        if (request.channel == null || request.channel.isBlank()) {
            throw new ValidationException("channel is required");
        }

        DeliveryChannel channel;
        try {
            channel = DeliveryChannel.valueOf(request.channel.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unsupported channel");
        }

        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String destination = resolveDestination(channel, request.destination, user);

        logger.info("OTP generation requested: userId={}, operationKey={}, channel={}, destination={}",
                userId,
                request.operationKey,
                channel.name(),
                destination);

        OtpConfig config = otpConfigService.getConfig();
        String code = com.team.otpservice.util.OtpGenerator.generateNumericCode(config.codeLength());

        Operation operation = operationDao.findByUserIdAndOperationKey(userId, request.operationKey.trim())
                .orElseGet(() -> operationDao.save(new Operation(
                        null,
                        userId,
                        request.operationKey.trim(),
                        request.operationDescription,
                        LocalDateTime.now()
                )));

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(config.ttlSeconds());

        OtpCode saved = otpCodeDao.save(new OtpCode(
                null,
                userId,
                operation.id(),
                code,
                OtpStatus.ACTIVE,
                channel,
                destination,
                expiresAt,
                LocalDateTime.now(),
                null
        ));

        notificationRouter.send(channel, destination, code, request.operationKey.trim());

        logger.info("OTP generated and notification sent: otpId={}, userId={}, operationKey={}, channel={}",
                saved.id(),
                userId,
                request.operationKey,
                channel.name());

        return new OtpResponse(
                saved.id(),
                request.operationKey.trim(),
                saved.status().name(),
                saved.channel().name(),
                saved.expiresAt().atZone(java.time.ZoneId.systemDefault()).toEpochSecond()
        );
    }

    private String resolveDestination(DeliveryChannel channel, String requestDestination, User user) {
        if (requestDestination != null && !requestDestination.isBlank()) {
            return requestDestination.trim();
        }

        return switch (channel) {
            case EMAIL -> {
                if (user.email() == null || user.email().isBlank()) {
                    throw new ValidationException("User email is not configured");
                }
                yield user.email().trim();
            }
            case SMS -> {
                if (user.phone() == null || user.phone().isBlank()) {
                    throw new ValidationException("User phone is not configured");
                }
                yield user.phone().trim();
            }
            case TELEGRAM -> {
                if (user.telegramChatId() == null || user.telegramChatId().isBlank()) {
                    throw new ValidationException("User telegram chat id is not configured");
                }
                yield user.telegramChatId().trim();
            }
            case FILE -> "local-file";
        };
    }

    public OtpValidationResponse validate(Long userId, ValidateOtpRequest request) {
        if (request == null || request.operationKey == null || request.operationKey.isBlank() || request.code == null || request.code.isBlank()) {
            throw new ValidationException("operationKey and code are required");
        }

        Operation operation = operationDao.findByUserIdAndOperationKey(userId, request.operationKey.trim())
                .orElseThrow(() -> new NotFoundException("Operation not found"));

        OtpCode otpCode = otpCodeDao.findByUserIdOperationIdAndCode(userId, operation.id(), request.code.trim())
                .orElseThrow(() -> new OtpValidationException("OTP code not found"));

        if (otpCode.status() == OtpStatus.USED) {
            throw new OtpValidationException("OTP code is already used");
        }

        if (otpCode.status() == OtpStatus.EXPIRED || otpCode.expiresAt().isBefore(LocalDateTime.now())) {
            otpCodeDao.updateStatus(otpCode.id(), OtpStatus.EXPIRED, null);
            throw new OtpValidationException("OTP code is expired");
        }

        otpCodeDao.updateStatus(otpCode.id(), OtpStatus.USED, LocalDateTime.now());

        logger.info("OTP validated successfully: otpId={}, userId={}, operationKey={}",
                otpCode.id(),
                userId,
                request.operationKey);

        return new OtpValidationResponse(true, OtpStatus.USED.name(), "OTP code validated successfully");
    }
}