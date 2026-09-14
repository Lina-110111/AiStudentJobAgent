package com.campus.jobagent.agent.impl;

import com.campus.jobagent.agent.LlmClient;
import com.campus.jobagent.agent.model.LlmChatMessage;
import com.campus.jobagent.agent.model.LlmChatRequest;
import com.campus.jobagent.agent.model.LlmChatResponse;
import com.campus.jobagent.config.AiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于“OpenAI 兼容协议”的大模型客户端。
 *
 * <p>当 {@code app.ai.enabled=true} 时生效；同一份代码可切换 DeepSeek / 通义千问 /
 * 智谱 / 本地 Ollama，只需改配置，不改业务代码。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.ai", name = "enabled", havingValue = "true")
public class OpenAiCompatibleLlmClient implements LlmClient {

    private final AiProperties properties;
    private final RestClient restClient;

    public OpenAiCompatibleLlmClient(AiProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.getTimeout().toMillis());
        factory.setReadTimeout((int) properties.getTimeout().toMillis());
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        if (StringUtils.hasText(properties.getApiKey())) {
            builder.defaultHeader("Authorization", "Bearer " + properties.getApiKey());
        }
        this.restClient = builder.build();
    }

    @Override
    public LlmChatResponse chat(LlmChatRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", request.model());
        payload.put("temperature", request.temperature());
        payload.put("max_tokens", request.maxTokens());
        if (request.jsonMode()) {
            payload.put("response_format", Map.of("type", "json_object"));
        }
        List<Map<String, String>> messages = new ArrayList<>();
        for (LlmChatMessage message : request.messages()) {
            messages.add(Map.of("role", message.role(), "content", message.content()));
        }
        payload.put("messages", messages);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restClient.post()
                    .uri(properties.getChatPath())
                    .body(payload)
                    .retrieve()
                    .body(Map.class);
            return parse(body);
        } catch (Exception e) {
            log.error("调用大模型失败：provider={}, model={}", properties.getProvider(), properties.getModel(), e);
            return LlmChatResponse.fail(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private LlmChatResponse parse(Map<String, Object> body) {
        if (body == null) {
            return LlmChatResponse.fail("大模型返回内容为空");
        }
        List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
        if (choices == null || choices.isEmpty()) {
            return LlmChatResponse.fail("大模型返回结果中不含 choices");
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = message == null ? null : String.valueOf(message.get("content"));
        Map<String, Object> usage = (Map<String, Object>) body.get("usage");
        Integer promptTokens = null;
        Integer completionTokens = null;
        if (usage != null) {
            promptTokens = toInt(usage.get("prompt_tokens"));
            completionTokens = toInt(usage.get("completion_tokens"));
        }
        return LlmChatResponse.ok(content, String.valueOf(body.getOrDefault("model", properties.getModel())),
                promptTokens, completionTokens);
    }

    private Integer toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    @Override
    public String provider() {
        return properties.getProvider();
    }

    /** 供健康检查/调试使用：当前是否配置了可用的 Key（本地模型允许为空）。 */
    public Map<String, Object> describe() {
        Map<String, Object> info = new HashMap<>();
        info.put("provider", properties.getProvider());
        info.put("model", properties.getModel());
        info.put("baseUrl", properties.getBaseUrl());
        info.put("apiKeyConfigured", StringUtils.hasText(properties.getApiKey()));
        return info;
    }
}
