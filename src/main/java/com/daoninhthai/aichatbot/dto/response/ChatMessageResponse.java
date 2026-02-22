package com.daoninhthai.aichatbot.dto.response;

import com.daoninhthai.aichatbot.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private String role;
    private String content;
    private String model;
    private Integer tokenCount;
    private LocalDateTime createdAt;

    public static ChatMessageResponse from(Message message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .role(message.getRole().name())
                .content(message.getContent())
                .model(message.getModel())
                .tokenCount(message.getTokenCount())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
