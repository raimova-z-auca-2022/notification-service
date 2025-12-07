-- Справочник статусов уведомлений

CREATE TABLE IF NOT EXISTS notification_status (
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);

INSERT INTO notification_status(code, description) VALUES
    ('PENDING', 'Notification waiting for processing'),
    ('SENDING', 'Notification is being sent'),
    ('SENT',    'Notification successfully sent'),
    ('FAILED',  'Notification failed to send')
ON CONFLICT (code) DO NOTHING;


CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGSERIAL PRIMARY KEY,
    client_id       INTEGER,
    channel_type    VARCHAR(20)  NOT NULL,
    recipient       VARCHAR(255) NOT NULL,
    subject         VARCHAR(255),
    message_body    TEXT         NOT NULL,
    status_id       INTEGER      NOT NULL DEFAULT 1 REFERENCES notification_status(id),
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


CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    action_type VARCHAR(64) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    entity_id   BIGINT,
    details     TEXT,
    ip_address  VARCHAR(45),
    user_agent  TEXT,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_log_entity
    ON audit_log(entity_type, entity_id);

CREATE INDEX IF NOT EXISTS idx_audit_log_created
    ON audit_log(created_at DESC);
