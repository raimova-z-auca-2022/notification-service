
CREATE TABLE IF NOT EXISTS notifications (
                                             notification_id BIGSERIAL PRIMARY KEY,
                                             external_id VARCHAR(36) NOT NULL UNIQUE,
    client_id INT,
    channel_type VARCHAR(20) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    message_body TEXT NOT NULL,
    status_id INT NOT NULL DEFAULT 1,
    retry_count INT NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    expires_at TIMESTAMP
    );


CREATE UNIQUE INDEX IF NOT EXISTS idx_notifications_external_id
    ON notifications(external_id);


CREATE INDEX IF NOT EXISTS idx_notifications_recipient_status
    ON notifications(recipient, status_id);


CREATE INDEX IF NOT EXISTS idx_notifications_status
    ON notifications(status_id);
