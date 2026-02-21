package com.daoninhthai.aichatbot.repository;

import com.daoninhthai.aichatbot.entity.WebhookConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebhookConfigRepository extends JpaRepository<WebhookConfig, Long> {

    List<WebhookConfig> findByActiveTrue();

    List<WebhookConfig> findByUserId(Long userId);
}
