DO
$$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_name = 'chats'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_name = 'clients'
    ) THEN
        ALTER TABLE chats RENAME TO clients;
    END IF;
END
$$;

DO
$$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class
        WHERE relkind = 'S'
          AND relname = 'chats_id_seq'
    ) AND NOT EXISTS (
        SELECT 1
        FROM pg_class
        WHERE relkind = 'S'
          AND relname = 'clients_id_seq'
    ) THEN
        ALTER SEQUENCE chats_id_seq RENAME TO clients_id_seq;
    END IF;
END
$$;

DO
$$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_name = 'clients'
    ) THEN
        ALTER TABLE clients
            ALTER COLUMN id SET DEFAULT nextval('clients_id_seq');
    END IF;
END
$$;

ALTER INDEX IF EXISTS chats_login_unique_idx RENAME TO clients_login_unique_idx;
