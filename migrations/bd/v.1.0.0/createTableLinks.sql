CREATE TABLE links (
    id BIGSERIAL PRIMARY KEY,
    link TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX link_value_index ON links (link);
CREATE INDEX link_updated_time_index ON links (updated_at);
