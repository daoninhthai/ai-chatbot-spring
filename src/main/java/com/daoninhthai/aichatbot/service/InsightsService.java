package com.daoninhthai.aichatbot.service;

import com.daoninhthai.aichatbot.dto.ConversationInsights;
import com.daoninhthai.aichatbot.dto.UserInsights;
import com.daoninhthai.aichatbot.entity.Conversation;
import com.daoninhthai.aichatbot.entity.Message;
import com.daoninhthai.aichatbot.entity.User;
import com.daoninhthai.aichatbot.repository.ConversationRepository;
import com.daoninhthai.aichatbot.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating AI-powered conversation insights and analytics
 * using heuristic analysis (keyword counting, sentiment detection, etc.).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InsightsService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    private static final Set<String> POSITIVE_KEYWORDS = Set.of(
            "great", "thanks", "good", "excellent", "perfect", "helpful",
            "awesome", "wonderful", "appreciate", "love", "amazing"
    );

    private static final Set<String> NEGATIVE_KEYWORDS = Set.of(
            "bad", "wrong", "error", "fail", "problem", "issue",
            "broken", "terrible", "awful", "hate", "frustrated"
    );

    private static final Map<String, List<String>> TOPIC_KEYWORDS = Map.of(
            "programming", List.of("code", "function", "variable", "class", "method", "api", "debug"),
            "data", List.of("database", "query", "sql", "data", "table", "schema", "migration"),
            "devops", List.of("deploy", "docker", "kubernetes", "ci/cd", "pipeline", "server"),
            "ai/ml", List.of("model", "training", "neural", "machine learning", "ai", "gpt", "llm"),
            "web", List.of("html", "css", "javascript", "react", "frontend", "backend", "http"),
            "general", List.of("help", "explain", "what", "how", "why", "tell me")
    );

    /**
     * Analyze a conversation and generate insights using heuristic methods.
     */
    @Transactional(readOnly = true)
    public ConversationInsights analyzeConversation(Long conversationId) {
        List<Message> messages = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId);

        if (messages.isEmpty()) {
            return ConversationInsights.builder()
                    .sentiment("neutral")
                    .topics(List.of())
                    .messageCount(0)
                    .avgResponseLength(0)
                    .complexityScore(0)
                    .build();
        }

        String allContent = messages.stream()
                .map(Message::getContent)
                .collect(Collectors.joining(" "))
                .toLowerCase();

        String sentiment = analyzeSentiment(allContent);
        List<String> topics = detectTopics(allContent);
        double avgResponseLength = messages.stream()
                .filter(m -> m.getRole() == Message.MessageRole.ASSISTANT)
                .mapToInt(m -> m.getContent().length())
                .average()
                .orElse(0);
        double complexityScore = calculateComplexity(messages);

        log.info("Insights generated for conversation {}: sentiment={}, topics={}",
                conversationId, sentiment, topics);

        return ConversationInsights.builder()
                .sentiment(sentiment)
                .topics(topics)
                .messageCount(messages.size())
                .avgResponseLength(avgResponseLength)
                .complexityScore(complexityScore)
                .build();
    }

    /**
     * Generate aggregated insights for a user across all conversations.
     */
    @Transactional(readOnly = true)
    public UserInsights getUserSummary(User user) {
        List<Conversation> conversations = conversationRepository
                .findByUserOrderByUpdatedAtDesc(user);

        long totalMessages = 0;
        Map<String, Integer> topicCounts = new HashMap<>();
        Map<Integer, Integer> hourCounts = new HashMap<>();

        for (Conversation conv : conversations) {
            List<Message> messages = messageRepository
                    .findByConversationIdOrderByCreatedAtAsc(conv.getId());
            totalMessages += messages.size();

            String allContent = messages.stream()
                    .map(Message::getContent)
                    .collect(Collectors.joining(" "))
                    .toLowerCase();

            for (String topic : detectTopics(allContent)) {
                topicCounts.merge(topic, 1, Integer::sum);
            }

            for (Message message : messages) {
                if (message.getCreatedAt() != null) {
                    int hour = message.getCreatedAt().getHour();
                    hourCounts.merge(hour, 1, Integer::sum);
                }
            }
        }

        int mostActiveHour = hourCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);

        List<String> topTopics = topicCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();

        double avgMessages = conversations.isEmpty() ? 0 :
                (double) totalMessages / conversations.size();

        return UserInsights.builder()
                .totalConversations(conversations.size())
                .totalMessages(totalMessages)
                .avgMessagesPerConversation(avgMessages)
                .mostActiveHour(mostActiveHour)
                .topTopics(topTopics)
                .build();
    }

    private String analyzeSentiment(String text) {
        long positiveCount = POSITIVE_KEYWORDS.stream()
                .filter(text::contains)
                .count();
        long negativeCount = NEGATIVE_KEYWORDS.stream()
                .filter(text::contains)
                .count();

        if (positiveCount > negativeCount * 2) return "positive";
        if (negativeCount > positiveCount * 2) return "negative";
        if (positiveCount > negativeCount) return "mostly positive";
        if (negativeCount > positiveCount) return "mostly negative";
        return "neutral";
    }

    private List<String> detectTopics(String text) {
        List<String> detectedTopics = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : TOPIC_KEYWORDS.entrySet()) {
            long matchCount = entry.getValue().stream()
                    .filter(text::contains)
                    .count();
            if (matchCount >= 2) {
                detectedTopics.add(entry.getKey());
            }
        }

        return detectedTopics.isEmpty() ? List.of("general") : detectedTopics;
    }

    private double calculateComplexity(List<Message> messages) {
        if (messages.isEmpty()) return 0;

        double avgLength = messages.stream()
                .mapToInt(m -> m.getContent().length())
                .average()
                .orElse(0);

        // Complexity based on average message length and technical keywords
        String allContent = messages.stream()
                .map(Message::getContent)
                .collect(Collectors.joining(" "))
                .toLowerCase();

        long technicalTerms = TOPIC_KEYWORDS.values().stream()
                .flatMap(List::stream)
                .filter(allContent::contains)
                .count();

        // Normalize complexity to 0-1 scale
        double lengthFactor = Math.min(avgLength / 1000.0, 1.0);
        double termFactor = Math.min(technicalTerms / 20.0, 1.0);

        return Math.round((lengthFactor * 0.4 + termFactor * 0.6) * 100.0) / 100.0;
    }
}
