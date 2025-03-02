package backend.academy.bot.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LinkValidatorTest {

    @Test
    void testValidUrls() {
        // Тестируем несколько валидных URL
        assertTrue(LinkValidator.isValid("https://github.com/21MokseM12/Balls_Group_Web_Site"));
        assertTrue(LinkValidator.isValid("https://github.com/21MokseM12/Log-analyzer-Tbank-project"));
        assertTrue(LinkValidator.isValid("https://github.com/21MokseM12/Labirinth-Tbank-project"));
        assertTrue(LinkValidator.isValid("https://stackoverflow.com/questions/79479386/bable-ignores-config-file-and-fails-in-react-native-npx-react-native-run-ios"));
        assertTrue(LinkValidator.isValid("https://stackoverflow.com/questions/79479385/inconsistency-in-the-constructors-of-stdtuple-when-using-stdany-elements"));
    }

    @Test
    void testInvalidUrls() {
        // Тестируем несколько некорректных URL
        assertFalse(LinkValidator.isValid("htp://github.com/21MokseM12/Balls_Group_Web_Site")); // Ошибка в протоколе
        assertFalse(LinkValidator.isValid("://github.com/21MokseM12/Balls_Group_Web_Site")); // Неправильный формат
        assertFalse(LinkValidator.isValid("http://")); // Без домена
        assertFalse(LinkValidator.isValid("example")); // Без протокола и домена
        assertFalse(LinkValidator.isValid("https://example.com")); // Без протокола и домена
    }

    @Test
    void testNullAndBlank() {
        // Тестируем null и пустую строку
        assertFalse(LinkValidator.isValid(null)); // null
        assertFalse(LinkValidator.isValid("")); // Пустая строка
        assertFalse(LinkValidator.isValid("   ")); // Строка с пробелами
    }
}
