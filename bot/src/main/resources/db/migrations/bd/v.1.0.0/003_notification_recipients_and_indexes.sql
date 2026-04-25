CREATE TABLE IF NOT EXISTS notification_recipients (
    notification_id BIGINT NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    client_login VARCHAR(255) NOT NULL REFERENCES clients(login) ON DELETE CASCADE,
    PRIMARY KEY (notification_id, client_login)
);

CREATE INDEX IF NOT EXISTS idx_notifications_link_id ON notifications(link_id);
CREATE INDEX IF NOT EXISTS idx_notifications_received_at ON notifications(received_at);
CREATE INDEX IF NOT EXISTS idx_notification_recipients_client_login ON notification_recipients(client_login);
