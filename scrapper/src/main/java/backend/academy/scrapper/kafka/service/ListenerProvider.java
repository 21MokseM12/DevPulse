package backend.academy.scrapper.kafka.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface ListenerProvider {
    void provide(ConsumerRecord<String, String> record);
}
