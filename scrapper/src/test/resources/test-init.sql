INSERT INTO chats (id) VALUES (123), (1), (5), (6), (7);
INSERT INTO links (id, link, updated_at) VALUES
    (1, 'https://github.com/21MokseM12/Log-analyzer-Tbank-project', timestamp '2025-03-19 10:30:00'),
    (2, 'https://stackoverflow.com/questions/79479661/i-set-up-a-due-date-reminder-in-sharepoint-ms-lists-through-power-automate-th', timestamp '2025-03-19 10:31:00'),
    (3, 'https://github.com/21MokseM12/Gallows-game-Tbank-project', timestamp '2025-03-19 10:32:00'),
    (4, 'https://github.com/21MokseM12/Knowledge-RAG-backend', timestamp '2025-03-25 20:03:00');

SELECT setval('links_id_seq', 5);

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
INSERT INTO processed_ids (id, link_id, processed_id, type) VALUES
    (1, 1, 34, 'github_pull_request'),
    (2, 1, 36, 'github_pull_request'),
    (3, 1, 48, 'github_issue'),
    (4, 1, 34, 'github_issue'),
    (5, 2, 34, 'stackoverflow_answer'),
    (6, 2, 34, 'stackoverflow_comment'),
    (7, 2, 35, 'stackoverflow_comment');

SELECT setval('processed_ids_id_seq', 8);
