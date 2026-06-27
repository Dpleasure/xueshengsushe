package com.dormitory.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Data
@Component
public class DeepSeekProperties {
    private static final Logger log = LoggerFactory.getLogger(DeepSeekProperties.class);

    @Value("${deepseek.api-key:${DEEPSEEK_API_KEY:}}")
    private String apiKey;

    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${deepseek.model:deepseek-v4-flash}")
    private String model;

    @Value("${deepseek.timeout-seconds:30}")
    private int timeoutSeconds;

    @PostConstruct
    public void loadLocalApiKeyIfNeeded() {
        if (StringUtils.hasText(apiKey)) {
            log.info("DeepSeek API Key loaded.");
            return;
        }

        for (Path path : List.of(Path.of(".env.local"), Path.of("backend", ".env.local"))) {
            String localKey = readApiKey(path);
            if (StringUtils.hasText(localKey)) {
                apiKey = localKey;
                log.info("DeepSeek API Key loaded from {}.", path.toAbsolutePath());
                return;
            }
        }
        log.warn("DeepSeek API Key is not configured. Set DEEPSEEK_API_KEY or create backend/.env.local.");
    }

    private String readApiKey(Path path) {
        if (!Files.isRegularFile(path)) {
            return "";
        }
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.startsWith("DEEPSEEK_API_KEY=")) {
                    return trimmed.substring("DEEPSEEK_API_KEY=".length()).trim();
                }
            }
        } catch (IOException ignored) {
            return "";
        }
        return "";
    }
}
