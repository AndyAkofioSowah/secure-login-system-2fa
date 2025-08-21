package com.securelogin.util;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PasswordResetRateLimiter {
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 15 * 60 * 1000L; // 15 min

    private final Map<String, Deque<Long>> attempts = new ConcurrentHashMap<>();

    public boolean allow(String key) {
        long now = System.currentTimeMillis();
        Deque<Long> q = attempts.computeIfAbsent(key, k -> new ArrayDeque<>());
        while (!q.isEmpty() && now - q.peekFirst() > WINDOW_MS) q.pollFirst();
        if (q.size() >= MAX_ATTEMPTS) return false;
        q.addLast(now);
        return true;
    }
}
