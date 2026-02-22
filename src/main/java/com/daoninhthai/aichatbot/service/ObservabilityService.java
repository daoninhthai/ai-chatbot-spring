package com.daoninhthai.aichatbot.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Service for recording observability metrics for AI chatbot operations.
 * Integrates with Micrometer/Prometheus for real-time monitoring.
 */
@Slf4j
@Service
public class ObservabilityService {

    private final Counter chatRequestsCounter;
    private final Counter errorsCounter;
    private final Timer responseTimer;
    private final MeterRegistry meterRegistry;

    public ObservabilityService(Counter aiChatRequestsTotal,
                                Counter aiErrorsTotal,
                                Timer aiResponseTime,
                                MeterRegistry meterRegistry) {
        this.chatRequestsCounter = aiChatRequestsTotal;
        this.errorsCounter = aiErrorsTotal;
        this.responseTimer = aiResponseTime;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Record a chat request.
     */
    public void recordChatRequest() {
        chatRequestsCounter.increment();
        log.debug("Chat request recorded, total: {}", chatRequestsCounter.count());
    }

    /**
     * Record token usage for a specific model.
     */
    public void recordTokenUsage(String model, long tokens) {
        Counter.builder("ai_tokens_used")
                .description("Tokens consumed by model")
                .tag("model", model)
                .register(meterRegistry)
                .increment(tokens);
        log.debug("Token usage recorded: {} tokens for model {}", tokens, model);
    }

    /**
     * Record an error during AI processing.
     */
    public void recordError() {
        errorsCounter.increment();
        log.debug("Error recorded, total: {}", errorsCounter.count());
    }

    /**
     * Record the latency of an AI response.
     */
    public void recordLatency(long durationMs) {
        responseTimer.record(Duration.ofMillis(durationMs));
        log.debug("Latency recorded: {}ms", durationMs);
    }
}
