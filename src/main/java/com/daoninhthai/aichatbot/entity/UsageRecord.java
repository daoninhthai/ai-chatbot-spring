package com.daoninhthai.aichatbot.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity tracking API usage per request for billing and analytics.
 */
@Entity
@Table(name = "usage_records", indexes = {
    @Index(name = "idx_usage_user_timestamp", columnList = "userId, timestamp"),
    @Index(name = "idx_usage_timestamp", columnList = "timestamp")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UsageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "prompt_tokens", nullable = false)
    @Builder.Default
    private int promptTokens = 0;

    @Column(name = "completion_tokens", nullable = false)
    @Builder.Default
    private int completionTokens = 0;

    @Column(name = "total_tokens", nullable = false)
    @Builder.Default
    private int totalTokens = 0;

    @Column(precision = 10, scale = 6)
    @Builder.Default
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "request_type", length = 50)
    @Builder.Default
    private String requestType = "chat";

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        totalTokens = promptTokens + completionTokens;
    }
}
