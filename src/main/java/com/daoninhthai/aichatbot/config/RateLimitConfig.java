package com.daoninhthai.aichatbot.config;

import com.daoninhthai.aichatbot.exception.RateLimitExceededException;
import com.daoninhthai.aichatbot.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    @Value("${app.rate-limit.requests-per-minute}")
    private int requestsPerMinute;

    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor())
                .addPathPatterns("/api/chat/**");
    }

    private class RateLimitInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                 Object handler) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
                return true; // Let Spring Security handle unauthenticated requests
            }

            String userKey = "user:" + userDetails.getId();
            RateLimitBucket bucket = buckets.computeIfAbsent(userKey, k -> new RateLimitBucket());

            if (!bucket.tryConsume()) {
                long retryAfter = bucket.getRetryAfterSeconds();
                response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
                response.setHeader("X-RateLimit-Remaining", "0");
                response.setHeader("Retry-After", String.valueOf(retryAfter));
                throw new RateLimitExceededException(
                        "Rate limit exceeded. Please try again in " + retryAfter + " seconds.",
                        retryAfter);
            }

            int remaining = bucket.getRemaining();
            response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
            return true;
        }
    }

    private class RateLimitBucket {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis();

        boolean tryConsume() {
            long now = System.currentTimeMillis();
            if (now - windowStart > 60_000) {
                // Reset window
                count.set(0);
                windowStart = now;
            }
            return count.incrementAndGet() <= requestsPerMinute;
        }

        int getRemaining() {
            return Math.max(0, requestsPerMinute - count.get());
        }

        long getRetryAfterSeconds() {
            return Math.max(1, (60_000 - (System.currentTimeMillis() - windowStart)) / 1000);
        }
    }
}
