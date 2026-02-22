package com.daoninhthai.aichatbot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultSystem("""
                        You are a helpful and knowledgeable AI assistant. Your responses should be:
                        - Clear, accurate, and well-structured
                        - Formatted with markdown when helpful (code blocks, lists, headers)
                        - Honest about limitations — say "I'm not sure" rather than guessing
                        - Concise but thorough — provide enough detail without being verbose

                        When answering technical questions, include code examples when relevant.
                        When the user provides documents for context, base your answers on that content.
                        """)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }
}
