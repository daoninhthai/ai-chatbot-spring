package com.daoninhthai.aichatbot.interceptor;

import com.daoninhthai.aichatbot.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limit interceptor that enforces per-user daily request limits using an in-memory counter.
 * Supports different limits per tier (free vs pro users).
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int FREE_TIER_DAILY_LIMIT = 50;
    private static final int PRO_TIER_DAILY_LIMIT = 500;

    private final Map<String, DailyBucket> dailyBuckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null) {
            return true; // Let authentication filters handle this
        }

        String tier = request.getHeader("X-User-Tier");
        int limit = "pro".equalsIgnoreCase(tier) ? PRO_TIER_DAILY_LIMIT : FREE_TIER_DAILY_LIMIT;

        String bucketKey = userId + ":" + java.time.LocalDate.now();
        DailyBucket bucket = dailyBuckets.computeIfAbsent(bucketKey, k -> new DailyBucket());

        if (!bucket.tryConsume(limit)) {
            log.warn("Rate limit exceeded for user {} (tier: {}, limit: {})", userId, tier, limit);
            response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("X-RateLimit-Reset", getEndOfDayEpoch());
            throw new RateLimitExceededException(
                    "Daily rate limit of " + limit + " requests exceeded. Upgrade to Pro for higher limits.",
                    getSecondsUntilMidnight());
        }

        int remaining = limit - bucket.getCount();
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, remaining)));
        return true;
    }

    private String getEndOfDayEpoch() {
        return String.valueOf(java.time.LocalDate.now()
                .plusDays(1)
                .atStartOfDay()
                .toEpochSecond(java.time.ZoneOffset.UTC));
    }

    private long getSecondsUntilMidnight() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return java.time.Duration.between(now, midnight).getSeconds();
    }

    /**
     * Periodically clean up old buckets (called manually or by scheduler).
     */
    public void cleanupOldBuckets() {
        String todayPrefix = java.time.LocalDate.now().toString();
        dailyBuckets.entrySet().removeIf(entry -> !entry.getKey().contains(todayPrefix));
    }

    private static class DailyBucket {
        private final AtomicInteger count = new AtomicInteger(0);

        boolean tryConsume(int limit) {
            return count.incrementAndGet() <= limit;
        }

        int getCount() {
            return count.get();
        }
    }
}
