CREATE TABLE filters (
    id BIGSERIAL PRIMARY KEY,
    link_id BIGSERIAL NOT NULL REFERENCES links(id),
    filter TEXT NOT NULL
);

CREATE INDEX filter_link_id_index ON filters (link_id);
