package backend.academy.scrapper.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.service.updaters.LinkUpdater;
import backend.academy.scrapper.service.updaters.impl.GithubUpdaterService;
import backend.academy.scrapper.service.updaters.impl.StackOverflowUpdaterService;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import scrapper.bot.connectivity.enums.LinkUpdaterType;

@ExtendWith(MockitoExtension.class)
public class LinkUpdaterServiceFactoryTest {

    private static LinkUpdaterServiceFactory factory;

    private static final LinkUpdater github = mock(GithubUpdaterService.class);

    private static final LinkUpdater stackOverflow = mock(StackOverflowUpdaterService.class);

    @BeforeAll
    public static void setUp() {
        when(github.getType()).thenReturn(LinkUpdaterType.GITHUB);
        when(stackOverflow.getType()).thenReturn(LinkUpdaterType.STACK_OVERFLOW);
        List<LinkUpdater> updaters = List.of(github, stackOverflow);
        factory = new LinkUpdaterServiceFactory(updaters);
    }

    @Test
    public void whenGivenAGithubLink_thenReturnGithubUpdaterService() {
        String link = "https://github.com/21MokseM12/Balls_Group_Web_Site";
        LinkUpdater linkUpdater = factory.get(URI.create(link));
        assertEquals(github, linkUpdater);
    }

    @Test
    public void whenGivenStackOverflowLink_thenReturnStackOverflowUpdaterService() {
        String link =
                "https://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes";
        LinkUpdater linkUpdater = factory.get(URI.create(link));
        assertEquals(stackOverflow, linkUpdater);
    }
}
