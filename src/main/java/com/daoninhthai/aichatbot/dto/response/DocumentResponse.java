package com.daoninhthai.aichatbot.dto.response;

import com.daoninhthai.aichatbot.entity.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class DocumentResponse {
    private Long id;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private Integer chunkCount;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;

    public static DocumentResponse from(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .contentType(document.getContentType())
                .fileSize(document.getFileSize())
                .chunkCount(document.getChunkCount())
                .status(document.getStatus().name())
                .errorMessage(document.getErrorMessage())
                .createdAt(document.getCreatedAt())
                .build();
    }
}
