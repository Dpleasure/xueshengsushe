package com.dormitory.service;

import com.dormitory.config.DeepSeekProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class DeepSeekService {
    private static final String SYSTEM_PROMPT = """
            你是学生宿舍管理系统的智能助手。只回答宿舍管理、入住信息、报修、来访登记、换宿申请、个人资料和系统使用相关问题。
            如果用户询问无关内容，请礼貌说明只能协助宿舍管理系统相关事项。回答要简洁、清晰、可操作。
            不要声称你可以直接读取数据库或替用户提交申请；需要操作时，引导用户到系统对应页面完成。
            """;

    private final DeepSeekProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public DeepSeekService(DeepSeekProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofSeconds(Math.max(1, properties.getTimeoutSeconds()));
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);

        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    public Map<String, String> chat(String message) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new DeepSeekException("DeepSeek API Key 未配置，请先设置环境变量 DEEPSEEK_API_KEY。");
        }

        Map<String, Object> requestBody = Map.of(
                "model", properties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", message)
                ),
                "thinking", Map.of("type", "disabled"),
                "temperature", 0.3,
                "max_tokens", 1000
        );

        try {
            String responseBody = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            String answer = parseAnswer(responseBody);
            return Map.of("answer", answer, "model", properties.getModel());
        } catch (DeepSeekException e) {
            throw e;
        } catch (RestClientResponseException e) {
            throw new DeepSeekException(buildDeepSeekErrorMessage(e), e);
        } catch (ResourceAccessException e) {
            throw new DeepSeekException("连接 DeepSeek 超时或网络不可用，请稍后重试。", e);
        } catch (Exception e) {
            throw new DeepSeekException("DeepSeek 响应解析失败，请稍后重试。", e);
        }
    }

    private String parseAnswer(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        if (!contentNode.isTextual() || !StringUtils.hasText(contentNode.asText())) {
            throw new DeepSeekException("DeepSeek 未返回有效回答，请稍后重试。");
        }
        return contentNode.asText().trim();
    }

    private String buildDeepSeekErrorMessage(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        String detail = extractDeepSeekDetail(e.getResponseBodyAsString());

        if (status == 401) {
            return "DeepSeek API Key 无效或未授权，请检查 DEEPSEEK_API_KEY。";
        }
        if (status == 429) {
            return "DeepSeek 请求过于频繁或额度不足，请稍后重试。";
        }
        if (status >= 500) {
            return "DeepSeek 服务暂时不可用，请稍后重试。";
        }
        if (StringUtils.hasText(detail)) {
            return "DeepSeek 请求失败：" + detail;
        }
        return "DeepSeek 请求失败，状态码：" + status;
    }

    private String extractDeepSeekDetail(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode message = root.path("error").path("message");
            if (message.isTextual()) {
                return message.asText();
            }
        } catch (Exception ignored) {
            return "";
        }
        return "";
    }

    public static class DeepSeekException extends RuntimeException {
        public DeepSeekException(String message) {
            super(message);
        }

        public DeepSeekException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
