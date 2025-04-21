CREATE TABLE links_chats (
    chat_id BIGINT REFERENCES chats(id) NOT NULL,
    link_id BIGINT REFERENCES links(id) NOT NULL
);

CREATE INDEX chat_id_link_id_index ON links_chats (chat_id, link_id);
