package io.github.raissaamaral.mapadevagas.user;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);
    private static final int CLEANUP_THRESHOLD = 10_000;

    private final Map<String, FailedAttempts> attemptsByEmail = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public boolean isBlocked(String email) {
        FailedAttempts attempts = attemptsByEmail.get(email);
        if (attempts == null) {
            return false;
        }
        if (attempts.isExpired(clock.instant())) {
            attemptsByEmail.remove(email);
            return false;
        }
        return attempts.count() >= MAX_FAILED_ATTEMPTS;
    }

    public void recordFailure(String email) {
        Instant now = clock.instant();
        removeExpiredEntriesIfTooMany(now);

        attemptsByEmail.compute(email, (key, current) -> {
            if (current == null || current.isExpired(now)) {
                return new FailedAttempts(1, now);
            }
            return new FailedAttempts(current.count() + 1, now);
        });
    }

    public void recordSuccess(String email) {
        attemptsByEmail.remove(email);
    }

    // Prevents the map from growing without limit if many different
    // emails are tried (e.g. an attacker generating random addresses)
    private void removeExpiredEntriesIfTooMany(Instant now) {
        if (attemptsByEmail.size() > CLEANUP_THRESHOLD) {
            attemptsByEmail.values().removeIf(attempts -> attempts.isExpired(now));
        }
    }

    private record FailedAttempts(int count, Instant lastFailureAt) {
        boolean isExpired(Instant now) {
            return now.isAfter(lastFailureAt.plus(BLOCK_DURATION));
        }
    }
}