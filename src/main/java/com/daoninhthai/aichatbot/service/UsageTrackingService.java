package com.daoninhthai.aichatbot.service;

import com.daoninhthai.aichatbot.dto.DailyUsage;
import com.daoninhthai.aichatbot.dto.UsageSummary;
import com.daoninhthai.aichatbot.entity.UsageRecord;
import com.daoninhthai.aichatbot.repository.UsageRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for tracking API usage, calculating costs, and checking rate limits.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsageTrackingService {

    private final UsageRecordRepository usageRecordRepository;

    /**
     * Track an API call by recording token usage and cost.
     */
    @Transactional
    public UsageRecord trackApiCall(Long userId, String model, int promptTokens,
                                    int completionTokens, long responseTimeMs) {
        BigDecimal cost = calculateCost(model, promptTokens, completionTokens);

        UsageRecord record = UsageRecord.builder()
                .userId(userId)
                .model(model)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(promptTokens + completionTokens)
                .cost(cost)
                .responseTimeMs(responseTimeMs)
                .timestamp(LocalDateTime.now())
                .build();

        record = usageRecordRepository.save(record);
        log.debug("Usage tracked for user {}: {} tokens, model {}", userId,
                record.getTotalTokens(), model);
        return record;
    }

    /**
     * Get usage records for a user within a date range.
     */
    @Transactional(readOnly = true)
    public List<UsageRecord> getUserUsage(Long userId, LocalDateTime start, LocalDateTime end) {
        return usageRecordRepository.findByUserIdAndTimestampBetween(userId, start, end);
    }

    /**
     * Get daily usage breakdown for the last N days.
     */
    @Transactional(readOnly = true)
    public List<DailyUsage> getDailyUsage(Long userId, int days) {
        LocalDateTime start = LocalDate.now().minusDays(days).atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        List<UsageRecord> records = usageRecordRepository
                .findByUserIdAndTimestampBetween(userId, start, end);

        Map<LocalDate, List<UsageRecord>> byDate = records.stream()
                .collect(Collectors.groupingBy(r -> r.getTimestamp().toLocalDate()));

        List<DailyUsage> dailyUsages = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            List<UsageRecord> dayRecords = byDate.getOrDefault(date, Collections.emptyList());

            String topModel = dayRecords.stream()
                    .collect(Collectors.groupingBy(UsageRecord::getModel, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("none");

            dailyUsages.add(DailyUsage.builder()
                    .date(date)
                    .requestCount(dayRecords.size())
                    .totalTokens(dayRecords.stream().mapToLong(UsageRecord::getTotalTokens).sum())
                    .cost(dayRecords.stream()
                            .map(UsageRecord::getCost)
                            .reduce(BigDecimal.ZERO, BigDecimal::add))
                    .mostUsedModel(topModel)
                    .build());
        }

        return dailyUsages;
    }

    /**
     * Get monthly usage summary.
     */
    @Transactional(readOnly = true)
    public UsageSummary getMonthlyUsage(Long userId) {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        List<UsageRecord> records = usageRecordRepository
                .findByUserIdAndTimestampBetween(userId, monthStart, now);

        Map<String, Long> tokensByModel = records.stream()
                .collect(Collectors.groupingBy(UsageRecord::getModel,
                        Collectors.summingLong(UsageRecord::getTotalTokens)));

        double avgResponseTime = records.stream()
                .filter(r -> r.getResponseTimeMs() != null)
                .mapToLong(UsageRecord::getResponseTimeMs)
                .average()
                .orElse(0.0);

        return UsageSummary.builder()
                .userId(userId)
                .totalRequests(records.size())
                .totalTokens(records.stream().mapToLong(UsageRecord::getTotalTokens).sum())
                .promptTokens(records.stream().mapToLong(UsageRecord::getPromptTokens).sum())
                .completionTokens(records.stream().mapToLong(UsageRecord::getCompletionTokens).sum())
                .totalCost(records.stream()
                        .map(UsageRecord::getCost)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .period("monthly")
                .tokensByModel(tokensByModel)
                .avgResponseTimeMs(avgResponseTime)
                .build();
    }

    /**
     * Check if a user has exceeded their daily rate limit.
     */
    public boolean checkRateLimit(Long userId, int maxRequestsPerDay) {
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        long todayCount = usageRecordRepository.countByUserIdSince(userId, dayStart);
        return todayCount < maxRequestsPerDay;
    }

    /**
     * Calculate cost based on model pricing.
     */
    private BigDecimal calculateCost(String model, int promptTokens, int completionTokens) {
        double promptRate;
        double completionRate;

        switch (model.toLowerCase()) {
            case "gpt-4o" -> {
                promptRate = 0.005;
                completionRate = 0.015;
            }
            case "gpt-4o-mini" -> {
                promptRate = 0.00015;
                completionRate = 0.0006;
            }
            case "claude-3-opus" -> {
                promptRate = 0.015;
                completionRate = 0.075;
            }
            case "claude-3-sonnet" -> {
                promptRate = 0.003;
                completionRate = 0.015;
            }
            default -> {
                promptRate = 0.001;
                completionRate = 0.002;
            }
        }

        double promptCost = (promptTokens / 1000.0) * promptRate;
        double completionCost = (completionTokens / 1000.0) * completionRate;
        return BigDecimal.valueOf(promptCost + completionCost);
    }
}
