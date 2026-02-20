package com.daoninhthai.aichatbot.repository;

import com.daoninhthai.aichatbot.entity.ConversationSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationSummaryRepository extends JpaRepository<ConversationSummary, Long> {

    List<ConversationSummary> findByConversationIdOrderByCreatedAtDesc(Long conversationId);
}
