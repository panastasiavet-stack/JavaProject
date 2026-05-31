package main.main.java.com.team.otpservice.dao.impl;

import com.team.otpservice.dao.OtpCodeDao;
import com.team.otpservice.db.ConnectionFactory;
import com.team.otpservice.model.DeliveryChannel;
import com.team.otpservice.model.OtpCode;
import com.team.otpservice.model.OtpStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class JdbcOtpCodeDao implements OtpCodeDao {
    private final ConnectionFactory connectionFactory;

    public JdbcOtpCodeDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public OtpCode save(OtpCode otpCode) {
        String sql = "INSERT INTO otp_codes (user_id, operation_id, code, status, channel, destination, expires_at, used_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, otpCode.userId());
            statement.setLong(2, otpCode.operationId());
            statement.setString(3, otpCode.code());
            statement.setString(4, otpCode.status().name());
            statement.setString(5, otpCode.channel().name());
            statement.setString(6, otpCode.destination());
            statement.setTimestamp(7, Timestamp.valueOf(otpCode.expiresAt()));
            if (otpCode.usedAt() == null) {
                statement.setNull(8, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(8, Timestamp.valueOf(otpCode.usedAt()));
            }
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return findLatestByUserIdAndOperationId(otpCode.userId(), otpCode.operationId()).orElseThrow();
                }
            }
            throw new SQLException("No generated key returned");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save OTP code", e);
        }
    }

    @Override
    public Optional<OtpCode> findLatestByUserIdAndOperationId(Long userId, Long operationId) {
        String sql = "SELECT * FROM otp_codes WHERE user_id = ? AND operation_id = ? ORDER BY id DESC LIMIT 1";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, operationId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find latest OTP", e);
        }
    }

    @Override
    public Optional<OtpCode> findByUserIdOperationIdAndCode(Long userId, Long operationId, String code) {
        String sql = "SELECT * FROM otp_codes WHERE user_id = ? AND operation_id = ? AND code = ? ORDER BY id DESC LIMIT 1";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, operationId);
            statement.setString(3, code);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find OTP by code", e);
        }
    }

    @Override
    public void updateStatus(Long id, OtpStatus status, LocalDateTime usedAt) {
        String sql = "UPDATE otp_codes SET status = ?, used_at = ? WHERE id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            if (usedAt == null) {
                statement.setNull(2, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(2, Timestamp.valueOf(usedAt));
            }
            statement.setLong(3, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update OTP status", e);
        }
    }

    @Override
    public int expireActiveCodes(LocalDateTime now) {
        String sql = "UPDATE otp_codes SET status = 'EXPIRED' WHERE status = 'ACTIVE' AND expires_at < ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(now));
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to expire active OTP codes", e);
        }
    }

    @Override
    public void deleteByUserId(Long userId) {
        String sql = "DELETE FROM otp_codes WHERE user_id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user OTP codes", e);
        }
    }

    private OtpCode map(ResultSet rs) throws SQLException {
        Timestamp expiresAt = rs.getTimestamp("expires_at");
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp usedAt = rs.getTimestamp("used_at");
        return new OtpCode(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getLong("operation_id"),
                rs.getString("code"),
                OtpStatus.valueOf(rs.getString("status")),
                DeliveryChannel.valueOf(rs.getString("channel")),
                rs.getString("destination"),
                expiresAt != null ? expiresAt.toLocalDateTime() : LocalDateTime.now(),
                createdAt != null ? createdAt.toLocalDateTime() : LocalDateTime.now(),
                usedAt != null ? usedAt.toLocalDateTime() : null
        );
    }
}
