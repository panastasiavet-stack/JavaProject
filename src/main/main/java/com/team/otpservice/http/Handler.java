package main.main.java.com.team.otpservice.http;

@FunctionalInterface
public interface Handler {
    void handle(RequestContext context) throws Exception;
}
