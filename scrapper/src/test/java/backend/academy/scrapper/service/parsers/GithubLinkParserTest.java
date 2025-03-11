package backend.academy.scrapper.service.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class GithubLinkParserTest {

    private final GithubLinkParser githubLinkParser = new GithubLinkParser();

    @Test
    void testValidUrls() {

        assertEquals(
                "Log-analyzer-Tbank-project",
                githubLinkParser.parseRepo("https://github.com/21MokseM12/Log-analyzer-Tbank-project"));
        assertEquals(
                "Balls_Group_Web_Site",
                githubLinkParser.parseRepo("https://github.com/21MokseM12/Balls_Group_Web_Site"));
        assertEquals("21MokseM12", githubLinkParser.parseUsername("https://github.com/21MokseM12/21MokseM12"));
        assertEquals(
                "21MokseM12", githubLinkParser.parseUsername("https://github.com/21MokseM12/Labirinth-Tbank-project"));
    }
}
