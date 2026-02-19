package com.daoninhthai.aichatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a tool definition that can be registered for AI function calling.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolDefinition {

    private String name;
    private String description;
    private String parametersSchema;
}
