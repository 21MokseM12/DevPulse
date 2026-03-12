package backend.academy.scrapper.integration_test.kafka;

import backend.academy.scrapper.config.CommonKafkaConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import scrapper.bot.connectivity.model.response.LinkResponse;

@Component
public class LinkResponseCapture {

    private static final TypeReference<List<LinkResponse>> TYPE_REF = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final BlockingQueue<List<LinkResponse>> responses = new LinkedBlockingQueue<>();

    public LinkResponseCapture(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
        topics = "${kafka.producers.common-sender.topic}",
        groupId = "link-response-test-capture",
        containerFactory = CommonKafkaConfig.STRING_VALUE_CONTAINER_FACTORY
    )
    public void capture(String message, org.springframework.kafka.support.Acknowledgment ack) {
        try {
            List<LinkResponse> response = objectMapper.readValue(message, TYPE_REF);
            responses.offer(response);
        } catch (Exception e) {
            // ignore parse errors in test
        } finally {
            ack.acknowledge();
        }
    }

    public List<LinkResponse> pollResponse(long timeout, TimeUnit unit) throws InterruptedException {
        return responses.poll(timeout, unit);
    }

    public void clear() {
        responses.clear();
    }
}
