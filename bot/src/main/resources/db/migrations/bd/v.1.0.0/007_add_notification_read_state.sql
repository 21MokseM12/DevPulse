ALTER TABLE notification_recipients
    ADD COLUMN IF NOT EXISTS read_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_notification_recipients_client_read_at
    ON notification_recipients (client_login, read_at);
