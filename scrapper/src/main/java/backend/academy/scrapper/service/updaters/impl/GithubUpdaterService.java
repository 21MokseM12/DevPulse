package backend.academy.scrapper.service.updaters.impl;

import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.db.DbLinkService;
import backend.academy.scrapper.enums.GithubActionType;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.github.GithubCommit;
import backend.academy.scrapper.model.github.GithubCompareCommit;
import backend.academy.scrapper.model.github.GithubCompareResponse;
import backend.academy.scrapper.model.github.GithubPayload;
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

    private static final String ZERO_SHA = "0000000000000000000000000000000000000000";

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
        String owner = linkParser.parseUsername(link.toString());
        String repo = linkParser.parseRepo(link.toString());
        ResponseEntity<List<GithubResponse>> events = resilienceExecutor.execute(
                "github-api", () -> githubClient.getEvents(owner, repo, etag, ifModifiedSince));
        if (events == null) {
            log.warn("Github API вернул пустой ответ для ссылки {}", link);
            return new ArrayList<>();
        }

        if (events.getStatusCode() == HttpStatus.NOT_MODIFIED) {
            updatePollState(link, events);
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
        List<GithubResponse> enrichedUpdates = enrichPushEventsWithCommits(owner, repo, updates, link);
        List<LinkUpdateDTO> resultList = new ArrayList<>();
        if (!enrichedUpdates.isEmpty()) {
            updateProcessors.stream()
                    .map(processor -> processor.processUpdates(link, enrichedUpdates))
                    .forEach(resultList::addAll);
            if (resultList.isEmpty()) {
                log.info(
                        "Github API вернул {} событий для ссылки {}, но ни одно не подходит под текущие фильтры действий.",
                        enrichedUpdates.size(),
                        link);
            }
        }
        if (resultList.isEmpty()) {
            updatePollState(link, events);
        }
        return resultList;
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

    private List<GithubResponse> enrichPushEventsWithCommits(
            String owner, String repo, List<GithubResponse> updates, URI link) {
        return updates.stream()
                .map(event -> enrichPushEventWithCompare(owner, repo, event, link))
                .toList();
    }

    private GithubResponse enrichPushEventWithCompare(String owner, String repo, GithubResponse event, URI link) {
        if (!GithubActionType.PUSH_EVENT.type().equals(event.type())
                || event.payload() == null
                || hasCommits(event.payload())) {
            return event;
        }
        String before = event.payload().before();
        String head = event.payload().head();
        if (!isComparableRange(before, head)) {
            return event;
        }

        ResponseEntity<GithubCompareResponse> compareResponse;
        try {
            compareResponse = resilienceExecutor.execute(
                    "github-api", () -> githubClient.compareCommits(owner, repo, before + "..." + head));
        } catch (RuntimeException ex) {
            log.warn(
                    "Не удалось получить compare для PushEvent {} ({}...{}) по ссылке {}",
                    event.id(),
                    before,
                    head,
                    link,
                    ex);
            return event;
        }

        if (compareResponse == null || !compareResponse.getStatusCode().is2xxSuccessful()) {
            return event;
        }

        GithubCompareResponse body = compareResponse.getBody();
        if (body == null || body.commits() == null || body.commits().isEmpty()) {
            return event;
        }

        List<GithubCommit> commits = body.commits().stream()
                .filter(compareCommit -> compareCommit != null)
                .map(this::toGithubCommit)
                .toList();
        if (commits.isEmpty()) {
            return event;
        }

        GithubPayload payload = event.payload();
        GithubPayload enrichedPayload = new GithubPayload(
                payload.action(),
                payload.pullRequest(),
                payload.issue(),
                payload.ref(),
                payload.head(),
                payload.before(),
                commits);
        return new GithubResponse(event.id(), event.type(), event.actor(), event.creationDate(), enrichedPayload);
    }

    private GithubCommit toGithubCommit(GithubCompareCommit commit) {
        String message = commit.commit() == null ? null : commit.commit().message();
        return new GithubCommit(commit.sha(), message);
    }

    private boolean hasCommits(GithubPayload payload) {
        return payload.commits() != null && !payload.commits().isEmpty();
    }

    private boolean isComparableRange(String before, String head) {
        return before != null
                && !before.isBlank()
                && !ZERO_SHA.equals(before)
                && head != null
                && !head.isBlank()
                && !ZERO_SHA.equals(head)
                && !before.equals(head);
    }
}
