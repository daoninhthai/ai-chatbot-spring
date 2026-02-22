package com.daoninhthai.aichatbot.controller;

import com.daoninhthai.aichatbot.dto.response.DocumentResponse;
import com.daoninhthai.aichatbot.security.CustomUserDetails;
import com.daoninhthai.aichatbot.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Upload and manage documents for RAG")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a document for RAG indexing")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.uploadDocument(file, userDetails.toUser()));
    }

    @GetMapping
    @Operation(summary = "List all documents for the current user")
    public ResponseEntity<List<DocumentResponse>> listDocuments(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(documentService.getUserDocuments(userDetails.toUser()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document details")
    public ResponseEntity<DocumentResponse> getDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(documentService.getDocument(id, userDetails.toUser()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a document and its vector embeddings")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        documentService.deleteDocument(id, userDetails.toUser());
        return ResponseEntity.noContent().build();
    }
}
