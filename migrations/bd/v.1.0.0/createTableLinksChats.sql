CREATE TABLE links_chats (
    chat_id BIGINT REFERENCES chats(id),
    link_id BIGINT REFERENCES links(id)
)
