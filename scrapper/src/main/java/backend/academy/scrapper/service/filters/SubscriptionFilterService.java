package backend.academy.scrapper.service.filters;

import backend.academy.scrapper.model.LinkUpdateDTO;
import java.util.Locale;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SubscriptionFilterService {

    public boolean matches(LinkUpdateDTO update, Set<String> rawFilters) {
        if (rawFilters == null || rawFilters.isEmpty()) {
            return true;
        }

        for (String rawFilter : rawFilters) {
            FilterRule rule = parseRule(rawFilter);
            if (rule == null) {
                continue;
            }
            if (!matchesRule(update, rule)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesRule(LinkUpdateDTO update, FilterRule rule) {
        return switch (rule.key()) {
            case "author" -> equalsIgnoreCase(update.updateOwner(), rule.value());
            case "type" -> update.type() != null
                    && equalsIgnoreCase(update.type().name(), normalizeType(rule.value()));
            case "label" -> update.labels().stream().anyMatch(label -> equalsIgnoreCase(label, rule.value()));
            default -> {
                log.warn("Неизвестный фильтр подписки '{}', правило будет пропущено", rule.key());
                yield true;
            }
        };
    }

    private FilterRule parseRule(String rawFilter) {
        if (rawFilter == null || rawFilter.isBlank()) {
            return null;
        }
        int delimiter = rawFilter.indexOf(':');
        if (delimiter <= 0 || delimiter == rawFilter.length() - 1) {
            log.warn("Некорректный формат фильтра '{}', ожидается key:value", rawFilter);
            return null;
        }
        String key = rawFilter.substring(0, delimiter).trim().toLowerCase(Locale.ROOT);
        String value = rawFilter.substring(delimiter + 1).trim();
        if (value.isBlank()) {
            log.warn("Пустое значение фильтра '{}', правило будет пропущено", rawFilter);
            return null;
        }
        return new FilterRule(key, value);
    }

    private String normalizeType(String value) {
        return value.trim().replace('-', '_').toUpperCase(Locale.ROOT);
    }

    private boolean equalsIgnoreCase(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }

    private record FilterRule(String key, String value) {}
}
