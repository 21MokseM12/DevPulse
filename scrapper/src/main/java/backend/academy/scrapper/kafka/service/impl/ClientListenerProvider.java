package backend.academy.scrapper.kafka.service.impl;

import backend.academy.scrapper.exceptions.KafkaProcessException;
import backend.academy.scrapper.kafka.service.ListenerProvider;
import backend.academy.scrapper.model.kafka.ClientMessage;
import backend.academy.scrapper.service.ChatOperationProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientListenerProvider implements ListenerProvider {

    private static final TypeReference<ClientMessage> TYPE_REFERENCE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final ChatOperationProcessor chatOperationProcessor;

    @Override
    public void provide(ConsumerRecord<String, String> record) {
        try {
            var request = objectMapper.readValue(record.value(), TYPE_REFERENCE);
            provideAction(request);
        } catch (JsonProcessingException e) {
            log.warn("Ошибка при маппинге сообщения из топика: {}", record.topic());
            throw new KafkaProcessException(e.getMessage());
        }
    }

    private void provideAction(@NonNull ClientMessage request) {
        boolean success = switch (request.action()) {
            case REGISTER -> chatOperationProcessor.register(request.login(), request.password());
            case UNREGISTER -> chatOperationProcessor.unregister(request.login(), request.password());
        };
        if (!success) {
            throw new KafkaProcessException("Произошла ошибка при проведении операции по запросу: " + request);
        }
    }
}
