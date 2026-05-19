ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS event_url TEXT;
