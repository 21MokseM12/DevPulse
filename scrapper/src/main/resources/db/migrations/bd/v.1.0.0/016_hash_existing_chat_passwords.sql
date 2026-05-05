CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO
$$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'chats'
          AND column_name = 'password'
    ) THEN
        ALTER TABLE chats RENAME COLUMN password TO password_hash;
    END IF;
END
$$;

UPDATE chats
SET password_hash = crypt(password_hash, gen_salt('bf'))
WHERE password_hash IS NOT NULL
  AND password_hash !~ '^\$2[aby]\$.{56}$';
