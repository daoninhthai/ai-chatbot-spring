package com.daoninhthai.aichatbot.service;

import com.daoninhthai.aichatbot.dto.request.ChatMessageRequest;
import com.daoninhthai.aichatbot.dto.response.ChatMessageResponse;
import com.daoninhthai.aichatbot.entity.Conversation;
import com.daoninhthai.aichatbot.entity.Message;
import com.daoninhthai.aichatbot.entity.User;
import com.daoninhthai.aichatbot.exception.ResourceNotFoundException;
import com.daoninhthai.aichatbot.repository.ConversationRepository;
import com.daoninhthai.aichatbot.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    /**
     * Send a message and get a complete (non-streaming) response.
     */
    @Transactional
    public ChatMessageResponse sendMessage(Long conversationId, ChatMessageRequest request, User user) {
        Conversation conversation = getConversation(conversationId, user);

        // Save user message
        Message userMessage = saveMessage(conversation, Message.MessageRole.USER, request.getContent(), null);

        // Call AI
        String aiResponse = chatClient.prompt()
                .user(request.getContent())
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId.toString()))
                .call()
                .content();

        // Save assistant message
        Message assistantMessage = saveMessage(conversation, Message.MessageRole.ASSISTANT, aiResponse, "gpt-4o-mini");

        // Update conversation timestamp
        conversation.setUpdatedAt(java.time.LocalDateTime.now());
        conversationRepository.save(conversation);

        log.debug("Chat response generated for conversation {}", conversationId);
        return ChatMessageResponse.from(assistantMessage);
    }

    /**
     * Stream a response via Server-Sent Events — the real deal, not fake chunking.
     */
    public Flux<String> streamMessage(Long conversationId, String content, User user) {
        Conversation conversation = getConversation(conversationId, user);

        // Save user message synchronously before streaming
        saveMessage(conversation, Message.MessageRole.USER, content, null);

        AtomicReference<StringBuilder> fullResponse = new AtomicReference<>(new StringBuilder());

        return chatClient.prompt()
                .user(content)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId.toString()))
                .stream()
                .content()
                .doOnNext(chunk -> fullResponse.get().append(chunk))
                .doOnComplete(() -> {
                    // Persist the complete assistant response after streaming finishes
                    String completeResponse = fullResponse.get().toString();
                    if (!completeResponse.isEmpty()) {
                        saveMessage(conversation, Message.MessageRole.ASSISTANT, completeResponse, "gpt-4o-mini");
                        conversation.setUpdatedAt(java.time.LocalDateTime.now());
                        conversationRepository.save(conversation);
                        log.debug("Streamed response saved for conversation {}", conversationId);
                    }
                })
                .doOnError(error -> log.error("Streaming error for conversation {}: {}",
                        conversationId, error.getMessage()));
    }

    /**
     * Get all messages in a conversation.
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long conversationId, User user) {
        Conversation conversation = getConversation(conversationId, user);
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId())
                .stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    private Conversation getConversation(Long conversationId, User user) {
        return conversationRepository.findByIdAndUser(conversationId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", conversationId));
    }

    private Message saveMessage(Conversation conversation, Message.MessageRole role, String content, String model) {
        Message message = Message.builder()
                .conversation(conversation)
                .role(role)
                .content(content)
                .model(model)
                .build();
        return messageRepository.save(message);
    }
}
