package com.daoninhthai.aichatbot.controller;

import com.daoninhthai.aichatbot.entity.WebhookConfig;
import com.daoninhthai.aichatbot.entity.WebhookDelivery;
import com.daoninhthai.aichatbot.security.CustomUserDetails;
import com.daoninhthai.aichatbot.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for managing webhook configurations and viewing delivery history.
 */
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Manage webhook integrations for external services")
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping
    @Operation(summary = "Register a new webhook")
    public ResponseEntity<WebhookConfig> createWebhook(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        WebhookConfig config = webhookService.register(
                userDetails.getId(),
                request.get("url"),
                request.get("events")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(config);
    }

    @GetMapping
    @Operation(summary = "List all webhooks for the current user")
    public ResponseEntity<List<WebhookConfig>> listWebhooks(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(webhookService.getUserWebhooks(userDetails.getId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a webhook configuration")
    public ResponseEntity<WebhookConfig> updateWebhook(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        WebhookConfig config = webhookService.update(
                id,
                (String) request.get("url"),
                (String) request.get("events"),
                (Boolean) request.getOrDefault("active", true)
        );
        return ResponseEntity.ok(config);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a webhook")
    public ResponseEntity<Void> deleteWebhook(@PathVariable Long id) {
        webhookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/deliveries")
    @Operation(summary = "Get delivery history for a webhook")
    public ResponseEntity<List<WebhookDelivery>> getDeliveries(@PathVariable Long id) {
        return ResponseEntity.ok(webhookService.getDeliveries(id));
    }
}
