package main.main.java.com.team.otpservice.config;

public record SmppConfig(
        String host,
        int port,
        String systemId,
        String password,
        String systemType,
        String sourceAddress
) {
}
