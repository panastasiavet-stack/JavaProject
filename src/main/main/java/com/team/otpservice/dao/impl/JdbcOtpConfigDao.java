package main.main.java.com.team.otpservice.dao.impl;

import com.team.otpservice.dao.OtpConfigDao;
import com.team.otpservice.db.ConnectionFactory;
import com.team.otpservice.model.OtpConfig;

import java.sql.*;
import java.time.LocalDateTime;

public class JdbcOtpConfigDao implements OtpConfigDao {
    private final ConnectionFactory connectionFactory;

    public JdbcOtpConfigDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public OtpConfig getConfig() {
        String sql = "SELECT * FROM otp_config ORDER BY id LIMIT 1";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (!rs.next()) {
                throw new IllegalStateException("OTP config row not found");
            }
            return map(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get OTP config", e);
        }
    }

    @Override
    public OtpConfig update(int codeLength, int ttlSeconds) {
        OtpConfig existing = getConfig();
        String sql = "UPDATE otp_config SET code_length = ?, ttl_seconds = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, codeLength);
            statement.setInt(2, ttlSeconds);
            statement.setLong(3, existing.id());
            statement.executeUpdate();
            return getConfig();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update OTP config", e);
        }
    }

    private OtpConfig map(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("updated_at");
        return new OtpConfig(
                rs.getLong("id"),
                rs.getInt("code_length"),
                rs.getInt("ttl_seconds"),
                ts != null ? ts.toLocalDateTime() : LocalDateTime.now()
        );
    }
}
