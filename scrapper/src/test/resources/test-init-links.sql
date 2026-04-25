-- Clean test data (order respects FK constraints)
DELETE FROM client_links WHERE client_id IN (100, 101);
DELETE FROM processed_ids WHERE link_id IN (1, 2);
DELETE FROM tags WHERE link_id IN (1, 2);
DELETE FROM filters WHERE link_id IN (1, 2);
DELETE FROM links WHERE id IN (1, 2);
DELETE FROM chats WHERE id IN (100, 101);

-- Insert fresh test data
INSERT INTO chats (id, login, password) VALUES
    (100, 'test-client-100', 'test-password-100'),
    (101, 'test-client-101', 'test-password-101');
INSERT INTO links (id, link, updated_at, url, link_type, last_checked_at, etag, created_at) VALUES
    (1, 'https://github.com/owner/repo1', timestamp '2025-03-19 10:30:00', 'https://github.com/owner/repo1', 'GITHUB', timestamp '2025-03-19 10:30:00', null, timestamp '2025-03-19 10:30:00'),
    (2, 'https://github.com/owner/repo2', timestamp '2025-03-19 10:31:00', 'https://github.com/owner/repo2', 'GITHUB', timestamp '2025-03-19 10:31:00', null, timestamp '2025-03-19 10:31:00');
SELECT setval('links_id_seq', 3);
INSERT INTO tags (link_id, tag) VALUES (1, 'tag1'), (1, 'tag2'), (2, 'tag3');
INSERT INTO filters (link_id, filter) VALUES (1, 'filter1'), (2, 'filter2');
INSERT INTO client_links (client_id, link_id, tags, filters, created_at) VALUES
    (100, 1, ARRAY['tag1','tag2'], ARRAY['filter1'], NOW()),
    (100, 2, ARRAY['tag3'], ARRAY['filter2'], NOW());
