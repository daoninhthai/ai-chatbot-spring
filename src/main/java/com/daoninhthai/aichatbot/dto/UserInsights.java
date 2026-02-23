package com.daoninhthai.aichatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO containing aggregated insights across all conversations for a user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInsights {

    private long totalConversations;
    private long totalMessages;
    private double avgMessagesPerConversation;
    private int mostActiveHour;
    private List<String> topTopics;
}
