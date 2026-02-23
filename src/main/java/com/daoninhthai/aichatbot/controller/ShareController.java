package com.daoninhthai.aichatbot.controller;

import com.daoninhthai.aichatbot.dto.response.ChatMessageResponse;
import com.daoninhthai.aichatbot.entity.SharedConversation;
import com.daoninhthai.aichatbot.exception.ResourceNotFoundException;
import com.daoninhthai.aichatbot.repository.MessageRepository;
import com.daoninhthai.aichatbot.repository.SharedConversationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Public controller for accessing shared conversations via token.
 * No authentication required.
 */
@RestController
@RequestMapping("/api/shared")
@RequiredArgsConstructor
@Tag(name = "Shared Conversations", description = "Public access to shared conversation links")
public class ShareController {

    private final SharedConversationRepository sharedConversationRepository;
    private final MessageRepository messageRepository;

    @GetMapping("/{token}")
    @Operation(summary = "View a shared conversation (public, no auth required)")
    public ResponseEntity<Map<String, Object>> getSharedConversation(@PathVariable String token) {
        SharedConversation shared = sharedConversationRepository.findByShareToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Shared conversation not found"));

        // Check expiration
        if (shared.getExpiresAt() != null && shared.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }

        // Increment view count
        shared.incrementViewCount();
        sharedConversationRepository.save(shared);

        // Load messages
        List<ChatMessageResponse> messages = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(shared.getConversationId())
                .stream()
                .map(ChatMessageResponse::from)
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("conversationId", shared.getConversationId());
        response.put("viewCount", shared.getViewCount());
        response.put("expiresAt", shared.getExpiresAt());
        response.put("messages", messages);

        return ResponseEntity.ok(response);
    }
}
