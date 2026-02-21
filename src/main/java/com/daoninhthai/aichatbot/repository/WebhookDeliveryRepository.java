package com.daoninhthai.aichatbot.repository;

import com.daoninhthai.aichatbot.entity.WebhookDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {

    List<WebhookDelivery> findByWebhookConfigIdOrderByDeliveredAtDesc(Long webhookConfigId);
}
