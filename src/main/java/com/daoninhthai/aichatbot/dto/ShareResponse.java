package com.daoninhthai.aichatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for conversation sharing operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareResponse {

    private String shareUrl;
    private LocalDateTime expiresAt;
    private String token;
}
