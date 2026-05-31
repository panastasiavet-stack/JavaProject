package main.main.java.com.team.otpservice.service.impl;

import com.team.otpservice.config.TelegramConfig;
import com.team.otpservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class TelegramNotificationService implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationService.class);

    private final TelegramConfig config;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public TelegramNotificationService(TelegramConfig config) {
        this.config = config;
    }

    @Override
    public void sendCode(String destination, String code, String operationKey) {
        logger.info("Telegram send requested: destination={}, operationKey={}", destination, operationKey);
        logger.info("Telegram config: enabled={}, botTokenPresent={}, apiUrlTemplate={}",
                config.enabled(),
                config.botToken() != null && !config.botToken().isBlank(),
                config.apiUrlTemplate());

        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Telegram chat id is required");
        }
        if (!config.enabled()) {
            throw new IllegalStateException("Telegram integration is not configured");
        }

        String message = "Ваш код подтверждения: " + code;
        String baseUrl = String.format(config.apiUrlTemplate(), config.botToken());
        String url = baseUrl + "?chat_id=" + destination + "&text=" + URLEncoder.encode(message, StandardCharsets.UTF_8);

        logger.info("Telegram request URL: {}", url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Telegram response status: {}", response.statusCode());
            logger.info("Telegram response body: {}", response.body());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Telegram API returned status " + response.statusCode() + ": " + response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Telegram request interrupted", e);
        } catch (IOException e) {
            logger.error("Telegram request failed for URL: {}", url, e);
            throw new RuntimeException("Failed to send Telegram message", e);
        }
    }
}