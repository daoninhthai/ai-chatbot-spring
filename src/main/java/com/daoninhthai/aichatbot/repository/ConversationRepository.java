package com.daoninhthai.aichatbot.repository;

import com.daoninhthai.aichatbot.entity.Conversation;
import com.daoninhthai.aichatbot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByUserOrderByUpdatedAtDesc(User user);

    Optional<Conversation> findByIdAndUser(Long id, User user);

    long countByUser(User user);
}
