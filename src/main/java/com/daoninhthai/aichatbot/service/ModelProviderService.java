package com.daoninhthai.aichatbot.service;

import com.daoninhthai.aichatbot.dto.ModelInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing multiple AI model providers (OpenAI, Anthropic, Ollama).
 * Tracks available models and handles model selection per user session.
 */
@Slf4j
@Service
public class ModelProviderService {

    private final List<ModelInfo> availableModels = new ArrayList<>();
    private final Map<Long, String> userModelSelections = new ConcurrentHashMap<>();

    public ModelProviderService() {
        initializeModels();
    }

    private void initializeModels() {
        // OpenAI models
        availableModels.add(ModelInfo.builder()
                .provider("openai")
                .modelName("gpt-4o")
                .description("Most capable OpenAI model with vision support")
                .maxTokens(128000)
                .costPer1kTokens(0.005)
                .available(true)
                .build());

        availableModels.add(ModelInfo.builder()
                .provider("openai")
                .modelName("gpt-4o-mini")
                .description("Fast and cost-effective OpenAI model")
                .maxTokens(128000)
                .costPer1kTokens(0.00015)
                .available(true)
                .build());

        availableModels.add(ModelInfo.builder()
                .provider("openai")
                .modelName("gpt-3.5-turbo")
                .description("Legacy OpenAI model, good for simple tasks")
                .maxTokens(16385)
                .costPer1kTokens(0.0005)
                .available(true)
                .build());

        // Anthropic models
        availableModels.add(ModelInfo.builder()
                .provider("anthropic")
                .modelName("claude-3-opus")
                .description("Most powerful Anthropic model for complex reasoning")
                .maxTokens(200000)
                .costPer1kTokens(0.015)
                .available(true)
                .build());

        availableModels.add(ModelInfo.builder()
                .provider("anthropic")
                .modelName("claude-3-sonnet")
                .description("Balanced Anthropic model for most tasks")
                .maxTokens(200000)
                .costPer1kTokens(0.003)
                .available(true)
                .build());

        availableModels.add(ModelInfo.builder()
                .provider("anthropic")
                .modelName("claude-3-haiku")
                .description("Fast and affordable Anthropic model")
                .maxTokens(200000)
                .costPer1kTokens(0.00025)
                .available(true)
                .build());

        // Ollama (local) models
        availableModels.add(ModelInfo.builder()
                .provider("ollama")
                .modelName("llama3")
                .description("Meta's open-source LLaMA 3 model via Ollama")
                .maxTokens(8192)
                .costPer1kTokens(0.0)
                .available(false)
                .build());

        availableModels.add(ModelInfo.builder()
                .provider("ollama")
                .modelName("mistral")
                .description("Mistral 7B model via Ollama for local inference")
                .maxTokens(32768)
                .costPer1kTokens(0.0)
                .available(false)
                .build());

        availableModels.add(ModelInfo.builder()
                .provider("ollama")
                .modelName("codellama")
                .description("Code-specialized LLaMA model via Ollama")
                .maxTokens(16384)
                .costPer1kTokens(0.0)
                .available(false)
                .build());

        log.info("Initialized {} AI models across multiple providers", availableModels.size());
    }

    /**
     * Get all available models across all providers.
     */
    public List<ModelInfo> getAvailableModels() {
        return Collections.unmodifiableList(availableModels);
    }

    /**
     * Get models filtered by provider name.
     */
    public List<ModelInfo> getModelsByProvider(String provider) {
        return availableModels.stream()
                .filter(m -> m.getProvider().equalsIgnoreCase(provider))
                .toList();
    }

    /**
     * Switch the active model for a given user.
     */
    public ModelInfo switchModel(Long userId, String modelId) {
        ModelInfo model = availableModels.stream()
                .filter(m -> m.getModelId().equals(modelId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown model: " + modelId));

        if (!model.isAvailable()) {
            throw new IllegalStateException("Model " + modelId + " is not currently available");
        }

        userModelSelections.put(userId, modelId);
        log.info("User {} switched to model: {}", userId, modelId);
        return model;
    }

    /**
     * Get the currently selected model for a user, defaulting to gpt-4o-mini.
     */
    public ModelInfo getCurrentModel(Long userId) {
        String modelId = userModelSelections.getOrDefault(userId, "openai:gpt-4o-mini");
        return availableModels.stream()
                .filter(m -> m.getModelId().equals(modelId))
                .findFirst()
                .orElse(availableModels.get(1)); // fallback to gpt-4o-mini
    }

    /**
     * Get all supported provider names.
     */
    public List<String> getProviders() {
        return availableModels.stream()
                .map(ModelInfo::getProvider)
                .distinct()
                .sorted()
                .toList();
    }
}
