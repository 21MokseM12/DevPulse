CREATE TABLE tags (
    id BIGSERIAL PRIMARY KEY,
    link_id BIGSERIAL NOT NULL REFERENCES links(id),
    tag TEXT NOT NULL
);

CREATE INDEX tag_link_id_index ON tags (link_id);
