package com.campus.jobagent.modules.dashboard.controller;

import com.campus.jobagent.agent.AgentOrchestrator;
import com.campus.jobagent.agent.LlmClient;
import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.config.AiProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AI 能力状态接口：用于演示时说明“当前走的是大模型还是本地规则引擎”。
 */
@Tag(name = "AI 能力状态", description = "查看智能体当前使用的模型与降级状态")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiStatusController {

    private final AiProperties aiProperties;
    private final ObjectProvider<LlmClient> llmClientProvider;

    @Operation(summary = "查看 AI 服务状态")
    @GetMapping("/status")
    public Result<Map<String, Object>> status() {
        LlmClient client = llmClientProvider.getIfAvailable();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("aiEnabled", aiProperties.isEnabled());
        data.put("provider", client == null ? "local-rule" : client.provider());
        data.put("model", aiProperties.getModel());
        data.put("baseUrl", aiProperties.getBaseUrl());
        data.put("degraded", client == null);
        data.put("scenes", java.util.Arrays.stream(AgentOrchestrator.class.getDeclaredMethods())
                .filter(m -> "run".equals(m.getName()))
                .count());
        data.put("message", client == null
                ? "当前未配置大模型，智能体运行在本地规则引擎模式（演示与离线可用）"
                : "当前已接入大模型服务：" + client.provider());
        return Result.ok(data);
    }
}
