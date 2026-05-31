package main.main.java.com.team.otpservice.dao;

import com.team.otpservice.model.OtpConfig;

public interface OtpConfigDao {
    OtpConfig getConfig();
    OtpConfig update(int codeLength, int ttlSeconds);
}
