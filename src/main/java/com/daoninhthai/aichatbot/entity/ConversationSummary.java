package com.daoninhthai.aichatbot.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity storing AI-generated summaries of older conversation messages
 * to manage context window size while preserving conversation history.
 */
@Entity
@Table(name = "conversation_summaries")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ConversationSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "summary_text", nullable = false, columnDefinition = "TEXT")
    private String summaryText;

    @Column(name = "from_message_index", nullable = false)
    private int fromMessageIndex;

    @Column(name = "to_message_index", nullable = false)
    private int toMessageIndex;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
