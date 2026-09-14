package com.campus.jobagent.agent.model;

/**
 * 大模型对话响应。
 */
public record LlmChatResponse(
        String content,
        String model,
        Integer promptTokens,
        Integer completionTokens,
        boolean success,
        String errorMessage
) {

    public static LlmChatResponse ok(String content, String model, Integer promptTokens, Integer completionTokens) {
        return new LlmChatResponse(content, model, promptTokens, completionTokens, true, null);
    }

    public static LlmChatResponse fail(String errorMessage) {
        return new LlmChatResponse(null, null, null, null, false, errorMessage);
    }
}
