package com.daoninhthai.aichatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO containing system-wide statistics for the admin dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStats {

    private long totalUsers;
    private long totalConversations;
    private long totalMessages;
    private long tokensUsedToday;
    private long activeUsersToday;
    private double avgResponseTimeMs;
}
