package com.campus.jobagent.agent;

import com.campus.jobagent.agent.LlmModels.ChatMessage;
import com.campus.jobagent.agent.LlmModels.ChatRequest;
import com.campus.jobagent.agent.LlmModels.ChatResponse;
import com.campus.jobagent.config.AiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于"OpenAI 兼容协议"的大模型客户端。
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
    public ChatResponse chat(ChatRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", request.model());
        payload.put("temperature", request.temperature());
        payload.put("max_tokens", request.maxTokens());
        if (request.jsonMode()) {
            payload.put("response_format", Map.of("type", "json_object"));
        }
        List<Map<String, String>> messages = new ArrayList<>();
        for (ChatMessage message : request.messages()) {
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
            return ChatResponse.fail(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private ChatResponse parse(Map<String, Object> body) {
        if (body == null) {
            return ChatResponse.fail("大模型返回内容为空");
        }
        List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
        if (choices == null || choices.isEmpty()) {
            return ChatResponse.fail("大模型返回结果中不含 choices");
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = message == null ? null : String.valueOf(message.get("content"));
        Map<String, Object> usage = (Map<String, Object>) body.get("usage");
        Integer promptTokens = usage == null ? null : toInt(usage.get("prompt_tokens"));
        Integer completionTokens = usage == null ? null : toInt(usage.get("completion_tokens"));
        return ChatResponse.ok(content, String.valueOf(body.getOrDefault("model", properties.getModel())),
                promptTokens, completionTokens);
    }

    private Integer toInt(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    @Override
    public String provider() {
        return properties.getProvider();
    }
}
