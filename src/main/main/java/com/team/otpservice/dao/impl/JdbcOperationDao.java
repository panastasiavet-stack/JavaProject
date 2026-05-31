package main.main.java.com.team.otpservice.dao.impl;

import com.team.otpservice.dao.OperationDao;
import com.team.otpservice.db.ConnectionFactory;
import com.team.otpservice.model.Operation;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class JdbcOperationDao implements OperationDao {
    private final ConnectionFactory connectionFactory;

    public JdbcOperationDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Operation save(Operation operation) {
        String sql = "INSERT INTO operations (user_id, operation_key, description) VALUES (?, ?, ?)";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, operation.userId());
            statement.setString(2, operation.operationKey());
            statement.setString(3, operation.description());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return findByUserIdAndOperationKey(operation.userId(), operation.operationKey()).orElseThrow();
                }
            }
            throw new SQLException("No generated key returned");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save operation", e);
        }
    }

    @Override
    public Optional<Operation> findByUserIdAndOperationKey(Long userId, String operationKey) {
        String sql = "SELECT * FROM operations WHERE user_id = ? AND operation_key = ? ORDER BY id DESC LIMIT 1";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, operationKey);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find operation", e);
        }
    }

    @Override
    public void deleteByUserId(Long userId) {
        String sql = "DELETE FROM operations WHERE user_id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete operations", e);
        }
    }

    private Operation map(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        return new Operation(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("operation_key"),
                rs.getString("description"),
                ts != null ? ts.toLocalDateTime() : LocalDateTime.now()
        );
    }
}
