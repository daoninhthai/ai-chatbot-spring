package com.daoninhthai.aichatbot.controller;

import com.daoninhthai.aichatbot.dto.ModelUsageStats;
import com.daoninhthai.aichatbot.dto.SystemStats;
import com.daoninhthai.aichatbot.dto.UserStatsResponse;
import com.daoninhthai.aichatbot.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin dashboard controller providing system metrics and user analytics.
 * Restricted to users with ADMIN role.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin dashboard with system metrics and analytics")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    @Operation(summary = "Get system-wide statistics")
    public ResponseEntity<SystemStats> getSystemStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }

    @GetMapping("/users")
    @Operation(summary = "Get per-user statistics")
    public ResponseEntity<List<UserStatsResponse>> getUserStats() {
        return ResponseEntity.ok(adminService.getUserStats());
    }

    @GetMapping("/model-usage")
    @Operation(summary = "Get AI model usage breakdown")
    public ResponseEntity<List<ModelUsageStats>> getModelUsage() {
        return ResponseEntity.ok(adminService.getModelUsageBreakdown());
    }

    @GetMapping("/errors")
    @Operation(summary = "Get recent system errors")
    public ResponseEntity<List<String>> getRecentErrors() {
        return ResponseEntity.ok(adminService.getRecentErrors());
    }
}
