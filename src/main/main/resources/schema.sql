CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     login VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    telegram_chat_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS otp_config (
                                          id BIGSERIAL PRIMARY KEY,
                                          code_length INT NOT NULL,
                                          ttl_seconds INT NOT NULL,
                                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS operations (
                                          id BIGSERIAL PRIMARY KEY,
                                          user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    operation_key VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS otp_codes (
                                         id BIGSERIAL PRIMARY KEY,
                                         user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    operation_id BIGINT NOT NULL REFERENCES operations(id) ON DELETE CASCADE,
    code VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    destination VARCHAR(255),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP
    );

INSERT INTO otp_config (id, code_length, ttl_seconds, updated_at)
VALUES (1, 6, 60, CURRENT_TIMESTAMP)
    ON CONFLICT (id) DO NOTHING;