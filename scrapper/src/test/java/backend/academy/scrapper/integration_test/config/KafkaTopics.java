package backend.academy.scrapper.integration_test.config;

import java.util.List;

public final class KafkaTopics {

    public static final List<String> REQUIRED_TOPICS = List.of(
            "link-updates", "client-listener-topic-request", "link-listener-topic-request", "link-topic-response");

    private KafkaTopics() {}
}
