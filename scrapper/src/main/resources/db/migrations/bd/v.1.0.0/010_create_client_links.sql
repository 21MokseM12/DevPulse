CREATE TABLE IF NOT EXISTS client_links (
    client_id BIGINT NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    link_id BIGINT NOT NULL REFERENCES links(id) ON DELETE CASCADE,
    tags TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
    filters TEXT[] NOT NULL DEFAULT ARRAY[]::TEXT[],
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (client_id, link_id)
);

CREATE INDEX IF NOT EXISTS client_links_link_id_idx ON client_links (link_id);
