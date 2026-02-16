package com.daoninhthai.aichatbot.service;

import com.daoninhthai.aichatbot.dto.request.CreateConversationRequest;
import com.daoninhthai.aichatbot.dto.request.UpdateConversationRequest;
import com.daoninhthai.aichatbot.dto.response.ConversationListResponse;
import com.daoninhthai.aichatbot.dto.response.ConversationResponse;
import com.daoninhthai.aichatbot.entity.Conversation;
import com.daoninhthai.aichatbot.entity.User;
import com.daoninhthai.aichatbot.exception.ResourceNotFoundException;
import com.daoninhthai.aichatbot.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;

    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations(User user) {
        return conversationRepository.findByUserOrderByUpdatedAtDesc(user)
                .stream()
                .map(c -> ConversationResponse.from(c, false))
                .toList();
    }

    /**
     * Get conversations as a lightweight list with last message preview.
     */
    @Transactional(readOnly = true)
    public List<ConversationListResponse> getUserConversationList(User user) {
        return conversationRepository.findByUserOrderByUpdatedAtDesc(user)
                .stream()
                .map(ConversationListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversation(Long id, User user) {
        Conversation conversation = findByIdAndUser(id, user);
        return ConversationResponse.from(conversation, true);
    }

    @Transactional
    public ConversationResponse createConversation(CreateConversationRequest request, User user) {
        Conversation conversation = Conversation.builder()
                .title(request.getTitle().trim())
                .user(user)
                .systemPrompt(request.getSystemPrompt())
                .ragEnabled(request.isRagEnabled())
                .build();

        conversation = conversationRepository.save(conversation);
        log.info("Conversation created: {} for user {}", conversation.getId(), user.getEmail());
        return ConversationResponse.from(conversation, false);
    }

    @Transactional
    public ConversationResponse updateConversation(Long id, CreateConversationRequest request, User user) {
        Conversation conversation = findByIdAndUser(id, user);
        conversation.setTitle(request.getTitle().trim());
        conversation.setSystemPrompt(request.getSystemPrompt());
        conversation.setRagEnabled(request.isRagEnabled());

        conversation = conversationRepository.save(conversation);
        return ConversationResponse.from(conversation, false);
    }

    /**
     * Rename a conversation.
     */
    @Transactional
    public ConversationResponse renameConversation(Long id, String newTitle, User user) {
        Conversation conversation = findByIdAndUser(id, user);
        conversation.setTitle(newTitle.trim());
        conversation = conversationRepository.save(conversation);
        log.info("Conversation {} renamed to '{}' by user {}", id, newTitle, user.getEmail());
        return ConversationResponse.from(conversation, false);
    }

    /**
     * Pin or unpin a conversation.
     * Uses the system prompt field to store pin state as a lightweight approach
     * without requiring a schema migration.
     */
    @Transactional
    public ConversationResponse pinConversation(Long id, boolean pinned, User user) {
        Conversation conversation = findByIdAndUser(id, user);
        // Pin status tracked via updated timestamp ordering — pinned conversations
        // get a far-future update time so they sort first
        if (pinned) {
            conversation.setUpdatedAt(java.time.LocalDateTime.of(2099, 12, 31, 23, 59));
        } else {
            conversation.setUpdatedAt(java.time.LocalDateTime.now());
        }
        conversation = conversationRepository.save(conversation);
        log.info("Conversation {} {} by user {}", id, pinned ? "pinned" : "unpinned", user.getEmail());
        return ConversationResponse.from(conversation, false);
    }

    /**
     * Partial update using UpdateConversationRequest.
     */
    @Transactional
    public ConversationResponse patchConversation(Long id, UpdateConversationRequest request, User user) {
        Conversation conversation = findByIdAndUser(id, user);

        if (request.getTitle() != null) {
            conversation.setTitle(request.getTitle().trim());
        }
        if (request.getSystemPrompt() != null) {
            conversation.setSystemPrompt(request.getSystemPrompt());
        }
        if (request.getRagEnabled() != null) {
            conversation.setRagEnabled(request.getRagEnabled());
        }

        conversation = conversationRepository.save(conversation);
        return ConversationResponse.from(conversation, false);
    }

    @Transactional
    public void deleteConversation(Long id, User user) {
        Conversation conversation = findByIdAndUser(id, user);
        conversationRepository.delete(conversation);
        log.info("Conversation deleted: {} by user {}", id, user.getEmail());
    }

    private Conversation findByIdAndUser(Long id, User user) {
        return conversationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", id));
    }
}
