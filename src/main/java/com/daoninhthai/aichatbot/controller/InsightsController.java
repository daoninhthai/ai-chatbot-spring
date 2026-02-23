package com.daoninhthai.aichatbot.controller;

import com.daoninhthai.aichatbot.dto.ConversationInsights;
import com.daoninhthai.aichatbot.dto.UserInsights;
import com.daoninhthai.aichatbot.security.CustomUserDetails;
import com.daoninhthai.aichatbot.service.InsightsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for AI-powered conversation insights and user analytics.
 */
@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
@Tag(name = "Insights", description = "AI-powered conversation insights and analytics")
public class InsightsController {

    private final InsightsService insightsService;

    @GetMapping("/conversation/{id}")
    @Operation(summary = "Get AI-generated insights for a specific conversation")
    public ResponseEntity<ConversationInsights> getConversationInsights(@PathVariable Long id) {
        return ResponseEntity.ok(insightsService.analyzeConversation(id));
    }

    @GetMapping("/user-summary")
    @Operation(summary = "Get aggregated insights across all user conversations")
    public ResponseEntity<UserInsights> getUserSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(insightsService.getUserSummary(userDetails.toUser()));
    }
}
