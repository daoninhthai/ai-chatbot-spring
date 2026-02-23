package com.daoninhthai.aichatbot.service;

import com.daoninhthai.aichatbot.dto.MemoryConfig;
import com.daoninhthai.aichatbot.entity.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing the AI context window size.
 * Handles token estimation, message selection, and truncation to stay
 * within the model's context window limits.
 */
@Slf4j
@Service
public class ContextWindowManager {

    private static final double TOKENS_PER_WORD = 1.3;

    /**
     * Estimate the number of tokens in a text using a simple word count heuristic.
     * Approximation: ~1.3 tokens per word for English text.
     */
    public int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        int wordCount = text.trim().split("\\s+").length;
        return (int) Math.ceil(wordCount * TOKENS_PER_WORD);
    }

    /**
     * Truncate text to fit within a maximum token count.
     */
    public String truncateToFit(String text, int maxTokens) {
        if (text == null) return null;

        int estimatedTokens = estimateTokens(text);
        if (estimatedTokens <= maxTokens) {
            return text;
        }

        // Approximate character limit based on average token size (~4 chars per token)
        int maxChars = maxTokens * 4;
        if (text.length() <= maxChars) {
            return text;
        }

        String truncated = text.substring(0, maxChars);
        // Try to truncate at a word boundary
        int lastSpace = truncated.lastIndexOf(' ');
        if (lastSpace > maxChars * 0.8) {
            truncated = truncated.substring(0, lastSpace);
        }

        log.debug("Truncated text from {} to {} estimated tokens", estimatedTokens, estimateTokens(truncated));
        return truncated + "... [truncated]";
    }

    /**
     * Select the most recent messages that fit within the token limit.
     * Always includes the system message (if any) and the most recent messages.
     */
    public List<Message> selectRecentMessages(List<Message> messages, MemoryConfig config) {
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }

        int maxTokens = config.getMaxContextTokens();
        int retainCount = config.getRetainRecentCount();

        // Always try to keep at least retainRecentCount messages
        if (messages.size() <= retainCount) {
            return new ArrayList<>(messages);
        }

        List<Message> selected = new ArrayList<>();
        int totalTokens = 0;

        // Start from the most recent and work backwards
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            int messageTokens = estimateTokens(message.getContent());

            if (totalTokens + messageTokens > maxTokens && selected.size() >= retainCount) {
                break;
            }

            selected.add(0, message);
            totalTokens += messageTokens;
        }

        log.debug("Selected {} of {} messages, estimated {} tokens",
                selected.size(), messages.size(), totalTokens);
        return selected;
    }
}
