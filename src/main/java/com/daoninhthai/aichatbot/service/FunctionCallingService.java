package com.daoninhthai.aichatbot.service;

import com.daoninhthai.aichatbot.dto.ToolCallResult;
import com.daoninhthai.aichatbot.dto.ToolDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Service for managing and executing AI function calling tools.
 * Tools are registered as functions that take a String input and return a String output.
 */
@Slf4j
@Service
public class FunctionCallingService {

    private final Map<String, Function<String, String>> tools = new ConcurrentHashMap<>();
    private final Map<String, ToolDefinition> toolDefinitions = new ConcurrentHashMap<>();

    /**
     * Register a new tool with its name, description, and implementation.
     */
    public void registerTool(String name, String description, String parametersSchema,
                             Function<String, String> implementation) {
        tools.put(name, implementation);
        toolDefinitions.put(name, ToolDefinition.builder()
                .name(name)
                .description(description)
                .parametersSchema(parametersSchema)
                .build());
        log.info("Tool registered: {}", name);
    }

    /**
     * Execute a tool by name with the given input.
     */
    public ToolCallResult executeTool(String toolName, String input) {
        Function<String, String> tool = tools.get(toolName);
        if (tool == null) {
            log.warn("Tool not found: {}", toolName);
            return ToolCallResult.builder()
                    .toolName(toolName)
                    .input(input)
                    .output("Error: Tool '" + toolName + "' not found")
                    .executionTimeMs(0)
                    .build();
        }

        long startTime = System.currentTimeMillis();
        try {
            String output = tool.apply(input);
            long duration = System.currentTimeMillis() - startTime;
            log.debug("Tool '{}' executed in {}ms", toolName, duration);

            return ToolCallResult.builder()
                    .toolName(toolName)
                    .input(input)
                    .output(output)
                    .executionTimeMs(duration)
                    .build();
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Tool '{}' execution failed: {}", toolName, e.getMessage(), e);

            return ToolCallResult.builder()
                    .toolName(toolName)
                    .input(input)
                    .output("Error executing tool: " + e.getMessage())
                    .executionTimeMs(duration)
                    .build();
        }
    }

    /**
     * Get all available tool definitions.
     */
    public List<ToolDefinition> getAvailableTools() {
        return new ArrayList<>(toolDefinitions.values());
    }

    /**
     * Check if a tool is registered.
     */
    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }
}
