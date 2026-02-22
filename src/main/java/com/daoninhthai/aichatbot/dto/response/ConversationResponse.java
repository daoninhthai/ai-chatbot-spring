package com.daoninhthai.aichatbot.dto.response;

import com.daoninhthai.aichatbot.entity.Conversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ConversationResponse {
    private Long id;
    private String title;
    private String systemPrompt;
    private boolean ragEnabled;
    private int messageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ChatMessageResponse> messages;

    public static ConversationResponse from(Conversation conversation, boolean includeMessages) {
        var builder = ConversationResponse.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .systemPrompt(conversation.getSystemPrompt())
                .ragEnabled(conversation.isRagEnabled())
                .messageCount(conversation.getMessages().size())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt());

        if (includeMessages) {
            builder.messages(conversation.getMessages().stream()
                    .map(ChatMessageResponse::from)
                    .toList());
        }

        return builder.build();
    }
}
