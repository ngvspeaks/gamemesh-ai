package com.gamemesh.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Optional;

@Configuration
public class AiConfig {

    @Value("${gamemesh.ai.provider:fallback}")
    private String configuredProvider;

    /**
     * Resolves multiple ChatModel candidates (e.g. OpenAI vs Ollama) by selecting a primary model
     * according to the configured provider property.
     */
    @Bean
    @Primary
    @ConditionalOnBean(ChatModel.class)
    public ChatModel primaryChatModel(List<ChatModel> chatModels) {
        if (chatModels == null || chatModels.isEmpty()) {
            return null;
        }
        for (ChatModel model : chatModels) {
            String name = model.getClass().getSimpleName().toLowerCase();
            if ("openai".equalsIgnoreCase(configuredProvider) && name.contains("openai")) {
                return model;
            }
            if ("ollama".equalsIgnoreCase(configuredProvider) && name.contains("ollama")) {
                return model;
            }
        }
        return chatModels.get(0);
    }

    /**
     * Creates an Optional ChatClient by accepting multiple ChatModel beans to avoid ambiguity.
     */
    @Bean
    public Optional<ChatClient> chatClient(List<ChatModel> chatModels) {
        if (chatModels != null && !chatModels.isEmpty() && !"fallback".equalsIgnoreCase(configuredProvider)) {
            ChatModel selectedModel = chatModels.stream()
                    .filter(m -> {
                        String name = m.getClass().getSimpleName().toLowerCase();
                        if ("openai".equalsIgnoreCase(configuredProvider)) return name.contains("openai");
                        if ("ollama".equalsIgnoreCase(configuredProvider)) return name.contains("ollama");
                        return true;
                    })
                    .findFirst()
                    .orElse(chatModels.get(0));

            return Optional.of(ChatClient.builder(selectedModel).build());
        }
        return Optional.empty();
    }
}
