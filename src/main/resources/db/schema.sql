-- Notification Service Database Schema
-- PostgreSQL 14+

-- Drop existing tables if they exist
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS api_clients CASCADE;
DROP TABLE IF EXISTS channel_configs CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Table: users
-- Stores admin panel users for authentication
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: api_clients
-- Stores external systems authorized to use the notification service
CREATE TABLE api_clients (
    client_id SERIAL PRIMARY KEY,
    client_name VARCHAR(100) NOT NULL,
    api_key VARCHAR(255) UNIQUE NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: channel_configs
-- Stores configuration for different notification channels
CREATE TABLE channel_configs (
    config_id SERIAL PRIMARY KEY,
    channel_name VARCHAR(50) NOT NULL UNIQUE,
    credentials JSONB,
    is_enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: notifications
-- Stores all notification requests and their status
CREATE TABLE notifications (
    notification_id BIGSERIAL PRIMARY KEY,
    client_id INTEGER REFERENCES api_clients(client_id),
    channel_type VARCHAR(20) NOT NULL CHECK (channel_type IN ('EMAIL', 'SMS', 'TELEGRAM', 'WHATSAPP')),
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    message_body TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SENDING', 'SENT', 'FAILED')),
    retry_count INTEGER DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP DEFAULT (CURRENT_TIMESTAMP + INTERVAL '7 days'), -- TTL: 7 days from TZ requirement 2.4
    CONSTRAINT fk_api_client FOREIGN KEY (client_id) REFERENCES api_clients(client_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_channel ON notifications(channel_type);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX idx_notifications_recipient ON notifications(recipient);
CREATE INDEX idx_notifications_expires ON notifications(expires_at); -- For TTL cleanup
CREATE INDEX idx_api_clients_api_key ON api_clients(api_key);

-- Composite index for common queries
CREATE INDEX idx_notifications_status_channel ON notifications(status, channel_type);
CREATE INDEX idx_notifications_created_status ON notifications(created_at DESC, status);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for automatic updated_at updates
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_api_clients_updated_at BEFORE UPDATE ON api_clients
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_channel_configs_updated_at BEFORE UPDATE ON channel_configs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notifications_updated_at BEFORE UPDATE ON notifications
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Comments for documentation
COMMENT ON TABLE users IS 'Admin panel users for authentication and management';
COMMENT ON TABLE api_clients IS 'External systems authorized to send notifications';
COMMENT ON TABLE channel_configs IS 'Configuration for notification channels (SMTP, Telegram, etc.)';
COMMENT ON TABLE notifications IS 'Complete log of all notification requests and their delivery status';
COMMENT ON COLUMN notifications.expires_at IS 'TTL - message expires after 7 days (TZ 2.4)';
