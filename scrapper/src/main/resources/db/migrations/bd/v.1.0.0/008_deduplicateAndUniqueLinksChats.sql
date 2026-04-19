-- Remove duplicate (chat_id, link_id) rows, keeping one row per pair (stable: smallest ctid).
DELETE FROM links_chats AS a
    USING links_chats AS b
WHERE a.ctid > b.ctid
  AND a.chat_id = b.chat_id
  AND a.link_id = b.link_id;

-- Replaced by UNIQUE constraint below (same columns).
DROP INDEX IF EXISTS chat_id_link_id_index;

ALTER TABLE links_chats
    ADD CONSTRAINT links_chats_chat_id_link_id_key UNIQUE (chat_id, link_id);
