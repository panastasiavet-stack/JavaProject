package main.main.java.com.team.otpservice.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public record AppConfig(
        String serverHost,
        int serverPort,
        int schedulerIntervalSeconds,
        DatabaseConfig databaseConfig,
        JwtConfig jwtConfig,
        MailConfig mailConfig,
        SmppConfig smppConfig,
        TelegramConfig telegramConfig,
        FileStorageConfig fileStorageConfig
) {
    public static AppConfig load() {
        Properties applicationProperties = loadProperties("application.properties");

        String mailConfigFile = applicationProperties.getProperty("mail.config-file", "email.properties");
        String smppConfigFile = applicationProperties.getProperty("smpp.config-file", "sms.properties");
        String telegramConfigFile = applicationProperties.getProperty("telegram.config-file", "telegram.properties");

        Properties mailProperties = loadProperties(mailConfigFile);
        Properties smppProperties = loadProperties(smppConfigFile);
        Properties telegramProperties = loadProperties(telegramConfigFile);

        return new AppConfig(
                applicationProperties.getProperty("app.host", "localhost"),
                Integer.parseInt(applicationProperties.getProperty("app.port", "8080")),
                Integer.parseInt(applicationProperties.getProperty("otp.scheduler.expiration-check-interval-seconds", "30")),
                new DatabaseConfig(
                        applicationProperties.getProperty("db.url"),
                        applicationProperties.getProperty("db.username"),
                        applicationProperties.getProperty("db.password")
                ),
                new JwtConfig(
                        applicationProperties.getProperty("jwt.secret"),
                        Long.parseLong(applicationProperties.getProperty("jwt.ttl-seconds", "3600")),
                        applicationProperties.getProperty("jwt.issuer", "otp-service")
                ),
                new MailConfig(
                        mailProperties.getProperty("email.username", ""),
                        mailProperties.getProperty("email.password", ""),
                        mailProperties.getProperty("email.from", ""),
                        mailProperties.getProperty("mail.smtp.host", ""),
                        Integer.parseInt(mailProperties.getProperty("mail.smtp.port", "587")),
                        Boolean.parseBoolean(mailProperties.getProperty("mail.smtp.auth", "true")),
                        Boolean.parseBoolean(mailProperties.getProperty("mail.smtp.starttls.enable", "true"))
                ),
                new SmppConfig(
                        smppProperties.getProperty("smpp.host", "localhost"),
                        Integer.parseInt(smppProperties.getProperty("smpp.port", "2775")),
                        smppProperties.getProperty("smpp.system_id", "smppclient1"),
                        smppProperties.getProperty("smpp.password", "password"),
                        smppProperties.getProperty("smpp.system_type", "OTP"),
                        smppProperties.getProperty("smpp.source_addr", "OTPService")
                ),
                new TelegramConfig(
                        telegramProperties.getProperty("telegram.bot-token", ""),
                        telegramProperties.getProperty("telegram.api-url-template", "https://api.telegram.org/bot%s/sendMessage")
                ),
                new FileStorageConfig(applicationProperties.getProperty("file.storage.path", "sent-otp-codes/otp-log.txt"))
        );
    }

    private static Properties loadProperties(String fileName) {
        Properties properties = new Properties();
        try (InputStream inputStream = AppConfig.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                return properties;
            }
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + fileName, e);
        }
    }
}