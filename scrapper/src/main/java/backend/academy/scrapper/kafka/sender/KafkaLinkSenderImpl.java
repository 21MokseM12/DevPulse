package backend.academy.scrapper.kafka.sender;

import backend.academy.scrapper.config.CommonKafkaConfig;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.model.response.LinkResponse;

@Slf4j
@Service
public class KafkaLinkSenderImpl implements KafkaLinkSender {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.producers.common-sender.topic}")
    private String linkTopic;

    public KafkaLinkSenderImpl(
        @Qualifier(CommonKafkaConfig.COMMON_KAFKA_TEMPLATE)
        KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(List<LinkResponse> response) {
        kafkaTemplate.send(linkTopic, response)
            .thenAccept(result -> log.info("Сообщение успешно отправлено: {}", result))
            .exceptionally(result -> {
                log.error("При отправке сообщения произошла ошибка: {}", result.getMessage());
                return null;
            });
    }
}
