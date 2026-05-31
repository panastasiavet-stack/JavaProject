package main.main.java.com.team.otpservice.service.impl;

import com.team.otpservice.config.MailConfig;
import com.team.otpservice.service.NotificationService;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailNotificationService implements NotificationService {
    private final MailConfig config;

    public EmailNotificationService(MailConfig config) {
        this.config = config;
    }

    @Override
    public void sendCode(String destination, String code, String operationKey) {
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Email destination is required");
        }
        if (!config.enabled()) {
            throw new IllegalStateException("Email integration is not configured");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", config.host());
        props.put("mail.smtp.port", String.valueOf(config.port()));
        props.put("mail.smtp.auth", String.valueOf(config.auth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(config.startTls()));
        props.put("mail.mime.charset", "UTF-8");
        props.put("mail.smtp.allow8bitmime", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.username(), config.password());
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.from()));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(destination));
            message.setSubject("Ваш OTP код", "UTF-8");
            message.setContent("Ваш код подтверждения: " + code, "text/plain; charset=UTF-8");

            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
