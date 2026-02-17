package com.daoninhthai.aichatbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for extracting text content from various file formats
 * using Apache Tika (PDF, DOCX, TXT, images, etc.).
 */
@Slf4j
@Service
public class TextExtractionService {

    /**
     * Extract text from a multipart file upload using Apache Tika.
     */
    public String extractText(MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType != null && contentType.startsWith("text/")) {
                return extractPlainText(file);
            }
            return extractWithTika(file.getResource());
        } catch (Exception e) {
            log.error("Failed to extract text from file {}: {}", file.getOriginalFilename(), e.getMessage());
            throw new RuntimeException("Text extraction failed: " + e.getMessage(), e);
        }
    }

    /**
     * Extract text from a Spring Resource using Tika.
     */
    public String extractWithTika(Resource resource) {
        try {
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            var documents = reader.get();
            return documents.stream()
                    .map(org.springframework.ai.document.Document::getContent)
                    .collect(Collectors.joining("\n\n"));
        } catch (Exception e) {
            log.error("Tika extraction failed: {}", e.getMessage());
            throw new RuntimeException("Tika extraction failed: " + e.getMessage(), e);
        }
    }

    /**
     * Extract text from a plain text file.
     */
    private String extractPlainText(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read text file: " + e.getMessage(), e);
        }
    }

    /**
     * Extract metadata from a file (size, type, name, etc.).
     */
    public Map<String, Object> extractMetadata(MultipartFile file) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("fileName", file.getOriginalFilename());
        metadata.put("contentType", file.getContentType());
        metadata.put("fileSize", file.getSize());
        metadata.put("fileSizeFormatted", formatFileSize(file.getSize()));
        return metadata;
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
