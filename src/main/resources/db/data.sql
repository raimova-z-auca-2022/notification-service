-- Sample data for Notification Service
-- This file contains initial data for development and testing

-- Insert default admin user
-- Password: admin123 (BCrypt encoded with proper hash)
-- ВАЖНО: Этот хеш сгенерирован для пароля 'admin123' с BCrypt strength 10
INSERT INTO users (username, password, email, full_name, is_active) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.BwONnqQQ3jQf8yHuxe', 'admin@notification-service.com', 'System Administrator', true),
('testuser', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.BwONnqQQ3jQf8yHuxe', 'test@notification-service.com', 'Test User', true);

-- Insert sample API clients
INSERT INTO api_clients (client_name, api_key, is_active) VALUES
('CRM System', 'crm-api-key-12345678901234567890123456789012', true),
('E-Commerce Platform', 'ecommerce-api-key-98765432109876543210987654321098', true),
('Mobile App', 'mobile-api-key-11111111111111111111111111111111', true),
('Website', 'website-api-key-22222222222222222222222222222222', false);

-- Insert channel configurations
INSERT INTO channel_configs (channel_name, credentials, is_enabled) VALUES
('EMAIL', '{"smtp_host": "smtp.gmail.com", "smtp_port": 587, "username": "notifications@example.com", "from_name": "Notification Service"}', true),
('TELEGRAM', '{"bot_token": "your-telegram-bot-token-here", "api_url": "https://api.telegram.org"}', false),
('WHATSAPP', '{"api_key": "your-whatsapp-api-key-here", "api_url": "https://api.whatsapp.com"}', false),
('SMS', '{"provider": "twilio", "account_sid": "your-account-sid", "auth_token": "your-auth-token", "from_number": "+1234567890"}', false);

-- Insert sample notification records for testing
INSERT INTO notifications (client_id, channel_type, recipient, subject, message_body, status, retry_count, created_at, sent_at) VALUES
(1, 'EMAIL', 'user1@example.com', 'Welcome to Our Service', 'Thank you for registering with our service!', 'SENT', 0, CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
(1, 'EMAIL', 'user2@example.com', 'Password Reset', 'Click here to reset your password.', 'SENT', 0, CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '1 hour'),
(2, 'EMAIL', 'customer@example.com', 'Order Confirmation', 'Your order has been confirmed.', 'SENT', 0, CURRENT_TIMESTAMP - INTERVAL '30 minutes', CURRENT_TIMESTAMP - INTERVAL '30 minutes'),
(2, 'SMS', '+1234567890', NULL, 'Your verification code is: 123456', 'PENDING', 0, CURRENT_TIMESTAMP - INTERVAL '10 minutes', NULL),
(1, 'EMAIL', 'failed@example.com', 'Test Email', 'This is a test message.', 'FAILED', 3, CURRENT_TIMESTAMP - INTERVAL '15 minutes', NULL);

-- Update error message for failed notification
UPDATE notifications 
SET error_message = 'SMTP connection timeout after 3 retry attempts' 
WHERE status = 'FAILED';

-- Statistics query examples (for reference)
-- SELECT COUNT(*) as total_notifications FROM notifications;
-- SELECT status, COUNT(*) as count FROM notifications GROUP BY status;
-- SELECT channel_type, COUNT(*) as count FROM notifications GROUP BY channel_type;
