package main.main.java.com.team.otpservice.service.impl;

import com.team.otpservice.config.SmppConfig;
import com.team.otpservice.service.NotificationService;
import org.jsmpp.bean.*;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;

import java.nio.charset.StandardCharsets;

public class SmsNotificationService implements NotificationService {
    private final SmppConfig config;

    public SmsNotificationService(SmppConfig config) {
        this.config = config;
    }

    @Override
    public void sendCode(String destination, String code, String operationKey) {
        if (destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Phone destination is required");
        }

        SMPPSession session = new SMPPSession();
        try {
            BindParameter bindParameter = new BindParameter(
                    BindType.BIND_TX,
                    config.systemId(),
                    config.password(),
                    config.systemType(),
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    config.sourceAddress()
            );

            session.connectAndBind(config.host(), config.port(), bindParameter);

            String text = "Ваш код подтверждения: " + code;

            session.submitShortMessage(
                    config.systemType(),
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    config.sourceAddress(),
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    destination,
                    new ESMClass(),
                    (byte) 0,
                    (byte) 1,
                    null,
                    null,
                    new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT),
                    (byte) 0,
                    new GeneralDataCoding(Alphabet.ALPHA_UCS2),
                    (byte) 0,
                    text.getBytes(StandardCharsets.UTF_16BE)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to send SMS via SMPP", e);
        } finally {
            try {
                session.unbindAndClose();
            } catch (Exception ignored) {
            }
        }
    }
}