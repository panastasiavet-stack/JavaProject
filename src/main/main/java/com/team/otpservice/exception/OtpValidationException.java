package main.main.java.com.team.otpservice.exception;

public class OtpValidationException extends ApiException {
    public OtpValidationException(String message) {
        super(400, "OTP Validation Error", message);
    }
}
