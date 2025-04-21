CREATE TABLE processed_ids (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    link_id BIGSERIAL REFERENCES links(id),
    processed_id BIGSERIAL NOT NULL,
    type TEXT NOT NULL
);

CREATE INDEX processed_id_link_id_index ON processed_ids (link_id);
