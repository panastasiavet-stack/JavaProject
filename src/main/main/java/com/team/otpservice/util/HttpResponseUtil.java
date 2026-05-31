package main.main.java.com.team.otpservice.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class HttpResponseUtil {
    public static void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        byte[] bytes = JsonUtil.toJsonBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
}
