package backend.academy.scrapper.controller;

import backend.academy.scrapper.service.ChatOperationProcessor;
import backend.academy.scrapper.exceptions.ResourceNotFoundException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scrapper.bot.connectivity.exceptions.BadRequestException;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/tg-chat/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChatController {

    private final ChatOperationProcessor chatOperationProcessor;

    @PostMapping
    public ResponseEntity<Void> registerChat(@PathVariable @NotNull @Positive Long id) throws BadRequestException {
        log.info("Получен запрос на регистрацию пользователя с id {}", id);
        chatOperationProcessor.register(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unregisterChat(@PathVariable @NotNull @Positive Long id)
        throws BadRequestException, ResourceNotFoundException {
        log.info("Получен запрос на удаление пользователя с id {}", id);
        if (!chatOperationProcessor.unregister(id)) {
            throw new ResourceNotFoundException("Чат не существует");
        }
        return ResponseEntity.ok().build();
    }
}

