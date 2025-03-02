package backend.academy.bot.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LinkValidatorTest {

    @Test
    void testValidUrls() {
        // Тестируем несколько валидных URL
        assertTrue(LinkValidator.isValid("https://www.example.com"));
        assertTrue(LinkValidator.isValid("http://example.com"));
        assertTrue(LinkValidator.isValid("www.example.com"));
        assertTrue(LinkValidator.isValid("https://example.com:8080/path/to/resource"));
        assertTrue(LinkValidator.isValid("example.com/path/to/resource"));
    }

    @Test
    void testInvalidUrls() {
        // Тестируем несколько некорректных URL
        assertFalse(LinkValidator.isValid("htp://example.com")); // Ошибка в протоколе
        assertFalse(LinkValidator.isValid("://example.com")); // Неправильный формат
        assertFalse(LinkValidator.isValid("http://")); // Без домена
        assertFalse(LinkValidator.isValid("example")); // Без протокола и домена
    }

    @Test
    void testNullAndBlank() {
        // Тестируем null и пустую строку
        assertFalse(LinkValidator.isValid(null)); // null
        assertFalse(LinkValidator.isValid("")); // Пустая строка
        assertFalse(LinkValidator.isValid("   ")); // Строка с пробелами
    }
}
