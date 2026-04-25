INSERT INTO client_links (client_id, link_id, tags, filters, created_at)
SELECT
    lc.chat_id AS client_id,
    lc.link_id AS link_id,
    COALESCE(t.tags, ARRAY[]::TEXT[]) AS tags,
    COALESCE(f.filters, ARRAY[]::TEXT[]) AS filters,
    COALESCE(l.created_at, l.updated_at, NOW()) AS created_at
FROM links_chats lc
JOIN links l
    ON l.id = lc.link_id
LEFT JOIN (
    SELECT link_id, ARRAY_AGG(DISTINCT tag) AS tags
    FROM tags
    GROUP BY link_id
) t ON t.link_id = lc.link_id
LEFT JOIN (
    SELECT link_id, ARRAY_AGG(DISTINCT filter) AS filters
    FROM filters
    GROUP BY link_id
) f ON f.link_id = lc.link_id
ON CONFLICT (client_id, link_id) DO NOTHING;
