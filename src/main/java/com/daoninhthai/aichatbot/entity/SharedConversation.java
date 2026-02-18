package com.daoninhthai.aichatbot.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity representing a shared conversation link with expiration and view tracking.
 */
@Entity
@Table(name = "shared_conversations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SharedConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "share_token", unique = true, nullable = false, length = 64)
    private String shareToken;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private int viewCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Increment the view count when the shared link is accessed.
     */
    public void incrementViewCount() {
        this.viewCount++;
    }
}
