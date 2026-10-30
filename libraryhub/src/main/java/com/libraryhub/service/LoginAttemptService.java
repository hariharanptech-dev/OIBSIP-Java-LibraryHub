package com.libraryhub.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

import com.libraryhub.config.AppProperties;

/**
 * In-memory sign-in rate limiter. After the configured number of failed attempts for an email
 * (default 5) that email is locked for the configured time (default 60 seconds). Emails are
 * tracked whether or not an account exists, so the lock reveals nothing about who is registered.
 */
@Service
public class LoginAttemptService {

    private static final Duration FAILURE_WINDOW = Duration.ofMinutes(15);
    private static final int CLEANUP_THRESHOLD = 5_000;
    private static final int KEY_LIMIT = 254;

    private final int maxFailures;
    private final Duration lockDuration;
    private final Clock clock;
    private final ConcurrentMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public LoginAttemptService(AppProperties properties, Clock clock) {
        this.maxFailures = properties.getLogin().getMaxFailedAttempts();
        this.lockDuration = Duration.ofSeconds(properties.getLogin().getLockSeconds());
        this.clock = clock;
    }

    public boolean isLocked(String email) {
        return secondsRemaining(email) > 0;
    }

    /** Seconds until the lock ends, rounded up. Zero when the email is not locked. */
    public long secondsRemaining(String email) {
        Attempt attempt = attempts.get(key(email));
        if (attempt == null || attempt.lockedUntil == null) {
            return 0;
        }
        long millis = Duration.between(clock.instant(), attempt.lockedUntil).toMillis();
        return millis <= 0 ? 0 : (millis + 999) / 1000;
    }

    public void recordFailure(String email) {
        String key = key(email);
        if (key.isEmpty()) {
            return;
        }
        Instant now = clock.instant();
        if (attempts.size() > CLEANUP_THRESHOLD) {
            purge(now);
        }
        attempts.compute(key, (k, current) -> {
            Attempt attempt = (current == null || current.isStale(now)) ? new Attempt() : current;
            attempt.failures++;
            attempt.lastFailure = now;
            if (attempt.failures >= maxFailures) {
                attempt.lockedUntil = now.plus(lockDuration);
            }
            return attempt;
        });
    }

    public void recordSuccess(String email) {
        attempts.remove(key(email));
    }

    private void purge(Instant now) {
        attempts.entrySet().removeIf(entry -> entry.getValue().isStale(now));
        if (attempts.size() > CLEANUP_THRESHOLD * 2) {
            attempts.clear();
        }
    }

    private static String key(String email) {
        if (email == null) {
            return "";
        }
        String key = email.trim().toLowerCase(Locale.ROOT);
        return key.length() > KEY_LIMIT ? key.substring(0, KEY_LIMIT) : key;
    }

    private static final class Attempt {
        private volatile int failures;
        private volatile Instant lastFailure;
        private volatile Instant lockedUntil;

        /** A finished lock, or failures older than the window, start counting from zero again. */
        boolean isStale(Instant now) {
            if (lockedUntil != null) {
                return !now.isBefore(lockedUntil);
            }
            return lastFailure != null && now.isAfter(lastFailure.plus(FAILURE_WINDOW));
        }
    }
}
