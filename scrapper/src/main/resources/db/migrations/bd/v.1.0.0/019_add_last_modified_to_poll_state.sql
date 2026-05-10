ALTER TABLE poll_state
    ADD COLUMN IF NOT EXISTS last_modified_at TIMESTAMP;
