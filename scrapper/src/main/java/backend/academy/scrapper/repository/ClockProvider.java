package backend.academy.scrapper.repository;

import java.time.Clock;

public interface ClockProvider {
    Clock getClock();
}
