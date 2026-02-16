package com.daoninhthai.aichatbot.dto.response;

import com.daoninhthai.aichatbot.entity.PromptTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for prompt template data.
 */
@Data
@Builder
@AllArgsConstructor
public class PromptTemplateResponse {

    private Long id;
    private String name;
    private String description;
    private String content;
    private String category;
    private List<String> variables;
    private boolean isPublic;
    private String createdBy;
    private int usageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PromptTemplateResponse from(PromptTemplate template) {
        return PromptTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .content(template.getContent())
                .category(template.getCategory())
                .variables(template.getVariables())
                .isPublic(template.isPublic())
                .createdBy(template.getCreatedBy())
                .usageCount(template.getUsageCount())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
