package main.main.java.com.team.otpservice.dao;

import com.team.otpservice.model.OtpCode;
import com.team.otpservice.model.OtpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpCodeDao {
    OtpCode save(OtpCode otpCode);
    Optional<OtpCode> findLatestByUserIdAndOperationId(Long userId, Long operationId);
    Optional<OtpCode> findByUserIdOperationIdAndCode(Long userId, Long operationId, String code);
    void updateStatus(Long id, OtpStatus status, LocalDateTime usedAt);
    int expireActiveCodes(LocalDateTime now);
    void deleteByUserId(Long userId);
}
