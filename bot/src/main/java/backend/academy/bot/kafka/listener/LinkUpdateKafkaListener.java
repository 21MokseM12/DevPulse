package backend.academy.bot.kafka.listener;

import backend.academy.bot.config.KafkaConsumerConfig;
import backend.academy.bot.service.notifications.LinkUpdateProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.model.LinkUpdate;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkUpdateKafkaListener {

    private final LinkUpdateProcessingService linkUpdateProcessingService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            id = "bot-link-updates-listener",
            groupId = "${app.kafka.consumers.link-updates.group-id}",
            topics = "${app.kafka.consumers.link-updates.topic}",
            containerFactory = KafkaConsumerConfig.LINK_UPDATES_LISTENER_CONTAINER_FACTORY)
    public void consume(String payload) throws Exception {
        LinkUpdate update = objectMapper.readValue(payload, LinkUpdate.class);
        linkUpdateProcessingService.process(update);
        log.info("LinkUpdate from Kafka processed successfully");
    }
}
