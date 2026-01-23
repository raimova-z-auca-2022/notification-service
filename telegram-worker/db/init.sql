-- Создаем таблицу заявок на регистрацию
CREATE TABLE IF NOT EXISTS registration_requests (
                                                     token VARCHAR(64) PRIMARY KEY,
    internal_user_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL
    );

-- Создаем таблицу пользователей Telegram
CREATE TABLE IF NOT EXISTS telegram_users (
                                              id BIGSERIAL PRIMARY KEY,
                                              chat_id BIGINT NOT NULL,
                                              phone_number VARCHAR(20) NOT NULL,
    internal_user_id VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_phone_chat UNIQUE (phone_number, chat_id)
    );

-- Создаем индекс для быстрого поиска по номеру
CREATE INDEX IF NOT EXISTS idx_telegram_users_phone ON telegram_users(phone_number);