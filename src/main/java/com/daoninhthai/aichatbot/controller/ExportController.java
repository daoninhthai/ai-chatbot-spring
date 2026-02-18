package com.daoninhthai.aichatbot.controller;

import com.daoninhthai.aichatbot.dto.ShareResponse;
import com.daoninhthai.aichatbot.security.CustomUserDetails;
import com.daoninhthai.aichatbot.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for exporting conversations and generating share links.
 */
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Tag(name = "Export", description = "Export conversations to various formats and share them")
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/{id}/export")
    @Operation(summary = "Export a conversation in Markdown or JSON format")
    public ResponseEntity<String> exportConversation(
            @PathVariable Long id,
            @RequestParam(value = "format", defaultValue = "md") String format,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if ("json".equalsIgnoreCase(format)) {
            String json = exportService.exportToJson(id, userDetails.toUser());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"conversation-" + id + ".json\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        }

        String markdown = exportService.exportToMarkdown(id, userDetails.toUser());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"conversation-" + id + ".md\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(markdown);
    }

    @PostMapping("/{id}/share")
    @Operation(summary = "Generate a share link for a conversation")
    public ResponseEntity<ShareResponse> shareConversation(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(exportService.generateShareToken(id, userDetails.toUser()));
    }
}
