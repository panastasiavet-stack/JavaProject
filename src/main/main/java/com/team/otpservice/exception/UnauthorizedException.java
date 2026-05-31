package main.main.java.com.team.otpservice.exception;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super(401, "Unauthorized", message);
    }
}
