package backend.academy.scrapper.kafka.service.impl;

import backend.academy.scrapper.exceptions.KafkaProcessException;
import backend.academy.scrapper.kafka.sender.KafkaLinkSender;
import backend.academy.scrapper.kafka.service.ListenerProvider;
import backend.academy.scrapper.model.kafka.LinkMessage;
import backend.academy.scrapper.service.ChatOperationProcessor;
import backend.academy.scrapper.service.LinkProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkListenerProvider implements ListenerProvider {

    private static final String ERROR_MESSAGE = "Ошибка при обработке запроса: {} по действию {}: {}";
    private static final TypeReference<LinkMessage> TYPE_REFERENCE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final LinkProcessor processor;
    private final ChatOperationProcessor chatOperationProcessor;
    private final KafkaLinkSender sender;

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

    private void provideAction(@NonNull LinkMessage request) {
        switch (request.action()) {
            case FIND_ALL -> provideFindAllRequest(request);
            case SUBSCRIBE -> provideSubscribeRequest(request);
            case UNSUBSCRIBE -> provideUnsubscribeRequest(request);
        }
    }

    private void provideFindAllRequest(LinkMessage request) {
        try {
            Long chatId = resolveChatId(request);
            var response = processor.findAll(chatId);
            sender.send(response);
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, request, request.action(), e.getMessage());
            throw new KafkaProcessException(e.getMessage());
        }
    }

    private void provideSubscribeRequest(LinkMessage request) {
        try {
            Long chatId = resolveChatId(request);
            var response = processor.subscribeLink(chatId, request.add());
            sender.send(List.of(response));
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, request, request.action(), e.getMessage());
            throw new KafkaProcessException(e.getMessage());
        }
    }

    private void provideUnsubscribeRequest(LinkMessage request) {
        try {
            Long chatId = resolveChatId(request);
            var response = processor.unsubscribeLink(chatId, request.remove());
            sender.send(List.of(response));
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, request, request.action(), e.getMessage());
            throw new KafkaProcessException(e.getMessage());
        }
    }

    private Long resolveChatId(@NonNull LinkMessage request) {
        return chatOperationProcessor.findClientId(request.login(), request.password())
            .orElseThrow(() -> new KafkaProcessException("Клиент не найден"));
    }
}
