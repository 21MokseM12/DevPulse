update kafka_outbox
set sent = true,
    sent_at = :sentAt,
    attempt_count = attempt_count + :attempts
where id = :id;
