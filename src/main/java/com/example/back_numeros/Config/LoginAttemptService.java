package com.example.back_numeros.Config;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 8;
    private static final int MAX_TRACKED_ADDRESSES = 10_000;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final Map<String, AttemptWindow> attempts = new LinkedHashMap<>();

    public synchronized boolean isBlocked(String address) {
        Instant now = Instant.now();
        removeExpired(now);
        AttemptWindow window = attempts.get(address);
        return window != null && window.count >= MAX_ATTEMPTS;
    }

    public synchronized void recordFailure(String address) {
        Instant now = Instant.now();
        removeExpired(now);
        AttemptWindow window = attempts.get(address);
        if (window == null) {
            if (attempts.size() >= MAX_TRACKED_ADDRESSES) {
                Iterator<String> oldest = attempts.keySet().iterator();
                if (oldest.hasNext()) {
                    oldest.next();
                    oldest.remove();
                }
            }
            attempts.put(address, new AttemptWindow(now, 1));
            return;
        }
        window.count++;
    }

    public synchronized void clear(String address) {
        attempts.remove(address);
    }

    private void removeExpired(Instant now) {
        attempts.entrySet().removeIf(entry ->
                Duration.between(entry.getValue().startedAt, now).compareTo(WINDOW) >= 0);
    }

    private static final class AttemptWindow {
        private final Instant startedAt;
        private int count;

        private AttemptWindow(Instant startedAt, int count) {
            this.startedAt = startedAt;
            this.count = count;
        }
    }
}
