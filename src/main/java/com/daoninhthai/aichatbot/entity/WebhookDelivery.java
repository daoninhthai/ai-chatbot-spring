package com.daoninhthai.aichatbot.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity tracking individual webhook delivery attempts and their results.
 */
@Entity
@Table(name = "webhook_deliveries")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WebhookDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "webhook_config_id", nullable = false)
    private Long webhookConfigId;

    @Column(nullable = false, length = 100)
    private String event;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(nullable = false)
    @Builder.Default
    private int attempts = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean success = false;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @PrePersist
    protected void onCreate() {
        if (deliveredAt == null) {
            deliveredAt = LocalDateTime.now();
        }
    }
}
