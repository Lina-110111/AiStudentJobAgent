package com.campus.jobagent.agent;

import com.campus.jobagent.agent.LlmModels.ChatMessage;
import com.campus.jobagent.agent.LlmModels.ChatRequest;
import com.campus.jobagent.agent.LlmModels.ChatResponse;
import com.campus.jobagent.agent.prompt.PromptLibrary;
import com.campus.jobagent.common.util.JsonUtils;
import com.campus.jobagent.config.AiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能体调度器：统一对外提供"场景 + 业务数据 → 结构化结果"的能力。
 *
 * <p>调用链：业务 Service → AgentOrchestrator →（大模型 | 本地规则引擎）→ 结构化 Map。
 * 大模型不可用时自动降级，保证接口始终有可用返回（断网也能演示）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentOrchestrator {

    private final PromptLibrary promptLibrary;
    private final LocalRuleEngine localRuleEngine;
    private final AiProperties aiProperties;
    private final ObjectProvider<LlmClient> llmClientProvider;

    /** 执行一次智能体任务。 */
    public AgentResult run(AgentScene scene, String payload) {
        LlmClient client = llmClientProvider.getIfAvailable();
        if (client != null) {
            ChatRequest request = new ChatRequest(
                    aiProperties.getModel(),
                    List.of(ChatMessage.system(promptLibrary.systemPrompt(scene)),
                            ChatMessage.user("【业务数据】\n" + payload)),
                    aiProperties.getTemperature(),
                    aiProperties.getMaxTokens(),
                    true);
            ChatResponse response = client.chat(request);
            if (response.success() && response.content() != null && !response.content().isBlank()) {
                Map<String, Object> data = JsonUtils.parseMap(response.content());
                data.putIfAbsent("engine", client.provider());
                return new AgentResult(true, client.provider(), scene, data);
            }
            log.warn("大模型调用未成功（{}），降级为本地规则引擎：scene={}", response.errorMessage(), scene);
        }
        Map<String, Object> data = new LinkedHashMap<>(localRuleEngine.generate(scene, payload));
        return new AgentResult(false, "local-rule", scene, data);
    }

    /** 智能体执行结果。 */
    public record AgentResult(boolean aiGenerated, String engine, AgentScene scene, Map<String, Object> data) {
    }
}
