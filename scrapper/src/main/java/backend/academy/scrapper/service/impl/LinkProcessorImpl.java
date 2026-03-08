package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.exceptions.ResourceNotFoundException;
import backend.academy.scrapper.service.LinkOperationProcessor;
import backend.academy.scrapper.service.LinkProcessor;
import backend.academy.scrapper.service.validators.LinkValidatorManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.exceptions.BadRequestException;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;
import scrapper.bot.connectivity.model.response.LinkResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkProcessorImpl implements LinkProcessor {

    private static final String BAD_REQUEST_MESSAGE = "Некорректные параметры запроса";
    private static final String NOT_FOUND_MESSAGE = "Ссылка не найдена";

    private final LinkOperationProcessor linkOperationProcessor;
    private final LinkValidatorManager linkValidatorManager;

    @Override
    public List<LinkResponse> findAll(Long chatId) {
        log.info("Начинается обработка запроса на поиск ссылок по id чата: {}", chatId);
        return linkOperationProcessor.findAllByChatId(chatId);
    }

    @Override
    public LinkResponse subscribeLink(Long chatId, AddLinkRequest request) {
        if (!linkValidatorManager.isValidLink(request.link().toString())) {
            log.warn("По id {} передан невалидный запрос на подписку: {}", chatId, request);
            throw new BadRequestException(BAD_REQUEST_MESSAGE);
        }
        log.info("Начинается обработка запроса на подписку ссылки {} с id чата: {}", request, chatId);
        return linkOperationProcessor.subscribe(chatId, request)
            .orElseThrow(() -> new BadRequestException(BAD_REQUEST_MESSAGE));
    }

    @Override
    public LinkResponse unsubscribeLink(Long chatId, RemoveLinkRequest request) {
        if (!linkValidatorManager.isValidLink(request.link().toString())) {
            log.warn("По id {} передан невалидный запрос на отписку: {}", chatId, request);
            throw new BadRequestException(BAD_REQUEST_MESSAGE);
        }
        log.info("Начинается обработка запроса на отписку ссылки {} с id чата: {}", request, chatId);
        return linkOperationProcessor.unsubscribe(chatId, request)
            .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE));
    }
}
