package com.daoninhthai.aichatbot.dto.response;

import com.daoninhthai.aichatbot.entity.Conversation;
import com.daoninhthai.aichatbot.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for conversation list view with last message preview and message count.
 */
@Data
@Builder
@AllArgsConstructor
public class ConversationListResponse {

    private Long id;
    private String title;
    private boolean pinned;
    private boolean ragEnabled;
    private int messageCount;
    private String lastMessagePreview;
    private String lastMessageRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Build a list response from a conversation entity, including a preview of the last message.
     */
    public static ConversationListResponse from(Conversation conversation) {
        List<Message> messages = conversation.getMessages();
        String preview = null;
        String lastRole = null;

        if (messages != null && !messages.isEmpty()) {
            Message lastMessage = messages.get(messages.size() - 1);
            String content = lastMessage.getContent();
            preview = content.length() > 120 ? content.substring(0, 120) + "..." : content;
            lastRole = lastMessage.getRole().name();
        }

        return ConversationListResponse.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .pinned(false) // default — extend Conversation entity if needed
                .ragEnabled(conversation.isRagEnabled())
                .messageCount(messages != null ? messages.size() : 0)
                .lastMessagePreview(preview)
                .lastMessageRole(lastRole)
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }
}
