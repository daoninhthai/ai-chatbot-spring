package com.daoninhthai.aichatbot.controller;

import com.daoninhthai.aichatbot.dto.request.CreateConversationRequest;
import com.daoninhthai.aichatbot.dto.response.ConversationResponse;
import com.daoninhthai.aichatbot.security.CustomUserDetails;
import com.daoninhthai.aichatbot.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversations", description = "Manage chat conversations")
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping
    @Operation(summary = "List all conversations for the current user")
    public ResponseEntity<List<ConversationResponse>> listConversations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(conversationService.getUserConversations(userDetails.toUser()));
    }

    @PostMapping
    @Operation(summary = "Create a new conversation")
    public ResponseEntity<ConversationResponse> createConversation(
            @Valid @RequestBody CreateConversationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conversationService.createConversation(request, userDetails.toUser()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a conversation with all messages")
    public ResponseEntity<ConversationResponse> getConversation(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(conversationService.getConversation(id, userDetails.toUser()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update conversation title or settings")
    public ResponseEntity<ConversationResponse> updateConversation(
            @PathVariable Long id,
            @Valid @RequestBody CreateConversationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(conversationService.updateConversation(id, request, userDetails.toUser()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a conversation and all its messages")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        conversationService.deleteConversation(id, userDetails.toUser());
        return ResponseEntity.noContent().build();
    }
}
