package backend.academy.scrapper.utils;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClockProviderImpl implements ClockProvider {

    private final Clock clock;

    @Override
    public Clock getClock() {
        return clock;
    }
}
