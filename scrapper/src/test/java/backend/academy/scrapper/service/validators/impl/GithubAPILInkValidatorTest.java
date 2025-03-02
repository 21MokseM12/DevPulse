package backend.academy.scrapper.service.validators.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class GithubAPILInkValidatorTest {

    private final GithubAPILinkValidator validator = new GithubAPILinkValidator();

    @Test
    void testValidUrls() {
        // Тестируем несколько валидных URL
        assertTrue(validator.isValidLink("https://github.com/21MokseM12/Log-analyzer-Tbank-project"));
        assertTrue(validator.isValidLink("https://github.com/21MokseM12/Balls_Group_Web_Site"));
        assertTrue(validator.isValidLink("https://github.com/21MokseM12/21MokseM12"));
        assertTrue(validator.isValidLink("https://github.com/21MokseM12/Labirinth-Tbank-project"));
        assertTrue(validator.isValidLink("https://github.com/21MokseM12/Gallows-game-Tbank-project"));
    }

    @Test
    void testInvalidUrls() {
        // Тестируем несколько некорректных URL
        assertFalse(validator.isValidLink("htp://github.com/21MokseM12/Balls_Group_Web_Site")); // Ошибка в протоколе
        assertFalse(validator.isValidLink("://github.com/21MokseM12/Balls_Group_Web_Site")); // Неправильный формат
        assertFalse(validator.isValidLink("http://")); // Без домена
        assertFalse(validator.isValidLink("example")); // Без протокола и домена
        assertFalse(validator.isValidLink("https://example.com")); // Без протокола и домена
    }

    @Test
    void testNullAndBlank() {
        // Тестируем null и пустую строку
        assertFalse(validator.isValidLink(null)); // null
        assertFalse(validator.isValidLink("")); // Пустая строка
        assertFalse(validator.isValidLink("   ")); // Строка с пробелами
    }
}
