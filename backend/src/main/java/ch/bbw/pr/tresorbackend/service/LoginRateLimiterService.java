package ch.bbw.pr.tresorbackend.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginRateLimiterService {

    private static final int maxAttempts = 5;
    private static final long windowMS = 60_000;

    private final Map<String, List<Long>> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String ip) {
        long now = System.currentTimeMillis();
        attempts.compute(ip, (key, timestamps) -> {
            if (timestamps == null) timestamps = new ArrayList<>();
            timestamps.removeIf(t -> now - t > windowMS);
            return timestamps;
        });
        return attempts.get(ip).size() >= maxAttempts;
    }

    public void recordAttempt(String ip) {
        long now = System.currentTimeMillis();
        attempts.compute(ip, (key, timestamps) -> {
            if (timestamps == null) timestamps = new ArrayList<>();
            timestamps.removeIf(t -> now - t > windowMS);
            timestamps.add(now);
            return timestamps;
        });
    }
}
