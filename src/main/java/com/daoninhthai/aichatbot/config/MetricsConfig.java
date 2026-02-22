package com.daoninhthai.aichatbot.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for custom Prometheus metrics related to AI chatbot operations.
 */
@Configuration
public class MetricsConfig {

    @Bean
    public Counter aiChatRequestsTotal(MeterRegistry registry) {
        return Counter.builder("ai_chat_requests_total")
                .description("Total number of AI chat requests")
                .register(registry);
    }

    @Bean
    public Counter aiTokensUsed(MeterRegistry registry) {
        return Counter.builder("ai_tokens_used")
                .description("Total tokens consumed by AI models")
                .tag("model", "default")
                .register(registry);
    }

    @Bean
    public Timer aiResponseTime(MeterRegistry registry) {
        return Timer.builder("ai_response_time")
                .description("AI response latency")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    @Bean
    public Counter aiErrorsTotal(MeterRegistry registry) {
        return Counter.builder("ai_errors_total")
                .description("Total number of AI processing errors")
                .register(registry);
    }
}
