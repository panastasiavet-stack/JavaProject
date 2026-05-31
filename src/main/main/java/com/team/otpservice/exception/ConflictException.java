package main.main.java.com.team.otpservice.exception;

public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(409, "Conflict", message);
    }
}
