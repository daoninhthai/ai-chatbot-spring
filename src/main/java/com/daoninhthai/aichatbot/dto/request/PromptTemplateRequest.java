package com.daoninhthai.aichatbot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for creating or updating a prompt template.
 */
@Data
public class PromptTemplateRequest {

    @NotBlank(message = "Template name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    private String description;

    @NotBlank(message = "Template content is required")
    private String content;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    private List<String> variables = new ArrayList<>();

    private boolean isPublic = false;
}
