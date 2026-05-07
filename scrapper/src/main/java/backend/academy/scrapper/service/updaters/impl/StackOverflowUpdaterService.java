package backend.academy.scrapper.service.updaters.impl;

import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.db.DbLinkService;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.stackoverflow.StackOverflowQuestionItem;
import backend.academy.scrapper.model.stackoverflow.StackOverflowResponse;
import backend.academy.scrapper.service.parsers.StackOverflowLinkParser;
import backend.academy.scrapper.service.updaters.LinkUpdater;
import backend.academy.scrapper.service.updaters.processors.StackOverflowQuestionUpdateProcessor;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.enums.LinkUpdaterType;

@Service
@RequiredArgsConstructor
public class StackOverflowUpdaterService implements LinkUpdater {

    private final StackOverflowClient stackOverflowClient;
    private final DbLinkService dbLinkService;
    private final StackOverflowLinkParser stackOverflowLinkParser;
    private final List<StackOverflowQuestionUpdateProcessor> questionUpdateProcessors;

    @Override
    public List<LinkUpdateDTO> getUpdates(URI link) {
        List<LinkUpdateDTO> resultUpdatesList = new ArrayList<>();

        Long questionId = stackOverflowLinkParser.parseQuestionId(link.toString());
        Long fromDate = dbLinkService
                .findLastEventDateByLink(link)
                .map(OffsetDateTime::toEpochSecond)
                .orElse(null);
        ResponseEntity<StackOverflowResponse<StackOverflowQuestionItem>> questionsResponse =
                stackOverflowClient.getQuestionById(questionId, "stackoverflow", fromDate);

        if (!(questionsResponse.getStatusCode() == HttpStatus.OK)
                || Objects.requireNonNull(questionsResponse.getBody()).items().isEmpty()) {
            return new ArrayList<>();
        }
        var question =
                Objects.requireNonNull(questionsResponse.getBody()).items().getFirst();

        questionUpdateProcessors.stream()
                .map(processor -> processor.processUpdates(link, questionId, question, fromDate))
                .forEach(resultUpdatesList::addAll);

        resultUpdatesList.stream()
                .map(LinkUpdateDTO::creationDate)
                .filter(Objects::nonNull)
                .max(OffsetDateTime::compareTo)
                .ifPresent(lastEventDate -> dbLinkService.updateLastEventDate(link, lastEventDate));

        return resultUpdatesList;
    }

    @Override
    public LinkUpdaterType getType() {
        return LinkUpdaterType.STACK_OVERFLOW;
    }
}
