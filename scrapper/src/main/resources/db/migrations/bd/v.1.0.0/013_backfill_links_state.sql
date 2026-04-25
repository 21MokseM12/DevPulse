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
