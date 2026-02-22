package com.daoninhthai.aichatbot.service;

import com.daoninhthai.aichatbot.dto.request.CreateConversationRequest;
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
