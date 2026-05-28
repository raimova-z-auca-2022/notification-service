CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS notifications (
  id UUID PRIMARY KEY,
  type VARCHAR(16) NOT NULL CHECK (type IN ('EMAIL', 'TELEGRAM', 'WHATSAPP')),
  recipient VARCHAR(255) NOT NULL,
  text TEXT NOT NULL,

  status VARCHAR(16) NOT NULL CHECK (status IN ('NEW','PUBLISHED','PROCESSING','SENT','FAILED','DEAD')),
  attempt INT NOT NULL DEFAULT 0,

  idempotency_key VARCHAR(128) NOT NULL,

  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

  last_error_code VARCHAR(64),
  last_error_message TEXT,
  provider_message_id VARCHAR(128)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_notifications_idempotency_key
  ON notifications (idempotency_key);

CREATE INDEX IF NOT EXISTS ix_notifications_status
  ON notifications (status);

CREATE INDEX IF NOT EXISTS ix_notifications_type_created
  ON notifications (type, created_at DESC);

CREATE INDEX IF NOT EXISTS ix_notifications_recipient
  ON notifications (recipient);

CREATE TABLE IF NOT EXISTS notification_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  notification_id UUID NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,

  event_type VARCHAR(32) NOT NULL,
  status VARCHAR(16),
  attempt INT,
  error_code VARCHAR(64),
  error_message TEXT,
  provider_message_id VARCHAR(128),

  occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS ix_events_notification_id_time
  ON notification_events (notification_id, occurred_at DESC);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_notifications_set_updated_at ON notifications;
CREATE TRIGGER trg_notifications_set_updated_at
BEFORE UPDATE ON notifications
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- schema.sql
CREATE TABLE IF NOT EXISTS scheduled_notifications (
                                                       id VARCHAR(36) PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    recipient VARCHAR(500) NOT NULL,
    text TEXT NOT NULL,
    scheduled_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) NOT NULL,
    sent_at TIMESTAMP NULL,
    provider_message_id VARCHAR(128),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Индексы
CREATE INDEX IF NOT EXISTS idx_scheduled_status
    ON scheduled_notifications(status);

CREATE INDEX IF NOT EXISTS idx_scheduled_at_status
    ON scheduled_notifications(scheduled_at, status);

CREATE INDEX IF NOT EXISTS idx_recipient_status
    ON scheduled_notifications(recipient, status);

CREATE INDEX IF NOT EXISTS idx_scheduled_at
    ON scheduled_notifications(scheduled_at);

-- Ограничения
ALTER TABLE scheduled_notifications
    ADD CONSTRAINT chk_status
        CHECK (status IN ('PENDING', 'SENT', 'CANCELLED', 'FAILED', 'MISSED'));
