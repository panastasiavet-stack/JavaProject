package main.main.java.com.team.otpservice.service;

import com.team.otpservice.dao.OtpCodeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class OtpExpirationService {
    private static final Logger logger = LoggerFactory.getLogger(OtpExpirationService.class);
    private final OtpCodeDao otpCodeDao;

    public OtpExpirationService(OtpCodeDao otpCodeDao) {
        this.otpCodeDao = otpCodeDao;
    }

    public void expireCodes() {
        int updated = otpCodeDao.expireActiveCodes(LocalDateTime.now());
        if (updated > 0) {
            logger.info("Expired {} OTP codes", updated);
        }
    }
}
