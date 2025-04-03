package backend.academy.scrapper.repository;

import org.springframework.stereotype.Component;
import java.time.Clock;

@Component
public interface ClockProvider {
    Clock getClock();
}
