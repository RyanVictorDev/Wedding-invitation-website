package com.wedding.backend.common;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final long FIVE_SECONDS_MILLIS = 5_000L;

    private final Map<String, Long> lastRequestByIp = new ConcurrentHashMap<>();

    /**
     * Returns true if the request is allowed for the given IP based on a 5s window.
     */
    public boolean isAllowed(String ipAddress) {
        long now = System.currentTimeMillis();
        Long last = lastRequestByIp.get(ipAddress);

        if (last != null && now - last < FIVE_SECONDS_MILLIS) {
            return false;
        }

        lastRequestByIp.put(ipAddress, now);
        return true;
    }
}

