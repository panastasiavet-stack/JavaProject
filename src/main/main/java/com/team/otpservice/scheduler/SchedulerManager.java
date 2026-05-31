package main.main.java.com.team.otpservice.scheduler;

import com.team.otpservice.service.OtpExpirationService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerManager {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final int intervalSeconds;
    private final OtpExpirationService otpExpirationService;

    public SchedulerManager(int intervalSeconds, OtpExpirationService otpExpirationService) {
        this.intervalSeconds = intervalSeconds;
        this.otpExpirationService = otpExpirationService;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(otpExpirationService::expireCodes, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }
}
