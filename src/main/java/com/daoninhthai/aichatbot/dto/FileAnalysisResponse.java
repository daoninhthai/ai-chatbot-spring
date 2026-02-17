package com.daoninhthai.aichatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO containing the results of a file analysis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileAnalysisResponse {

    private String analysis;
    private String extractedText;
    private double confidence;
    private Map<String, Object> metadata;
    private String fileName;
    private String contentType;
    private long fileSize;
    private String analysisType;
    private long processingTimeMs;
}
