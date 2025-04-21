package backend.academy.scrapper.utils;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClockProviderImpl implements ClockProvider {

    private final Clock clock;

    @Autowired
    public ClockProviderImpl(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Clock getClock() {
        return clock;
    }
}
