INSERT INTO chats (id) VALUES (123), (1), (5), (6), (7);
INSERT INTO links (id, link, updated_at) VALUES
    (1, 'https://github.com/21MokseM12/Log-analyzer-Tbank-project', timestamp '2025-03-19 10:30:00'),
    (2, 'https://stackoverflow.com/questions/79479661/i-set-up-a-due-date-reminder-in-sharepoint-ms-lists-through-power-automate-th', timestamp '2025-03-19 10:31:00'),
    (3, 'https://github.com/21MokseM12/Gallows-game-Tbank-project', timestamp '2025-03-19 10:32:00');

SELECT setval('links_id_seq', 4);

INSERT INTO tags (link_id, tag) VALUES
    (1, 'logger'),
    (1, 'tinkoff'),
    (2, 'setup'),
    (2, 'sharepoint'),
    (3, 'gallows'),
    (3, 'tinkoff');
INSERT INTO filters (link_id, filter) VALUES
    (1, 'pet-project'),
    (1, 'project'),
    (2, 'question'),
    (2, 'project'),
    (3, 'game'),
    (3, 'tinkoff');
INSERT INTO links_chats (chat_id, link_id) VALUES
    (5, 1),
    (6, 2),
    (123, 3),
    (5, 2);
