CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO
$$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'clients'
          AND column_name = 'password'
    ) THEN
        ALTER TABLE clients RENAME COLUMN password TO password_hash;
    END IF;
END
$$;

UPDATE clients
SET password_hash = crypt(password_hash, gen_salt('bf'))
WHERE password_hash IS NOT NULL
  AND password_hash !~ '^\$2[aby]\$.{56}$';
