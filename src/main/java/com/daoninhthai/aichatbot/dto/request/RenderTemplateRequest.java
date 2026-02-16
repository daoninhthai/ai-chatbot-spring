package com.daoninhthai.aichatbot.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Request DTO for rendering a prompt template with variable values.
 */
@Data
public class RenderTemplateRequest {

    @NotNull(message = "Variables map is required")
    private Map<String, String> variables = new HashMap<>();
}
