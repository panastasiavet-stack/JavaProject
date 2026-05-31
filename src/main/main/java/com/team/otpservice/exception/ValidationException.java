package main.main.java.com.team.otpservice.exception;

public class ValidationException extends ApiException {
    public ValidationException(String message) {
        super(400, "Bad Request", message);
    }
}
