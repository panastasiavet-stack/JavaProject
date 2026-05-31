package main.main.java.com.team.otpservice.exception;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(403, "Forbidden", message);
    }
}
