package com.daoninhthai.aichatbot.repository;

import com.daoninhthai.aichatbot.entity.SharedConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SharedConversationRepository extends JpaRepository<SharedConversation, Long> {

    Optional<SharedConversation> findByShareToken(String shareToken);

    List<SharedConversation> findByConversationId(Long conversationId);
}
