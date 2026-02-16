package com.daoninhthai.aichatbot.controller;

import com.daoninhthai.aichatbot.dto.request.PromptTemplateRequest;
import com.daoninhthai.aichatbot.dto.request.RenderTemplateRequest;
import com.daoninhthai.aichatbot.dto.response.PromptTemplateResponse;
import com.daoninhthai.aichatbot.security.CustomUserDetails;
import com.daoninhthai.aichatbot.service.PromptTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@Tag(name = "Prompt Templates", description = "Manage reusable prompt templates")
public class PromptTemplateController {

    private final PromptTemplateService templateService;

    @PostMapping
    @Operation(summary = "Create a new prompt template")
    public ResponseEntity<PromptTemplateResponse> createTemplate(
            @Valid @RequestBody PromptTemplateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(templateService.createTemplate(request, userDetails.getId().toString()));
    }

    @GetMapping
    @Operation(summary = "List all prompt templates")
    public ResponseEntity<List<PromptTemplateResponse>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a prompt template by ID")
    public ResponseEntity<PromptTemplateResponse> getTemplate(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.getTemplate(id));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get templates by category")
    public ResponseEntity<List<PromptTemplateResponse>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(templateService.getTemplatesByCategory(category));
    }

    @GetMapping("/search")
    @Operation(summary = "Search templates by name or description")
    public ResponseEntity<List<PromptTemplateResponse>> searchTemplates(@RequestParam String q) {
        return ResponseEntity.ok(templateService.searchTemplates(q));
    }

    @GetMapping("/popular")
    @Operation(summary = "Get most popular templates")
    public ResponseEntity<List<PromptTemplateResponse>> getPopularTemplates(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(templateService.getPopularTemplates(limit));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a prompt template")
    public ResponseEntity<PromptTemplateResponse> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody PromptTemplateRequest request) {
        return ResponseEntity.ok(templateService.updateTemplate(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a prompt template")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/render")
    @Operation(summary = "Render a template with variable substitution")
    public ResponseEntity<Map<String, String>> renderTemplate(
            @PathVariable Long id,
            @Valid @RequestBody RenderTemplateRequest request) {
        String rendered = templateService.renderTemplate(id, request.getVariables());
        return ResponseEntity.ok(Map.of("rendered", rendered));
    }
}
