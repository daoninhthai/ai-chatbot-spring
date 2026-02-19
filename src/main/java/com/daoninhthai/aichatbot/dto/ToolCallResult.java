package com.daoninhthai.aichatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO containing the result of a tool execution.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCallResult {

    private String toolName;
    private String input;
    private String output;
    private long executionTimeMs;
}
