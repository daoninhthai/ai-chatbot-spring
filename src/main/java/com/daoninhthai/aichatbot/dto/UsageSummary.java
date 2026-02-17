package com.daoninhthai.aichatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Summary of API usage for a user across a given time period.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageSummary {

    private Long userId;
    private int totalRequests;
    private long totalTokens;
    private long promptTokens;
    private long completionTokens;
    private BigDecimal totalCost;
    private String period;
    private Map<String, Long> tokensByModel;
    private double avgResponseTimeMs;
}
