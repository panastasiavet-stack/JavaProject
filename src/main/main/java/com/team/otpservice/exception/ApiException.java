package main.main.java.com.team.otpservice.exception;

public class ApiException extends RuntimeException {
    private final int statusCode;
    private final String error;

    public ApiException(int statusCode, String error, String message) {
        super(message);
        this.statusCode = statusCode;
        this.error = error;
    }

    public int statusCode() {
        return statusCode;
    }

    public String error() {
        return error;
    }
}
