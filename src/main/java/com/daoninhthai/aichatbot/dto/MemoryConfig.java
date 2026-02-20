package com.daoninhthai.aichatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration DTO for conversation memory and context window settings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryConfig {

    @Builder.Default
    private int maxContextTokens = 4096;

    @Builder.Default
    private int summarizationThreshold = 50;

    @Builder.Default
    private int retainRecentCount = 10;
}
