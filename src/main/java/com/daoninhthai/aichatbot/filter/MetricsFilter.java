package com.daoninhthai.aichatbot.filter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * Filter that records HTTP request metrics (count and duration)
 * for all incoming API requests.
 */
@Slf4j
@Component
public class MetricsFilter extends OncePerRequestFilter {

    private final Counter requestCounter;
    private final Timer requestTimer;

    public MetricsFilter(MeterRegistry meterRegistry) {
        this.requestCounter = Counter.builder("http_requests_total")
                .description("Total HTTP requests")
                .register(meterRegistry);
        this.requestTimer = Timer.builder("http_request_duration")
                .description("HTTP request duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            requestCounter.increment();
            requestTimer.record(Duration.ofMillis(duration));

            if (log.isTraceEnabled()) {
                log.trace("{} {} - {}ms (status: {})",
                        request.getMethod(), request.getRequestURI(),
                        duration, response.getStatus());
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip metrics for actuator and static resources
        return path.startsWith("/actuator") || path.startsWith("/swagger");
    }
}
