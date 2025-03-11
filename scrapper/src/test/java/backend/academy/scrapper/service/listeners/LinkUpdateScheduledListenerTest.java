package backend.academy.scrapper.service.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.config.ScrapperConfig;
import backend.academy.scrapper.factory.LinkUpdaterServiceFactory;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.LinkUpdateDTO;
import backend.academy.scrapper.repository.ClientRepository;
import backend.academy.scrapper.service.notifications.ScrapperNotificationManager;
import backend.academy.scrapper.service.updaters.LinkUpdater;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class LinkUpdateScheduledListenerTest {

    @Autowired
    private ScrapperConfig scrapperConfig;

    @Autowired
    private LinkUpdateScheduledListener listener;

    @MockitoBean
    private ClientRepository clientRepository;

    @MockitoBean
    private LinkUpdaterServiceFactory updaterFactory;

    @MockitoBean
    private ScrapperNotificationManager notificationManager;

    @MockitoBean
    @Qualifier("githubUpdaterService")
    private LinkUpdater updater;

    @Test
    void testListenUpdates_NoUpdates_NoNotification() {
        when(clientRepository.findAllLinksByForceCheckDelay(any())).thenReturn(Collections.emptyMap());
        listener.listenUpdates();
        verify(notificationManager, never()).notify(any());
    }

    @Test
    void testListenUpdates_WithUpdates_NotificationSent_Scheduling() throws InterruptedException {
        Long chatId = 1L;
        URI linkUri = URI.create("https://example.com");
        Link link = new Link(1L, linkUri, List.of("tag1"), List.of("filter1"), OffsetDateTime.now());
        List<Link> links = List.of(link);
        Map<Long, List<Link>> linksMap = Map.of(chatId, links);

        when(clientRepository.findAllLinksByForceCheckDelay(any())).thenReturn(linksMap);
        when(updaterFactory.get(linkUri)).thenReturn(updater);
        when(updater.getUpdates(linkUri))
                .thenReturn(Optional.of(List.of(new LinkUpdateDTO(1L, URI.create("update"), "description"))));

        Thread.sleep(16000);

        ArgumentCaptor<Map<URI, List<Long>>> captor = ArgumentCaptor.forClass(Map.class);
        verify(notificationManager).notify(captor.capture());

        Map<URI, List<Long>> capturedValue = captor.getValue();
        assertThat(capturedValue).containsKey(linkUri);
        assertThat(capturedValue.get(linkUri)).contains(chatId);
    }
}
