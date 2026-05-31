package main.main.java.com.team.otpservice.dao;

import com.team.otpservice.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    User save(User user);
    Optional<User> findByLogin(String login);
    Optional<User> findById(Long id);
    boolean existsAdmin();
    List<User> findAllNonAdminUsers();
    void deleteById(Long id);
}
