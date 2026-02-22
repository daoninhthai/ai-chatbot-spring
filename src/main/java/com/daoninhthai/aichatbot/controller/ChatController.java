package com.daoninhthai.aichatbot.controller;

import com.daoninhthai.aichatbot.dto.request.ChatMessageRequest;
import com.daoninhthai.aichatbot.dto.response.ChatMessageResponse;
import com.daoninhthai.aichatbot.security.CustomUserDetails;
import com.daoninhthai.aichatbot.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "AI chat with streaming support")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/{conversationId}")
    @Operation(summary = "Send a message and get a complete response")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody ChatMessageRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ChatMessageResponse response = chatService.sendMessage(
                conversationId, request, userDetails.toUser());
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{conversationId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream a response via Server-Sent Events")
    public Flux<String> streamMessage(
            @PathVariable Long conversationId,
            @RequestParam String message,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return chatService.streamMessage(conversationId, message, userDetails.toUser());
    }

    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "Get all messages in a conversation")
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<ChatMessageResponse> messages = chatService.getMessages(
                conversationId, userDetails.toUser());
        return ResponseEntity.ok(messages);
    }
}
