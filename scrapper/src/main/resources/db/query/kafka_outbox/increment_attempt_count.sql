update kafka_outbox
set attempt_count = attempt_count + :attempts
where id = :id;
