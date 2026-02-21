package com.daoninhthai.aichatbot.service;

import com.daoninhthai.aichatbot.entity.WebhookConfig;
import com.daoninhthai.aichatbot.entity.WebhookDelivery;
import com.daoninhthai.aichatbot.exception.ResourceNotFoundException;
import com.daoninhthai.aichatbot.repository.WebhookConfigRepository;
import com.daoninhthai.aichatbot.repository.WebhookDeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing webhooks and triggering webhook deliveries
 * with HMAC-SHA256 signature verification.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final WebhookConfigRepository webhookConfigRepository;
    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final WebClient.Builder webClientBuilder;

    /**
     * Register a new webhook configuration.
     */
    @Transactional
    public WebhookConfig register(Long userId, String url, String events) {
        WebhookConfig config = WebhookConfig.builder()
                .userId(userId)
                .url(url)
                .events(events)
                .secret(UUID.randomUUID().toString().replace("-", ""))
                .active(true)
                .build();

        config = webhookConfigRepository.save(config);
        log.info("Webhook registered for user {}: {}", userId, url);
        return config;
    }

    /**
     * Update an existing webhook configuration.
     */
    @Transactional
    public WebhookConfig update(Long id, String url, String events, boolean active) {
        WebhookConfig config = webhookConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WebhookConfig", id));

        config.setUrl(url);
        config.setEvents(events);
        config.setActive(active);

        config = webhookConfigRepository.save(config);
        log.info("Webhook {} updated", id);
        return config;
    }

    /**
     * Delete a webhook configuration.
     */
    @Transactional
    public void delete(Long id) {
        WebhookConfig config = webhookConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WebhookConfig", id));
        webhookConfigRepository.delete(config);
        log.info("Webhook {} deleted", id);
    }

    /**
     * Trigger webhook delivery for a specific event.
     * Sends the payload asynchronously with HMAC-SHA256 signature in X-Webhook-Signature header.
     */
    public void triggerWebhook(String event, String payload) {
        List<WebhookConfig> activeWebhooks = webhookConfigRepository.findByActiveTrue();

        for (WebhookConfig config : activeWebhooks) {
            // Check if this webhook is subscribed to this event
            if (!isSubscribedToEvent(config, event)) {
                continue;
            }

            String signature = computeHmacSha256(payload, config.getSecret());

            // Send async webhook
            webClientBuilder.build()
                    .post()
                    .uri(config.getUrl())
                    .header("X-Webhook-Signature", "sha256=" + signature)
                    .header("X-Webhook-Event", event)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .subscribe(
                            response -> {
                                saveDelivery(config.getId(), event, payload,
                                        response.getStatusCode().value(), true);
                                log.debug("Webhook delivered to {}: {}", config.getUrl(), event);
                            },
                            error -> {
                                saveDelivery(config.getId(), event, payload, 0, false);
                                log.error("Webhook delivery failed to {}: {}",
                                        config.getUrl(), error.getMessage());
                            }
                    );
        }
    }

    /**
     * Get delivery history for a webhook.
     */
    @Transactional(readOnly = true)
    public List<WebhookDelivery> getDeliveries(Long webhookConfigId) {
        return webhookDeliveryRepository
                .findByWebhookConfigIdOrderByDeliveredAtDesc(webhookConfigId);
    }

    /**
     * Get all webhooks for a user.
     */
    @Transactional(readOnly = true)
    public List<WebhookConfig> getUserWebhooks(Long userId) {
        return webhookConfigRepository.findByUserId(userId);
    }

    private boolean isSubscribedToEvent(WebhookConfig config, String event) {
        if (config.getEvents() == null || config.getEvents().isBlank()) return false;
        String[] subscribedEvents = config.getEvents().split(",");
        for (String e : subscribedEvents) {
            if (e.trim().equalsIgnoreCase(event) || "*".equals(e.trim())) {
                return true;
            }
        }
        return false;
    }

    private String computeHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            log.error("HMAC-SHA256 computation failed", e);
            throw new RuntimeException("Failed to compute webhook signature", e);
        }
    }

    private void saveDelivery(Long configId, String event, String payload,
                              int statusCode, boolean success) {
        WebhookDelivery delivery = WebhookDelivery.builder()
                .webhookConfigId(configId)
                .event(event)
                .payload(payload)
                .responseStatus(statusCode)
                .attempts(1)
                .success(success)
                .deliveredAt(LocalDateTime.now())
                .build();
        webhookDeliveryRepository.save(delivery);
    }
}
