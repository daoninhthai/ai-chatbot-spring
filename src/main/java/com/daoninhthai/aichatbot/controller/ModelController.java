package com.daoninhthai.aichatbot.controller;

import com.daoninhthai.aichatbot.dto.ModelInfo;
import com.daoninhthai.aichatbot.security.CustomUserDetails;
import com.daoninhthai.aichatbot.service.ModelProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
@Tag(name = "Models", description = "Manage AI model providers and selection")
public class ModelController {

    private final ModelProviderService modelProviderService;

    @GetMapping
    @Operation(summary = "List all available AI models across providers")
    public ResponseEntity<List<ModelInfo>> getAvailableModels() {
        return ResponseEntity.ok(modelProviderService.getAvailableModels());
    }

    @GetMapping("/providers")
    @Operation(summary = "List all supported AI providers")
    public ResponseEntity<List<String>> getProviders() {
        return ResponseEntity.ok(modelProviderService.getProviders());
    }

    @GetMapping("/provider/{provider}")
    @Operation(summary = "List models for a specific provider")
    public ResponseEntity<List<ModelInfo>> getModelsByProvider(@PathVariable String provider) {
        return ResponseEntity.ok(modelProviderService.getModelsByProvider(provider));
    }

    @GetMapping("/current")
    @Operation(summary = "Get the currently selected model for the user")
    public ResponseEntity<ModelInfo> getCurrentModel(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(modelProviderService.getCurrentModel(userDetails.getId()));
    }

    @PutMapping("/select")
    @Operation(summary = "Switch to a different AI model")
    public ResponseEntity<ModelInfo> selectModel(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String modelId = request.get("modelId");
        ModelInfo selected = modelProviderService.switchModel(userDetails.getId(), modelId);
        return ResponseEntity.ok(selected);
    }
}
