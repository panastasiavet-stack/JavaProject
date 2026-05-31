package main.main.java.com.team.otpservice.http;

public record Route(
        HttpMethod method,
        String pattern,
        boolean authRequired,
        boolean adminOnly,
        Handler handler
) {
}
