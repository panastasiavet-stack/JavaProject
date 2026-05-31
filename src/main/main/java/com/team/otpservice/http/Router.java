package main.main.java.com.team.otpservice.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.team.otpservice.dto.response.ApiErrorResponse;
import com.team.otpservice.exception.ApiException;
import com.team.otpservice.security.AuthContext;
import com.team.otpservice.security.AuthorizationService;
import com.team.otpservice.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class Router implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(Router.class);

    private final List<Route> routes = new ArrayList<>();
    private final AuthorizationService authorizationService;

    public Router(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    public void add(HttpMethod method, String pattern, boolean authRequired, boolean adminOnly, Handler handler) {
        routes.add(new Route(method, pattern, authRequired, adminOnly, handler));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        long startedAt = System.currentTimeMillis();
        String method = exchange.getRequestMethod().toUpperCase(Locale.ROOT);
        String path = exchange.getRequestURI().getPath();

        logger.info("Incoming request: {} {}", method, path);

        try {
            for (Route route : routes) {
                if (!route.method().name().equals(method)) {
                    continue;
                }

                Map<String, String> params = match(route.pattern(), path);
                if (params == null) {
                    continue;
                }

                RequestContext context = new RequestContext(exchange, params);

                if (route.authRequired()) {
                    AuthContext authContext = authorizationService.authenticate(exchange.getRequestHeaders().getFirst("Authorization"));
                    if (route.adminOnly()) {
                        authorizationService.requireAdmin(authContext);
                    }
                    context.setAuthContext(authContext);
                }

                route.handler().handle(context);
                logResponse(exchange, startedAt, 200);
                return;
            }

            writeError(exchange, 404, new ApiErrorResponse(
                    "Not Found",
                    "Route not found",
                    path,
                    System.currentTimeMillis()
            ));
            logWarn(exchange, startedAt, 404, "Route not found");
        } catch (ApiException e) {
            writeError(exchange, e.statusCode(), new ApiErrorResponse(
                    e.error(),
                    e.getMessage(),
                    path,
                    System.currentTimeMillis()
            ));

            if (e.statusCode() >= 500) {
                logger.error("Request failed: {} {} -> {}, message={}", method, path, e.statusCode(), e.getMessage());
            } else {
                logger.warn("Request validation failed: {} {} -> {}, message={}", method, path, e.statusCode(), e.getMessage());
            }

            logResponse(exchange, startedAt, e.statusCode());
        } catch (Exception e) {
            logger.error("Unhandled exception", e);

            writeError(exchange, 500, new ApiErrorResponse(
                    "Internal Server Error",
                    "Unexpected server error",
                    path,
                    System.currentTimeMillis()
            ));

            logResponse(exchange, startedAt, 500);
        } finally {
            exchange.close();
        }
    }

    private void writeError(HttpExchange exchange, int status, ApiErrorResponse body) throws IOException {
        byte[] bytes = JsonUtil.toJsonBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    private Map<String, String> match(String pattern, String path) {
        String[] patternParts = trim(pattern).split("/");
        String[] pathParts = trim(path).split("/");

        if (trim(pattern).isEmpty() && trim(path).isEmpty()) {
            return new HashMap<>();
        }
        if (patternParts.length != pathParts.length) {
            return null;
        }

        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < patternParts.length; i++) {
            String patternPart = patternParts[i];
            String pathPart = pathParts[i];
            if (patternPart.startsWith("{") && patternPart.endsWith("}")) {
                result.put(patternPart.substring(1, patternPart.length() - 1), pathPart);
            } else if (!patternPart.equals(pathPart)) {
                return null;
            }
        }
        return result;
    }

    private String trim(String value) {
        if (value == null) return "";
        return value.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private void logWarn(HttpExchange exchange, long startedAt, int status, String message) {
        long duration = System.currentTimeMillis() - startedAt;
        logger.warn("{} {} -> {} ({} ms), message={}",
                exchange.getRequestMethod(),
                exchange.getRequestURI().getPath(),
                status,
                duration,
                message);
    }

    private void logResponse(HttpExchange exchange, long startedAt, int status) {
        long duration = System.currentTimeMillis() - startedAt;
        logger.info("Response sent: {} {} -> {} ({} ms)",
                exchange.getRequestMethod(),
                exchange.getRequestURI().getPath(),
                status,
                duration);
    }
}