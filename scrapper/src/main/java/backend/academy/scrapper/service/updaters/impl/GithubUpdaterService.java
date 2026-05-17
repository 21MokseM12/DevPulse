package backend.academy.scrapper.service.updaters.impl;

import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.db.DbLinkService;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.github.GithubResponse;
import backend.academy.scrapper.service.parsers.GithubLinkParser;
import backend.academy.scrapper.service.resilience.ExternalApiResilienceExecutor;
import backend.academy.scrapper.service.updaters.LinkUpdater;
import backend.academy.scrapper.service.updaters.processors.GithubRepoUpdateProcessor;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import scrapper.bot.connectivity.enums.LinkUpdaterType;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubUpdaterService implements LinkUpdater {

    private final GithubClient githubClient;
    private final DbLinkService dbLinkService;
    private final GithubLinkParser linkParser;
    private final List<GithubRepoUpdateProcessor> updateProcessors;
    private final ExternalApiResilienceExecutor resilienceExecutor;

    @Override
    public List<LinkUpdateDTO> getUpdates(URI link) {
        String etag = dbLinkService.findEtagByLink(link).orElse(null);
        String ifModifiedSince =
                dbLinkService.findLastModifiedByLink(link).map(this::toHttpDate).orElse(null);
        ResponseEntity<List<GithubResponse>> events = resilienceExecutor.execute(
                "github-api",
                () -> githubClient.getEvents(
                        linkParser.parseUsername(link.toString()),
                        linkParser.parseRepo(link.toString()),
                        etag,
                        ifModifiedSince));
        if (events == null) {
            log.warn("Github API вернул пустой ответ для ссылки {}", link);
            return new ArrayList<>();
        }

        updatePollState(link, events);

        if (events.getStatusCode() == HttpStatus.NOT_MODIFIED) {
            return new ArrayList<>();
        }

        if (!events.getStatusCode().is2xxSuccessful()) {
            log.warn(
                    "Github API вернул статус {} для ссылки {}. X-RateLimit-Remaining={}, X-RateLimit-Reset={}",
                    events.getStatusCode().value(),
                    link,
                    events.getHeaders().getFirst("X-RateLimit-Remaining"),
                    events.getHeaders().getFirst("X-RateLimit-Reset"));
            throw new IllegalStateException("Github API вернул неуспешный статус: "
                    + events.getStatusCode().value());
        }

        List<GithubResponse> responseBody = events.getBody();
        List<GithubResponse> updates = responseBody == null ? List.of() : responseBody;
        if (!updates.isEmpty()) {
            List<LinkUpdateDTO> resultList = new ArrayList<>();
            updateProcessors.stream()
                    .map(processor -> processor.processUpdates(link, updates))
                    .forEach(resultList::addAll);
            if (resultList.isEmpty()) {
                log.info(
                        "Github API вернул {} событий для ссылки {}, но ни одно не подходит под текущие фильтры действий.",
                        updates.size(),
                        link);
            }
            return resultList;
        }
        return new ArrayList<>();
    }

    @Override
    public LinkUpdaterType getType() {
        return LinkUpdaterType.GITHUB;
    }

    private void updatePollState(URI link, ResponseEntity<List<GithubResponse>> events) {
        if (events.getHeaders().getETag() != null) {
            dbLinkService.updateEtag(link, events.getHeaders().getETag());
        }
        String lastModified = events.getHeaders().getFirst("Last-Modified");
        if (lastModified != null) {
            parseHttpDate(lastModified).ifPresent(parsedDate -> dbLinkService.updateLastModified(link, parsedDate));
        }
    }

    private String toHttpDate(OffsetDateTime value) {
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(value.atZoneSameInstant(ZoneOffset.UTC));
    }

    private java.util.Optional<OffsetDateTime> parseHttpDate(String value) {
        try {
            return java.util.Optional.of(OffsetDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME));
        } catch (DateTimeParseException ex) {
            log.warn("Не удалось распарсить Last-Modified от Github: {}", value);
            return java.util.Optional.empty();
        }
    }
}
