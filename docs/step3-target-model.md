## Step 3 Target Data Model

This document fixes the target schema before SQL migrations.

### Target Tables

- `links`
  - Purpose: store polling lifecycle per tracked URL.
  - Columns:
    - `id BIGSERIAL PRIMARY KEY`
    - `url TEXT NOT NULL`
    - `link_type TEXT NOT NULL` (`GITHUB`, `STACKOVERFLOW`, `UNKNOWN`)
    - `last_checked_at TIMESTAMP NOT NULL`
    - `etag TEXT NULL`
    - `created_at TIMESTAMP NOT NULL`
  - Notes:
    - Legacy columns `link`, `updated_at` remain during transition.

- `client_links`
  - Purpose: client-to-link subscription and client-specific metadata.
  - Columns:
    - `client_id BIGINT NOT NULL REFERENCES chats(id) ON DELETE CASCADE`
    - `link_id BIGINT NOT NULL REFERENCES links(id) ON DELETE CASCADE`
    - `tags TEXT[] NOT NULL DEFAULT '{}'`
    - `filters TEXT[] NOT NULL DEFAULT '{}'`
    - `created_at TIMESTAMP NOT NULL`
  - Constraints:
    - `PRIMARY KEY (client_id, link_id)`

- `poll_state`
  - Purpose: polling/retry/backoff/dedup state per link.
  - Columns:
    - `link_id BIGINT PRIMARY KEY REFERENCES links(id) ON DELETE CASCADE`
    - `next_poll_at TIMESTAMP NOT NULL`
    - `retry_count INT NOT NULL DEFAULT 0`
    - `backoff_until TIMESTAMP NULL`
    - `last_event_hash TEXT NULL`
    - `last_success_at TIMESTAMP NULL`
    - `last_error TEXT NULL`

- `kafka_outbox`
  - Purpose: reliable event queue for Kafka publishing.
  - Columns:
    - `id BIGSERIAL PRIMARY KEY`
    - `topic TEXT NOT NULL`
    - `payload JSONB NOT NULL`
    - `created_at TIMESTAMP NOT NULL`
    - `sent BOOLEAN NOT NULL DEFAULT FALSE`
    - `sent_at TIMESTAMP NULL`
    - `attempt_count INT NOT NULL DEFAULT 0`

### Old -> New Mapping

- `links_chats` + `tags` + `filters` -> `client_links`
  - key mapping: `links_chats.chat_id -> client_links.client_id`
  - key mapping: `links_chats.link_id -> client_links.link_id`
  - tags/filters are aggregated by `link_id` from legacy tables into arrays.

- `links.updated_at` -> `links.last_checked_at`

- `links.link` -> `links.url`

- `processed_ids`
  - kept as legacy-compatible table for this stage.
  - dedup/backoff state is initialized in `poll_state`.
