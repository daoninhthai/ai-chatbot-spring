package com.daoninhthai.aichatbot.config;

import com.daoninhthai.aichatbot.service.FunctionCallingService;
import com.daoninhthai.aichatbot.service.tools.CalculatorTool;
import com.daoninhthai.aichatbot.service.tools.DateTimeTool;
import com.daoninhthai.aichatbot.service.tools.UrlSummaryTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for registering built-in AI tools (function calling).
 */
@Slf4j
@Configuration
public class ToolConfig {

    @Bean
    public CommandLineRunner registerTools(FunctionCallingService functionCallingService,
                                           CalculatorTool calculatorTool,
                                           DateTimeTool dateTimeTool,
                                           UrlSummaryTool urlSummaryTool) {
        return args -> {
            functionCallingService.registerTool(
                    "calculator",
                    "Evaluate mathematical expressions. Supports +, -, *, / and parentheses.",
                    "{\"type\":\"object\",\"properties\":{\"expression\":{\"type\":\"string\"}}}",
                    calculatorTool
            );

            functionCallingService.registerTool(
                    "datetime",
                    "Get current date/time or convert between time zones.",
                    "{\"type\":\"object\",\"properties\":{\"timezone\":{\"type\":\"string\"}}}",
                    dateTimeTool
            );

            functionCallingService.registerTool(
                    "url_summary",
                    "Get a summary of content from a URL.",
                    "{\"type\":\"object\",\"properties\":{\"url\":{\"type\":\"string\"}}}",
                    urlSummaryTool
            );

            log.info("Registered {} built-in tools", functionCallingService.getAvailableTools().size());
        };
    }
}
