package com.daoninhthai.aichatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Daily usage breakdown for charts and analytics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyUsage {

    private LocalDate date;
    private int requestCount;
    private long totalTokens;
    private BigDecimal cost;
    private String mostUsedModel;
}
