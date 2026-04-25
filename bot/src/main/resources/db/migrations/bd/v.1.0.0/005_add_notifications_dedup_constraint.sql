CREATE UNIQUE INDEX IF NOT EXISTS uq_notifications_dedup_event
    ON notifications(link_id, creation_date, update_owner, title);
