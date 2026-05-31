package main.main.java.com.team.otpservice.controller;

import com.team.otpservice.dto.request.UpdateOtpConfigRequest;
import com.team.otpservice.dto.response.MessageResponse;
import com.team.otpservice.dto.response.OtpConfigResponse;
import com.team.otpservice.dto.response.UserResponse;
import com.team.otpservice.http.HttpMethod;
import com.team.otpservice.http.Router;
import com.team.otpservice.service.AdminService;
import com.team.otpservice.util.HttpResponseUtil;
import com.team.otpservice.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    public void register(Router router) {
        router.add(HttpMethod.GET, "/admin/users", true, true, context -> {
            List<UserResponse> users = adminService.getUsers();

            logger.info("Users list requested by admin: adminId={}, count={}",
                    context.authContext().userId(),
                    users.size());

            HttpResponseUtil.sendJson(context.exchange(), 200, users);
        });

        router.add(HttpMethod.GET, "/admin/otp-config", true, true, context -> {
            OtpConfigResponse response = adminService.getOtpConfig();

            logger.info("OTP config requested by admin: adminId={}",
                    context.authContext().userId());

            HttpResponseUtil.sendJson(context.exchange(), 200, response);
        });

        router.add(HttpMethod.PUT, "/admin/otp-config", true, true, context -> {
            UpdateOtpConfigRequest request = JsonUtil.fromJson(context.body(), UpdateOtpConfigRequest.class);
            OtpConfigResponse response = adminService.updateOtpConfig(request);

            logger.info("OTP config updated by admin: adminId={}, codeLength={}, ttlSeconds={}",
                    context.authContext().userId(),
                    response.codeLength(),
                    response.ttlSeconds());

            HttpResponseUtil.sendJson(context.exchange(), 200, response);
        });

        router.add(HttpMethod.DELETE, "/admin/users/{id}", true, true, context -> {
            Long id = Long.parseLong(context.pathParam("id"));
            adminService.deleteUser(id);

            logger.info("User deleted by admin: adminId={}, deletedUserId={}",
                    context.authContext().userId(),
                    id);

            HttpResponseUtil.sendJson(context.exchange(), 200, new MessageResponse("User deleted"));
        });
    }
}