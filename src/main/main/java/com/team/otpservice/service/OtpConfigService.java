package main.main.java.com.team.otpservice.service;

import com.team.otpservice.dao.OtpConfigDao;
import com.team.otpservice.model.OtpConfig;

public class OtpConfigService {
    private final OtpConfigDao otpConfigDao;

    public OtpConfigService(OtpConfigDao otpConfigDao) {
        this.otpConfigDao = otpConfigDao;
    }

    public OtpConfig getConfig() {
        return otpConfigDao.getConfig();
    }

    public OtpConfig update(int codeLength, int ttlSeconds) {
        return otpConfigDao.update(codeLength, ttlSeconds);
    }
}
