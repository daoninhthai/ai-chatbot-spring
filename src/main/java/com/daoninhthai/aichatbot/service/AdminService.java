package com.daoninhthai.aichatbot.service;

import com.daoninhthai.aichatbot.dto.ModelUsageStats;
import com.daoninhthai.aichatbot.dto.SystemStats;
import com.daoninhthai.aichatbot.dto.UserStatsResponse;
import com.daoninhthai.aichatbot.entity.User;
import com.daoninhthai.aichatbot.repository.ConversationRepository;
import com.daoninhthai.aichatbot.repository.MessageRepository;
import com.daoninhthai.aichatbot.repository.UsageRecordRepository;
import com.daoninhthai.aichatbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for admin dashboard operations including system stats and user analytics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UsageRecordRepository usageRecordRepository;

    /**
     * Get system-wide statistics.
     */
    @Transactional(readOnly = true)
    public SystemStats getSystemStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Long tokensToday = usageRecordRepository.sumAllTokensSince(todayStart);
        long requestsToday = usageRecordRepository.countByTimestampAfter(todayStart);

        return SystemStats.builder()
                .totalUsers(userRepository.count())
                .totalConversations(conversationRepository.count())
                .totalMessages(messageRepository.count())
                .tokensUsedToday(tokensToday != null ? tokensToday : 0)
                .activeUsersToday(requestsToday)
                .avgResponseTimeMs(0.0)
                .build();
    }

    /**
     * Get per-user statistics list.
     */
    @Transactional(readOnly = true)
    public List<UserStatsResponse> getUserStats() {
        List<User> users = userRepository.findAll();
        List<UserStatsResponse> stats = new ArrayList<>();

        for (User user : users) {
            long convCount = conversationRepository.countByUser(user);

            stats.add(UserStatsResponse.builder()
                    .userId(user.getId())
                    .username(user.getDisplayName())
                    .conversationCount(convCount)
                    .messageCount(0)
                    .lastActive(user.getUpdatedAt())
                    .build());
        }

        return stats;
    }

    /**
     * Get usage breakdown by model.
     */
    @Transactional(readOnly = true)
    public List<ModelUsageStats> getModelUsageBreakdown() {
        List<ModelUsageStats> result = new ArrayList<>();

        // Aggregate usage records by model across all users
        List<User> users = userRepository.findAll();
        java.util.Map<String, long[]> modelStats = new java.util.HashMap<>();

        for (User user : users) {
            List<Object[]> usageByModel = usageRecordRepository.getUsageByModel(user.getId());
            for (Object[] row : usageByModel) {
                String model = (String) row[0];
                long count = (Long) row[1];
                long tokens = (Long) row[2];
                modelStats.merge(model, new long[]{count, tokens},
                        (a, b) -> new long[]{a[0] + b[0], a[1] + b[1]});
            }
        }

        modelStats.forEach((model, stats) -> {
            result.add(ModelUsageStats.builder()
                    .modelName(model)
                    .requestCount(stats[0])
                    .totalTokens(stats[1])
                    .avgLatencyMs(0.0)
                    .errorCount(0)
                    .build());
        });

        return result;
    }

    /**
     * Get recent errors (placeholder - returns empty list for now).
     */
    public List<String> getRecentErrors() {
        log.debug("Fetching recent errors");
        return new ArrayList<>();
    }
}
