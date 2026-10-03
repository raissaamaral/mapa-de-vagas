package io.github.raissaamaral.mapadevagas.user;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginAttemptServiceTest {

    private static final String EMAIL = "rai@example.com";
    private static final Instant START = Instant.parse("2026-10-04T12:00:00Z");

    private final Clock clock = mock(Clock.class);
    private final LoginAttemptService service = new LoginAttemptService(clock);

    @Test
    void shouldNotBlockBeforeFiveFailures() {
        when(clock.instant()).thenReturn(START);

        recordFailures(4);

        assertFalse(service.isBlocked(EMAIL));
    }

    @Test
    void shouldBlockAfterFiveFailures() {
        when(clock.instant()).thenReturn(START);

        recordFailures(5);

        assertTrue(service.isBlocked(EMAIL));
    }

    @Test
    void shouldUnblockFifteenMinutesAfterLastFailure() {
        when(clock.instant()).thenReturn(START);
        recordFailures(5);

        when(clock.instant()).thenReturn(START.plus(Duration.ofMinutes(16)));

        assertFalse(service.isBlocked(EMAIL));
    }

    @Test
    void successShouldResetTheCounter() {
        when(clock.instant()).thenReturn(START);

        recordFailures(4);
        service.recordSuccess(EMAIL);
        recordFailures(4);

        assertFalse(service.isBlocked(EMAIL));
    }

    private void recordFailures(int times) {
        for (int i = 0; i < times; i++) {
            service.recordFailure(EMAIL);
        }
    }
}