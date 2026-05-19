package backend.academy.bot.service.push;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import scrapper.bot.connectivity.model.LinkUpdate;

class PushPayloadBuilderTest {
    private final PushPayloadBuilder builder = new PushPayloadBuilder();

    @Test
    void build_containsRequiredAndOptionalFields() {
        LinkUpdate update = new LinkUpdate(
                101L,
                URI.create("https://github.com/org/repo/issues/101"),
                URI.create("https://github.com/org/repo/issues/101"),
                "Issue updated",
                "octocat",
                "New comment",
                OffsetDateTime.parse("2026-05-01T10:15:30Z"),
                List.of("alice"));

        PushMessagePayload payload = builder.build(5001L, update);

        assertThat(payload.eventId()).isEqualTo("5001");
        assertThat(payload.title()).isEqualTo("Issue updated");
        assertThat(payload.content()).isEqualTo("New comment");
        assertThat(payload.url()).isEqualTo("https://github.com/org/repo/issues/101");
        assertThat(payload.metadata())
                .containsEntry("event_id", "5001")
                .containsEntry("title", "Issue updated")
                .containsEntry("content", "New comment")
                .containsEntry("url", "https://github.com/org/repo/issues/101")
                .containsEntry("source", "octocat");
    }
}
