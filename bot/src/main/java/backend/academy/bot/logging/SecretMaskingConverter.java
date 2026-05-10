package backend.academy.bot.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SecretMaskingConverter extends ClassicConverter {

    private static final String MASK = "***";
    private static final Pattern SECRET_PATTERN =
            Pattern.compile("(?i)\\b(password|passwd|secret|token|api[_-]?key)(\\s*[:=]\\s*|\\s+)([^\\s,;]+)");

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (message == null || message.isBlank()) {
            return message;
        }

        Matcher matcher = SECRET_PATTERN.matcher(message);
        StringBuilder masked = new StringBuilder(message.length());
        int lastAppendedIndex = 0;
        while (matcher.find()) {
            masked.append(message, lastAppendedIndex, matcher.start());
            masked.append(matcher.group(1)).append(matcher.group(2)).append(MASK);
            lastAppendedIndex = matcher.end();
        }
        masked.append(message, lastAppendedIndex, message.length());

        return masked.toString();
    }
}
