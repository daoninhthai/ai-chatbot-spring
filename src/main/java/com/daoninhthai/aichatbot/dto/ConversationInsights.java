package com.daoninhthai.aichatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO containing AI-powered insights for a single conversation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationInsights {

    private String sentiment;
    private List<String> topics;
    private int messageCount;
    private double avgResponseLength;
    private double complexityScore;
}
