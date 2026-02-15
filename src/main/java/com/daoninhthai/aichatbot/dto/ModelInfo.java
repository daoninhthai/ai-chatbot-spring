package com.daoninhthai.aichatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents metadata about an AI model available in the system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelInfo {

    private String provider;
    private String modelName;
    private String description;
    private int maxTokens;
    private double costPer1kTokens;
    private boolean available;

    public String getModelId() {
        return provider + ":" + modelName;
    }
}
