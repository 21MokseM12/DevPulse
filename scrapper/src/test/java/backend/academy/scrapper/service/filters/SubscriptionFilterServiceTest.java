package backend.academy.scrapper.service.filters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.model.UpdateType;
import java.time.OffsetDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SubscriptionFilterServiceTest {

    private final SubscriptionFilterService service = new SubscriptionFilterService();

    @Test
    void matches_whenNoFilters_returnsTrue() {
        assertTrue(service.matches(update(), Set.of()));
    }

    @Test
    void matches_authorAndType_shouldRespectAllRules() {
        LinkUpdateDTO update = update();
        assertTrue(service.matches(update, Set.of("author:octocat", "type:github_issue")));
        assertFalse(service.matches(update, Set.of("author:other", "type:github_issue")));
    }

    @Test
    void matches_label_shouldUseUpdateLabels() {
        LinkUpdateDTO update = update();
        assertTrue(service.matches(update, Set.of("label:bug")));
        assertFalse(service.matches(update, Set.of("label:feature")));
    }

    @Test
    void matches_unknownOrMalformedFilters_shouldNotBlockDelivery() {
        LinkUpdateDTO update = update();
        assertTrue(service.matches(update, Set.of("foo:bar", "bad-format")));
    }

    private LinkUpdateDTO update() {
        return new LinkUpdateDTO(
                1L,
                "Issue title",
                "octocat",
                OffsetDateTime.parse("2026-05-05T12:00:00Z"),
                "desc",
                UpdateType.GITHUB_ISSUE,
                Set.of("bug", "urgent"));
    }
}
