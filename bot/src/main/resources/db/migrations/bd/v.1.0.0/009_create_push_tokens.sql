CREATE TABLE IF NOT EXISTS push_tokens (
    id BIGSERIAL PRIMARY KEY,
    client_login VARCHAR(255) NOT NULL REFERENCES clients(login) ON DELETE CASCADE,
    platform VARCHAR(32) NOT NULL,
    token TEXT NOT NULL,
    app_version VARCHAR(64),
    device_id VARCHAR(255),
    status VARCHAR(32) NOT NULL DEFAULT 'active',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_push_tokens_platform CHECK (platform IN ('android')),
    CONSTRAINT chk_push_tokens_status CHECK (status IN ('active', 'inactive', 'invalid')),
    CONSTRAINT uq_push_tokens_platform_token UNIQUE (platform, token)
);

CREATE INDEX IF NOT EXISTS idx_push_tokens_client_status ON push_tokens(client_login, status);
