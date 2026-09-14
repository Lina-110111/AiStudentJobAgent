package com.campus.jobagent.agent.model;

import java.util.List;

/**
 * 大模型对话请求。
 */
public record LlmChatRequest(
        String model,
        List<LlmChatMessage> messages,
        double temperature,
        int maxTokens,
        boolean jsonMode
) {
}
