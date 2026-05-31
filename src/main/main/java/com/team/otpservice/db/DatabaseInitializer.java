package main.main.java.com.team.otpservice.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseInitializer {

    private final ConnectionFactory connectionFactory;

    public DatabaseInitializer(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void initSchema() {
        try (
                Connection connection = connectionFactory.getConnection();
                Statement statement = connection.createStatement()
        ) {
            InputStream inputStream = DatabaseInitializer.class
                    .getClassLoader()
                    .getResourceAsStream("schema.sql");

            if (inputStream == null) {
                throw new RuntimeException("schema.sql not found in src/main/resources");
            }

            String sql;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                sql = reader.lines().collect(Collectors.joining("\n"));
            }

            statement.execute(sql);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }
}