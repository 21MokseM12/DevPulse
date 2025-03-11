package backend.academy.scrapper.service.validators.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class StackOverflowAPILInkValidatorTest {

    private final StackOverflowAPILinkValidator validator = new StackOverflowAPILinkValidator();

    @Test
    void testValidUrls() {
        // Тестируем несколько валидных URL
        assertTrue(
                validator.isValidLink(
                        "https://stackoverflow.com/questions/79479661/i-set-up-a-due-date-reminder-in-sharepoint-ms-lists-through-power-automate-th"));
        assertTrue(
                validator.isValidLink(
                        "https://stackoverflow.com/questions/79479661/i-set-up-a-due-date-reminder-in-sharepoint-ms-lists-through-power-automate-th"));
        assertTrue(validator.isValidLink("https://stackoverflow.com/questions/79479642/nats-cannot-elect-leader"));
        assertTrue(
                validator.isValidLink(
                        "https://stackoverflow.com/questions/79479386/bable-ignores-config-file-and-fails-in-react-native-npx-react-native-run-ios"));
        assertTrue(
                validator.isValidLink(
                        "https://stackoverflow.com/questions/79479385/inconsistency-in-the-constructors-of-stdtuple-when-using-stdany-elements"));
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
