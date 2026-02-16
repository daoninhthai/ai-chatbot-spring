package com.daoninhthai.aichatbot.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a reusable prompt template with variable placeholders.
 */
@Entity
@Table(name = "prompt_templates")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PromptTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 100)
    private String category;

    @ElementCollection
    @CollectionTable(name = "prompt_template_variables", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "variable_name")
    @Builder.Default
    private List<String> variables = new ArrayList<>();

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private boolean isPublic = false;

    @Column(name = "created_by", length = 255)
    private String createdBy;

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private int usageCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Increment the usage counter.
     */
    public void incrementUsage() {
        this.usageCount++;
    }
}
