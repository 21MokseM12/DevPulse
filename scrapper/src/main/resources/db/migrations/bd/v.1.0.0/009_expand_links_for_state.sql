ALTER TABLE links
    ADD COLUMN IF NOT EXISTS url TEXT,
    ADD COLUMN IF NOT EXISTS link_type TEXT,
    ADD COLUMN IF NOT EXISTS last_checked_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS etag TEXT,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;

UPDATE links
SET url = COALESCE(url, link),
    last_checked_at = COALESCE(last_checked_at, updated_at),
    created_at = COALESCE(created_at, updated_at),
    link_type = COALESCE(
        link_type,
        CASE
            WHEN COALESCE(url, link) LIKE 'https://github.com/%' THEN 'GITHUB'
            WHEN COALESCE(url, link) LIKE 'https://stackoverflow.com/%' THEN 'STACKOVERFLOW'
            ELSE 'UNKNOWN'
        END
    );

ALTER TABLE links
    ALTER COLUMN url SET NOT NULL,
    ALTER COLUMN last_checked_at SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN link_type SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS links_url_unique_idx ON links (url);
CREATE INDEX IF NOT EXISTS links_last_checked_at_idx ON links (last_checked_at);
CREATE INDEX IF NOT EXISTS links_link_type_idx ON links (link_type);
