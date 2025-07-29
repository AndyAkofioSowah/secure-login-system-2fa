package com.securelogin.util;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {
    private final int MAX_ATTEMPT = 5;
    private final long BLOCK_TIME_MS = TimeUnit.MINUTES.toMillis(15);

    private final ConcurrentHashMap<String, Integer> attempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> blockTimestamps = new ConcurrentHashMap<>();

    public void loginFailed(String ip) {
        int count = attempts.getOrDefault(ip, 0);
        attempts.put(ip, count + 1);

        if (count + 1 >= MAX_ATTEMPT) {
            blockTimestamps.put(ip, System.currentTimeMillis());
        }
    }

    public void loginSucceeded(String ip) {
        attempts.remove(ip);
        blockTimestamps.remove(ip);
    }

    public boolean isBlocked(String ip) {
        if (!blockTimestamps.containsKey(ip)) return false;

        long blockedAt = blockTimestamps.get(ip);
        if (System.currentTimeMillis() - blockedAt > BLOCK_TIME_MS) {
            blockTimestamps.remove(ip);
            attempts.remove(ip);
            return false;
        }

        return true;
    }
}
