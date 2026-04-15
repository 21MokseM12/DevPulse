package backend.academy.scrapper.controller;

import backend.academy.scrapper.service.ChatOperationProcessor;
import backend.academy.scrapper.exceptions.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.request.ClientCredentialsRequest;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/tg-chat", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChatController {

    private final ChatOperationProcessor chatOperationProcessor;

    @PostMapping
    public ResponseEntity<Void> registerChat(@Valid @RequestBody ClientCredentialsRequest request) throws BadRequestException {
        if (request.login() == null || request.password() == null
            || request.login().isBlank() || request.password().isBlank()) {
            throw new BadRequestException("Некорректные параметры запроса");
        }
        log.info("Получен запрос на регистрацию пользователя с login {}", request.login());
        if (!chatOperationProcessor.register(request.login(), request.password())) {
            throw new BadRequestException("Пользователь с таким логином уже существует");
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unregisterChat(@Valid @RequestBody ClientCredentialsRequest request)
        throws BadRequestException, ResourceNotFoundException {
        if (request.login() == null || request.password() == null
            || request.login().isBlank() || request.password().isBlank()) {
            throw new BadRequestException("Некорректные параметры запроса");
        }
        log.info("Получен запрос на удаление пользователя с login {}", request.login());
        if (!chatOperationProcessor.unregister(request.login(), request.password())) {
            throw new ResourceNotFoundException("Чат не существует");
        }
        return ResponseEntity.ok().build();
    }
}

