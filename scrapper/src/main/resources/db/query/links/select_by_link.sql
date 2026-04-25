SELECT
    l.id,
    l.url,
    l.last_checked_at,
    l.created_at,
    COALESCE(
        ARRAY(SELECT DISTINCT tag_item FROM client_links cl, unnest(cl.tags) AS tag_item WHERE cl.link_id = l.id),
        ARRAY[]::TEXT[]
    ) AS tags,
    COALESCE(
        ARRAY(SELECT DISTINCT filter_item FROM client_links cl, unnest(cl.filters) AS filter_item WHERE cl.link_id = l.id),
        ARRAY[]::TEXT[]
    ) AS filters
FROM links l
WHERE l.url = :url;
