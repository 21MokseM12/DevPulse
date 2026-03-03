-- todo протестировать
SELECT
    l.id,
    l.link,
    l.updated_at,
    COALESCE(
        ARRAY(SELECT t.tag FROM tags t WHERE t.link_id = l.id),
        ARRAY[]::TEXT[]
    ) AS tags,
    COALESCE(
        ARRAY(SELECT f.filter FROM filters f WHERE f.link_id = l.id),
        ARRAY[]::TEXT[]
    ) AS filters
FROM links l
WHERE l.id = :link_id;
