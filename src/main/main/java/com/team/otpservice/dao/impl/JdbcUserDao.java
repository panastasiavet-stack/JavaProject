package main.main.java.com.team.otpservice.dao.impl;

import com.team.otpservice.dao.UserDao;
import com.team.otpservice.db.ConnectionFactory;
import com.team.otpservice.model.Role;
import com.team.otpservice.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserDao implements UserDao {
    private final ConnectionFactory connectionFactory;

    public JdbcUserDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (login, password_hash, role, email, phone, telegram_chat_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.login());
            statement.setString(2, user.passwordHash());
            statement.setString(3, user.role().name());
            statement.setString(4, user.email());
            statement.setString(5, user.phone());
            statement.setString(6, user.telegramChatId());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return findById(keys.getLong(1)).orElseThrow();
                }
            }
            throw new SQLException("No generated key returned");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save user", e);
        }
    }

    @Override
    public Optional<User> findByLogin(String login) {
        String sql = "SELECT * FROM users WHERE login = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, login);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by login", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by id", e);
        }
    }

    @Override
    public boolean existsAdmin() {
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE role = 'ADMIN')";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check admin existence", e);
        }
    }

    @Override
    public List<User> findAllNonAdminUsers() {
        String sql = "SELECT * FROM users WHERE role <> 'ADMIN' ORDER BY id";
        List<User> users = new ArrayList<>();
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                users.add(map(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to list non-admin users", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        return new User(
                rs.getLong("id"),
                rs.getString("login"),
                rs.getString("password_hash"),
                Role.valueOf(rs.getString("role")),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("telegram_chat_id"),
                ts != null ? ts.toLocalDateTime() : LocalDateTime.now()
        );
    }
}
