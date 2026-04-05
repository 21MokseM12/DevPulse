package backend.academy.scrapper.kafka.listener;

import backend.academy.scrapper.config.CommonKafkaConfig;
import backend.academy.scrapper.kafka.service.impl.ClientListenerProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientListener {

    private static final String CONSUMER_ID = "clientListener";

    private final ClientListenerProvider clientProvider;

    @RetryableTopic(
        attempts = "${kafka.properties.retry-policy.max-attempts}",
        backoff = @Backoff(
            delayExpression = "${kafka.properties.retry-policy.interval}",
            multiplierExpression = "${kafka.properties.retry-policy.multiplier}",
            maxDelayExpression = "${kafka.properties.retry-policy.max-delay}"
        ),
        autoCreateTopics = "${kafka.properties.retry-policy.auto-create-topics}",
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(
        id = CONSUMER_ID,
        groupId = "${kafka.consumers.client-listener.group-id}",
        concurrency = "${kafka.consumers.client-listener.concurrency}",
        topics = {"${kafka.consumers.client-listener.topic}"},
        containerFactory = CommonKafkaConfig.STRING_VALUE_CONTAINER_FACTORY
    )
    public void process(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            clientProvider.provide(record);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error(
                "Ошибка при получении сообщения в consumer с id: {} по ошибке: {}", CONSUMER_ID, e.getMessage());
        }
    }
}
