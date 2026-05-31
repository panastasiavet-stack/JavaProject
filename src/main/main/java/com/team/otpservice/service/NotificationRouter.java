package main.main.java.com.team.otpservice.service;

import com.team.otpservice.exception.ValidationException;
import com.team.otpservice.model.DeliveryChannel;

import java.util.EnumMap;
import java.util.Map;

public class NotificationRouter {
    private final Map<DeliveryChannel, NotificationService> delegates = new EnumMap<>(DeliveryChannel.class);

    public void register(DeliveryChannel channel, NotificationService service) {
        delegates.put(channel, service);
    }

    public void send(DeliveryChannel channel, String destination, String code, String operationKey) {
        NotificationService service = delegates.get(channel);
        if (service == null) {
            throw new ValidationException("Delivery channel is not configured: " + channel);
        }
        service.sendCode(destination, code, operationKey);
    }
}
