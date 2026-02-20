package com.daoninhthai.aichatbot.service;

import com.daoninhthai.aichatbot.dto.MemoryConfig;
import com.daoninhthai.aichatbot.entity.ConversationSummary;
import com.daoninhthai.aichatbot.entity.Message;
import com.daoninhthai.aichatbot.repository.ConversationSummaryRepository;
import com.daoninhthai.aichatbot.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing conversation memory with automatic summarization
 * and context window management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryService {

    private final MessageRepository messageRepository;
    private final ConversationSummaryRepository conversationSummaryRepository;
    private final ContextWindowManager contextWindowManager;
    private final ChatClient chatClient;

    private static final MemoryConfig DEFAULT_CONFIG = MemoryConfig.builder()
            .maxContextTokens(4096)
            .summarizationThreshold(50)
            .retainRecentCount(10)
            .build();

    /**
     * Get relevant conversation history, including summaries of older messages.
     */
    @Transactional(readOnly = true)
    public List<Message> getRelevantHistory(Long conversationId) {
        return getRelevantHistory(conversationId, DEFAULT_CONFIG);
    }

    /**
     * Get relevant conversation history with custom memory configuration.
     */
    @Transactional(readOnly = true)
    public List<Message> getRelevantHistory(Long conversationId, MemoryConfig config) {
        List<Message> allMessages = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId);

        return contextWindowManager.selectRecentMessages(allMessages, config);
    }

    /**
     * Summarize old messages when conversation exceeds the summarization threshold.
     * Uses AI to create a concise summary of older messages.
     */
    @Transactional
    public void summarizeOldMessages(Long conversationId) {
        summarizeOldMessages(conversationId, DEFAULT_CONFIG);
    }

    /**
     * Summarize old messages with custom configuration.
     */
    @Transactional
    public void summarizeOldMessages(Long conversationId, MemoryConfig config) {
        List<Message> allMessages = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId);

        if (allMessages.size() <= config.getSummarizationThreshold()) {
            log.debug("Conversation {} has {} messages, below threshold {}",
                    conversationId, allMessages.size(), config.getSummarizationThreshold());
            return;
        }

        int messagesToSummarize = allMessages.size() - config.getRetainRecentCount();
        if (messagesToSummarize <= 0) return;

        List<Message> oldMessages = allMessages.subList(0, messagesToSummarize);
        String conversationText = oldMessages.stream()
                .map(m -> m.getRole().name() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));

        // Use AI to summarize the conversation
        String summaryPrompt = String.format("""
                Summarize the following conversation concisely. Capture the key topics discussed,
                important decisions made, and any action items mentioned.

                Conversation:
                %s

                Provide a brief summary:
                """, contextWindowManager.truncateToFit(conversationText, 3000));

        String summaryText = chatClient.prompt()
                .user(summaryPrompt)
                .call()
                .content();

        ConversationSummary summary = ConversationSummary.builder()
                .conversationId(conversationId)
                .summaryText(summaryText)
                .fromMessageIndex(0)
                .toMessageIndex(messagesToSummarize - 1)
                .build();

        conversationSummaryRepository.save(summary);
        log.info("Summarized messages 0-{} for conversation {}", messagesToSummarize - 1, conversationId);
    }

    /**
     * Prune old messages from a conversation, keeping only recent ones
     * after a summary has been created.
     */
    @Transactional
    public void pruneOldMessages(Long conversationId, int keepRecent) {
        List<Message> allMessages = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId);

        if (allMessages.size() <= keepRecent) {
            return;
        }

        List<Message> toDelete = allMessages.subList(0, allMessages.size() - keepRecent);
        messageRepository.deleteAll(toDelete);
        log.info("Pruned {} old messages from conversation {}", toDelete.size(), conversationId);
    }
}
