package main.main.java.com.team.otpservice.service.impl;

import com.team.otpservice.config.FileStorageConfig;
import com.team.otpservice.service.NotificationService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class FileNotificationService implements NotificationService {
    private final Path directory;

    public FileNotificationService(FileStorageConfig config) {
        this.directory = Path.of(config.directory());
    }

    @Override
    public void sendCode(String destination, String code, String operationKey) {
        try {
            Files.createDirectories(directory);
            String fileName = (destination == null || destination.isBlank())
                    ? "otp-codes.log"
                    : destination.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path file = directory.resolve(fileName);
            String line = LocalDateTime.now() + " | operation=" + operationKey + " | code=" + code + System.lineSeparator();
            Files.writeString(file, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save OTP to file", e);
        }
    }
}
