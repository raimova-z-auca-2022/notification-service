-- Таблица уведомлений, максимально приближенная к боевой public.notifications

CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGSERIAL PRIMARY KEY,
    client_id       INTEGER,
    channel_type    VARCHAR(20)  NOT NULL,
    recipient       VARCHAR(255) NOT NULL,
    subject         VARCHAR(255),
    message_body    TEXT         NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count     INTEGER      NOT NULL DEFAULT 0,
    error_message   TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at         TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at      TIMESTAMP    NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL '7 days')
);

CREATE INDEX IF NOT EXISTS idx_notifications_channel
    ON notifications(channel_type);

CREATE INDEX IF NOT EXISTS idx_notifications_created_at
    ON notifications(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_notifications_created_status
    ON notifications(created_at DESC, status);

CREATE INDEX IF NOT EXISTS idx_notifications_expires
    ON notifications(expires_at);

CREATE INDEX IF NOT EXISTS idx_notifications_recipient
    ON notifications(recipient);

CREATE INDEX IF NOT EXISTS idx_notifications_status
    ON notifications(status);

CREATE INDEX IF NOT EXISTS idx_notifications_status_channel
    ON notifications(status, channel_type);

-- Таблица логов аудита (оставляем как была)

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    action_type VARCHAR(64) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    entity_id   VARCHAR(64),
    details     TEXT,
    ip_address  VARCHAR(45),
    user_agent  TEXT,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_log_entity
    ON audit_log(entity_type, entity_id);

CREATE INDEX IF NOT EXISTS idx_audit_log_created
    ON audit_log(created_at DESC);