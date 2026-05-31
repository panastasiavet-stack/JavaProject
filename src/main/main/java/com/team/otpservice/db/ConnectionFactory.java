package main.main.java.com.team.otpservice.db;

import com.team.otpservice.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    private final DatabaseConfig config;

    public ConnectionFactory(DatabaseConfig config) {
        this.config = config;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.url(), config.username(), config.password());
    }

    public void close() {
        // no-op for DriverManager based connections
    }
}
