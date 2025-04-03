package backend.academy.scrapper.repository;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.model.Link;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scrapper.bot.connectivity.model.request.AddLinkRequest;
import scrapper.bot.connectivity.model.request.RemoveLinkRequest;

class ClientRepositoryTest {

    static class TestClockProvider implements ClockProvider {

        private Clock clock;

        TestClockProvider(Clock clock) {
            this.clock = clock;
        }

        public void offset(Duration duration) {
            clock = Clock.offset(clock, duration);
        }

        @Override
        public Clock getClock() {
            return clock;
        }
    }

    private ClientRepository repository;

    private TestClockProvider clockProvider;

    @BeforeEach
    void setUp() {
        clockProvider = new TestClockProvider(Clock.systemUTC());
        repository = new ClientRepository(clockProvider);
    }

    @Test
    void testSaveChatAndExistsChat() {
        Long chatId = 1L;
        assertThat(repository.existsChat(chatId)).isFalse();
        repository.saveChat(chatId);
        assertThat(repository.existsChat(chatId)).isTrue();
    }

    @Test
    void testDeleteChat() {
        Long chatId = 2L;
        repository.saveChat(chatId);
        assertThat(repository.existsChat(chatId)).isTrue();
        repository.deleteChat(chatId);
        assertThat(repository.existsChat(chatId)).isFalse();
    }

    @Test
    void testSaveLink() {
        Long chatId = 3L;
        repository.saveChat(chatId);
        AddLinkRequest request =
                new AddLinkRequest(URI.create("https://example.com"), List.of("tag1"), List.of("filter1"));
        Link link = repository.saveLink(chatId, request);

        assertThat(repository.findAllLinks(chatId)).containsExactly(link);
        assertThat(link.url()).isEqualTo(URI.create("https://example.com"));
    }

    @Test
    void testDeleteLink() {
        Long chatId = 4L;
        repository.saveChat(chatId);
        AddLinkRequest request =
                new AddLinkRequest(URI.create("https://example.com"), List.of("tag1"), List.of("filter1"));
        repository.saveLink(chatId, request);

        RemoveLinkRequest removeRequest = new RemoveLinkRequest(URI.create("https://example.com"));
        Link removedLink = repository.deleteLink(chatId, removeRequest);

        assertThat(removedLink.url()).isEqualTo(URI.create("https://example.com"));
        assertThat(repository.findAllLinks(chatId)).isEmpty();
    }

    @Test
    void testFindAllLinks() {
        Long chatId = 5L;
        repository.saveChat(chatId);
        AddLinkRequest request1 =
                new AddLinkRequest(URI.create("https://example.com"), List.of("tag1"), List.of("filter1"));
        AddLinkRequest request2 =
                new AddLinkRequest(URI.create("https://example.org"), List.of("tag2"), List.of("filter2"));
        repository.saveLink(chatId, request1);
        repository.saveLink(chatId, request2);

        List<Link> links = repository.findAllLinks(chatId);
        assertThat(links).hasSize(2);
    }

    @Test
    void testFindAllLinksByForceCheckDelay() throws InterruptedException {
        Long chatId = 6L;
        repository.saveChat(chatId);
        AddLinkRequest request =
                new AddLinkRequest(URI.create("https://example.com"), List.of("tag1"), List.of("filter1"));
        repository.saveLink(chatId, request);

        clockProvider.offset(Duration.ofMillis(2));

        Map<Long, List<Link>> result = repository.findAllLinksByForceCheckDelay(Duration.ofMillis(1));
        assertThat(result).containsKey(chatId);
        assertThat(result.get(chatId)).hasSize(1);
    }
}
