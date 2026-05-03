select id, topic, payload::text as payload
from kafka_outbox
where sent = false
order by created_at asc, id asc
limit :batchSize;
