package main.main.java.com.team.otpservice.dao;

import com.team.otpservice.model.Operation;

import java.util.Optional;

public interface OperationDao {
    Operation save(Operation operation);
    Optional<Operation> findByUserIdAndOperationKey(Long userId, String operationKey);
    void deleteByUserId(Long userId);
}
