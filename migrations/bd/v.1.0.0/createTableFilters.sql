CREATE TABLE filters (
    id BIGSERIAL PRIMARY KEY,
    link_id BIGINT NOT NULL REFERENCES links(id),
    filter TEXT NOT NULL
)
