CREATE SEQUENCE IF NOT EXISTS chats_id_seq START WITH 1 INCREMENT BY 1;

ALTER TABLE chats
    ALTER COLUMN id SET DEFAULT nextval('chats_id_seq');

SELECT setval('chats_id_seq', COALESCE((SELECT MAX(id) FROM chats), 0) + 1, false);

ALTER TABLE chats
    ADD COLUMN IF NOT EXISTS login VARCHAR(255),
    ADD COLUMN IF NOT EXISTS password VARCHAR(255);

CREATE UNIQUE INDEX IF NOT EXISTS chats_login_unique_idx
    ON chats (login)
    WHERE login IS NOT NULL;
