package com.daoninhthai.aichatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for file analysis operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileAnalysisRequest {

    private String question;
    private AnalysisType analysisType;
    private String targetLanguage; // for TRANSLATE type

    public enum AnalysisType {
        SUMMARIZE,
        EXTRACT,
        QA,
        TRANSLATE
    }
}
