package com.example.back_numeros.Config;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_MILLIS = 15 * 60 * 1000L; // 15 minutos

    private record AttemptRecord(int count, long lastAttemptTime) {}

    private final Map<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    public void recordFailure(String key) {
        long now = Instant.now().toEpochMilli();
        attempts.compute(key, (k, existing) -> {
            if (existing == null || (now - existing.lastAttemptTime()) > BLOCK_DURATION_MILLIS) {
                return new AttemptRecord(1, now);
            }
            return new AttemptRecord(existing.count() + 1, now);
        });
    }

    public void clear(String key) {
        attempts.remove(key);
    }

    public boolean isBlocked(String key) {
        AttemptRecord record = attempts.get(key);
        if (record == null) {
            return false;
        }
        long now = Instant.now().toEpochMilli();
        if ((now - record.lastAttemptTime()) > BLOCK_DURATION_MILLIS) {
            attempts.remove(key);
            return false;
        }
        return record.count() >= MAX_ATTEMPTS;
    }
}