package main.main.java.com.team.otpservice.controller;

import com.team.otpservice.dto.request.GenerateOtpRequest;
import com.team.otpservice.dto.request.ValidateOtpRequest;
import com.team.otpservice.dto.response.OtpResponse;
import com.team.otpservice.dto.response.OtpValidationResponse;
import com.team.otpservice.http.HttpMethod;
import com.team.otpservice.http.Router;
import com.team.otpservice.service.OtpService;
import com.team.otpservice.util.HttpResponseUtil;
import com.team.otpservice.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OtpController {
    private static final Logger logger = LoggerFactory.getLogger(OtpController.class);

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    public void register(Router router) {
        router.add(HttpMethod.POST, "/otp/generate", true, false, context -> {
            GenerateOtpRequest request = JsonUtil.fromJson(context.body(), GenerateOtpRequest.class);
            OtpResponse response = otpService.generate(context.authContext().userId(), request);

            logger.info("OTP generated successfully: userId={}, operationKey={}, channel={}, otpId={}",
                    context.authContext().userId(),
                    response.operationKey(),
                    response.channel(),
                    response.otpId());

            HttpResponseUtil.sendJson(context.exchange(), 201, response);
        });

        router.add(HttpMethod.POST, "/otp/validate", true, false, context -> {
            ValidateOtpRequest request = JsonUtil.fromJson(context.body(), ValidateOtpRequest.class);
            OtpValidationResponse response = otpService.validate(context.authContext().userId(), request);

            logger.info("OTP validated successfully: userId={}, operationKey={}, status={}",
                    context.authContext().userId(),
                    request.operationKey,
                    response.status());

            HttpResponseUtil.sendJson(context.exchange(), 200, response);
        });
    }
}