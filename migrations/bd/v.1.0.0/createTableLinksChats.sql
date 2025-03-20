CREATE TABLE links_chats (
    chat_id BIGINT REFERENCES chats(id) NOT NULL,
    link_id BIGINT REFERENCES links(id) NOT NULL
)
