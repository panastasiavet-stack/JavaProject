package main.main.java.com.team.otpservice.http;

import com.sun.net.httpserver.HttpExchange;
import com.team.otpservice.security.AuthContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RequestContext {
    private final HttpExchange exchange;
    private final Map<String, String> pathParams;
    private AuthContext authContext;

    public RequestContext(HttpExchange exchange, Map<String, String> pathParams) {
        this.exchange = exchange;
        this.pathParams = pathParams;
    }

    public HttpExchange exchange() {
        return exchange;
    }

    public String body() throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public String header(String name) {
        return exchange.getRequestHeaders().getFirst(name);
    }

    public String pathParam(String name) {
        return pathParams.get(name);
    }

    public String path() {
        return exchange.getRequestURI().getPath();
    }

    public AuthContext authContext() {
        return authContext;
    }

    public void setAuthContext(AuthContext authContext) {
        this.authContext = authContext;
    }
}
