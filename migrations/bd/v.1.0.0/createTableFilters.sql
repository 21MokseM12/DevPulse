CREATE TABLE filters (
    id SERIAL PRIMARY KEY,
    link_id BIGINT NOT NULL REFERENCES links(id),
    filter TEXT NOT NULL
)
