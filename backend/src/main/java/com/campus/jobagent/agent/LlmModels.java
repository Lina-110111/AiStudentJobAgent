package com.campus.jobagent.agent;

import java.util.List;

/**
 * 大模型（OpenAI 兼容协议）的请求 / 响应模型。
 *
 * <p>脚手架精简考虑：三个小模型集中在一个文件里，用嵌套 record 表达，避免文件过多。
 */
public final class LlmModels {

    private LlmModels() {
    }

    /** 一条会话消息 */
    public record ChatMessage(String role, String content) {

        public static ChatMessage system(String content) {
            return new ChatMessage("system", content);
        }

        public static ChatMessage user(String content) {
            return new ChatMessage("user", content);
        }
    }

    /** 对话补全请求 */
    public record ChatRequest(String model, List<ChatMessage> messages,
                              double temperature, int maxTokens, boolean jsonMode) {
    }

    /** 对话补全响应 */
    public record ChatResponse(String content, String model, Integer promptTokens,
                               Integer completionTokens, boolean success, String errorMessage) {

        public static ChatResponse ok(String content, String model, Integer promptTokens, Integer completionTokens) {
            return new ChatResponse(content, model, promptTokens, completionTokens, true, null);
        }

        public static ChatResponse fail(String errorMessage) {
            return new ChatResponse(null, null, null, null, false, errorMessage);
        }
    }
}
