package main.main.java.com.team.otpservice.service;

import com.team.otpservice.dao.OperationDao;
import com.team.otpservice.dao.OtpCodeDao;
import com.team.otpservice.dao.UserDao;
import com.team.otpservice.dto.response.UserResponse;
import com.team.otpservice.exception.NotFoundException;
import com.team.otpservice.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final OtpCodeDao otpCodeDao;
    private final OperationDao operationDao;

    public UserService(UserDao userDao, OtpCodeDao otpCodeDao, OperationDao operationDao) {
        this.userDao = userDao;
        this.otpCodeDao = otpCodeDao;
        this.operationDao = operationDao;
    }

    public List<UserResponse> findAllNonAdminUsers() {
        return userDao.findAllNonAdminUsers().stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteUser(Long userId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if ("ADMIN".equals(user.role().name())) {
            throw new NotFoundException("Cannot delete administrator by this endpoint");
        }

        otpCodeDao.deleteByUserId(userId);
        operationDao.deleteByUserId(userId);
        userDao.deleteById(userId);

        logger.info("User and related data deleted: userId={}, login={}",
                user.id(),
                user.login());
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.id(), user.login(), user.role().name(), user.email(), user.phone(), user.telegramChatId());
    }
}