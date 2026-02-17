package com.daoninhthai.aichatbot.controller;

import com.daoninhthai.aichatbot.dto.DailyUsage;
import com.daoninhthai.aichatbot.dto.UsageSummary;
import com.daoninhthai.aichatbot.security.CustomUserDetails;
import com.daoninhthai.aichatbot.service.UsageTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
@Tag(name = "Usage", description = "API usage tracking and analytics")
public class UsageController {

    private final UsageTrackingService usageTrackingService;

    @GetMapping("/daily")
    @Operation(summary = "Get daily usage breakdown for the last N days")
    public ResponseEntity<List<DailyUsage>> getDailyUsage(
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(usageTrackingService.getDailyUsage(userDetails.getId(), days));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get current month usage summary")
    public ResponseEntity<UsageSummary> getMonthlyUsage(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(usageTrackingService.getMonthlyUsage(userDetails.getId()));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get overall usage summary for the current user")
    public ResponseEntity<UsageSummary> getUsageSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(usageTrackingService.getMonthlyUsage(userDetails.getId()));
    }
}
