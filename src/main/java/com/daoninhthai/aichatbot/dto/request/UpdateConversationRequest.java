package com.daoninhthai.aichatbot.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for updating conversation properties like title, pin status, etc.
 */
@Data
public class UpdateConversationRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String systemPrompt;

    private Boolean ragEnabled;

    private Boolean pinned;
}
