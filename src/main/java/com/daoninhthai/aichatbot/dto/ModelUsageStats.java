package com.daoninhthai.aichatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO containing usage statistics per AI model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelUsageStats {

    private String modelName;
    private long requestCount;
    private long totalTokens;
    private double avgLatencyMs;
    private long errorCount;
}
