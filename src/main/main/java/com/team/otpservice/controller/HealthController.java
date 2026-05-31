package main.main.java.com.team.otpservice.controller;

import com.team.otpservice.dto.response.MessageResponse;
import com.team.otpservice.http.HttpMethod;
import com.team.otpservice.http.Router;
import com.team.otpservice.util.HttpResponseUtil;

public class HealthController {
    public void register(Router router) {
        router.add(HttpMethod.GET, "/health", false, false, context ->
                HttpResponseUtil.sendJson(context.exchange(), 200, new MessageResponse("OK")));
    }
}
