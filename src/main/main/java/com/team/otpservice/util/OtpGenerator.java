package main.main.java.com.team.otpservice.util;

import java.security.SecureRandom;

public class OtpGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateNumericCode(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(RANDOM.nextInt(10));
        }
        return builder.toString();
    }
}
