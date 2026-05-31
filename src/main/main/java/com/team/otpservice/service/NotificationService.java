package main.main.java.com.team.otpservice.service;

public interface NotificationService {
    void sendCode(String destination, String code, String operationKey);
}
