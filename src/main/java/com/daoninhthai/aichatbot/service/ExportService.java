package com.daoninhthai.aichatbot.service;

import com.daoninhthai.aichatbot.dto.ShareResponse;
import com.daoninhthai.aichatbot.entity.Conversation;
import com.daoninhthai.aichatbot.entity.Message;
import com.daoninhthai.aichatbot.entity.SharedConversation;
import com.daoninhthai.aichatbot.entity.User;
import com.daoninhthai.aichatbot.exception.ResourceNotFoundException;
import com.daoninhthai.aichatbot.repository.ConversationRepository;
import com.daoninhthai.aichatbot.repository.MessageRepository;
import com.daoninhthai.aichatbot.repository.SharedConversationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for exporting conversations to various formats and managing sharing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SharedConversationRepository sharedConversationRepository;

    /**
     * Export a conversation to Markdown format.
     */
    @Transactional(readOnly = true)
    public String exportToMarkdown(Long conversationId, User user) {
        Conversation conversation = getConversation(conversationId, user);
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        StringBuilder markdown = new StringBuilder();
        markdown.append("# ").append(conversation.getTitle()).append("\n\n");
        markdown.append("**Exported:** ").append(LocalDateTime.now()).append("\n\n");
        markdown.append("---\n\n");

        for (Message message : messages) {
            String roleLabel = switch (message.getRole()) {
                case USER -> "**User**";
                case ASSISTANT -> "**Assistant**";
                case SYSTEM -> "**System**";
            };
            markdown.append(roleLabel).append(" (")
                    .append(message.getCreatedAt()).append(")\n\n");
            markdown.append(message.getContent()).append("\n\n");
            markdown.append("---\n\n");
        }

        log.info("Exported conversation {} to Markdown ({} messages)", conversationId, messages.size());
        return markdown.toString();
    }

    /**
     * Export a conversation to JSON format.
     */
    @Transactional(readOnly = true)
    public String exportToJson(Long conversationId, User user) {
        Conversation conversation = getConversation(conversationId, user);
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            Map<String, Object> export = new LinkedHashMap<>();
            export.put("title", conversation.getTitle());
            export.put("exportedAt", LocalDateTime.now().toString());
            export.put("messageCount", messages.size());

            List<Map<String, Object>> messageList = new ArrayList<>();
            for (Message message : messages) {
                Map<String, Object> msgMap = new LinkedHashMap<>();
                msgMap.put("role", message.getRole().name());
                msgMap.put("content", message.getContent());
                msgMap.put("model", message.getModel());
                msgMap.put("createdAt", message.getCreatedAt() != null ? message.getCreatedAt().toString() : null);
                messageList.add(msgMap);
            }
            export.put("messages", messageList);

            log.info("Exported conversation {} to JSON ({} messages)", conversationId, messages.size());
            return mapper.writeValueAsString(export);
        } catch (Exception e) {
            log.error("Failed to export conversation {} to JSON", conversationId, e);
            throw new RuntimeException("JSON export failed: " + e.getMessage(), e);
        }
    }

    /**
     * Generate a share token for a conversation.
     */
    @Transactional
    public ShareResponse generateShareToken(Long conversationId, User user) {
        Conversation conversation = getConversation(conversationId, user);

        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        SharedConversation shared = SharedConversation.builder()
                .conversationId(conversation.getId())
                .shareToken(token)
                .expiresAt(expiresAt)
                .build();

        sharedConversationRepository.save(shared);
        log.info("Share token generated for conversation {}: {}", conversationId, token);

        return ShareResponse.builder()
                .shareUrl("/api/shared/" + token)
                .expiresAt(expiresAt)
                .token(token)
                .build();
    }

    private Conversation getConversation(Long conversationId, User user) {
        return conversationRepository.findByIdAndUser(conversationId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", conversationId));
    }
}
