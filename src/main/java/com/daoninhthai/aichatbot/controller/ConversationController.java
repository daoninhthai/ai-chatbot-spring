package com.daoninhthai.aichatbot.controller;

import com.daoninhthai.aichatbot.dto.request.CreateConversationRequest;
import com.daoninhthai.aichatbot.dto.request.UpdateConversationRequest;
import com.daoninhthai.aichatbot.dto.response.ConversationListResponse;
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
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversations", description = "Manage chat conversations with full CRUD and history")
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping
    @Operation(summary = "List all conversations for the current user")
    public ResponseEntity<List<ConversationResponse>> listConversations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(conversationService.getUserConversations(userDetails.toUser()));
    }

    @GetMapping("/list")
    @Operation(summary = "List conversations with last message preview and message count")
    public ResponseEntity<List<ConversationListResponse>> listConversationsWithPreview(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(conversationService.getUserConversationList(userDetails.toUser()));
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

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update a conversation")
    public ResponseEntity<ConversationResponse> patchConversation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateConversationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(conversationService.patchConversation(id, request, userDetails.toUser()));
    }

    @PutMapping("/{id}/rename")
    @Operation(summary = "Rename a conversation")
    public ResponseEntity<ConversationResponse> renameConversation(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String newTitle = request.get("title");
        return ResponseEntity.ok(conversationService.renameConversation(id, newTitle, userDetails.toUser()));
    }

    @PutMapping("/{id}/pin")
    @Operation(summary = "Pin or unpin a conversation")
    public ResponseEntity<ConversationResponse> pinConversation(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean pinned = request.getOrDefault("pinned", true);
        return ResponseEntity.ok(conversationService.pinConversation(id, pinned, userDetails.toUser()));
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
