package com.daoninhthai.aichatbot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for multiple AI model providers.
 * Allows configuring API keys and base URLs for OpenAI, Anthropic, and Ollama.
 */
@Configuration
@ConfigurationProperties(prefix = "app.ai")
@Getter
@Setter
public class ModelConfig {

    private String defaultProvider = "openai";
    private String defaultModel = "gpt-4o-mini";

    private OpenAiProperties openai = new OpenAiProperties();
    private AnthropicProperties anthropic = new AnthropicProperties();
    private OllamaProperties ollama = new OllamaProperties();

    /**
     * Additional provider-specific configurations.
     */
    private Map<String, Map<String, String>> customProviders = new HashMap<>();

    @Getter
    @Setter
    public static class OpenAiProperties {
        private String apiKey;
        private String baseUrl = "https://api.openai.com";
        private boolean enabled = true;
        private double temperature = 0.7;
        private int maxTokens = 4096;
    }

    @Getter
    @Setter
    public static class AnthropicProperties {
        private String apiKey;
        private String baseUrl = "https://api.anthropic.com";
        private boolean enabled = false;
        private double temperature = 0.7;
        private int maxTokens = 4096;
    }

    @Getter
    @Setter
    public static class OllamaProperties {
        private String baseUrl = "http://localhost:11434";
        private boolean enabled = false;
        private double temperature = 0.7;
        private int maxTokens = 4096;
    }
}
