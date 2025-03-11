package backend.academy.scrapper.service.parsers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class StackOverflowLinkParserTest {

    private final StackOverflowLinkParser stackOverflowLinkParser = new StackOverflowLinkParser();

    @Test
    void testValidUrls() {
        assertEquals(
                79479661,
                stackOverflowLinkParser.parseQuestionId(
                        "https://stackoverflow.com/questions/79479661/i-set-up-a-due-date-reminder-in-sharepoint-ms-lists-through-power-automate-th"));
        assertEquals(
                79479642,
                stackOverflowLinkParser.parseQuestionId(
                        "https://stackoverflow.com/questions/79479642/nats-cannot-elect-leader"));
    }
}
