package com.campus.jobagent.agent.model;

/**
 * OpenAI 兼容协议中的一条会话消息。
 */
public record LlmChatMessage(String role, String content) {

    public static LlmChatMessage system(String content) {
        return new LlmChatMessage("system", content);
    }

    public static LlmChatMessage user(String content) {
        return new LlmChatMessage("user", content);
    }

    public static LlmChatMessage assistant(String content) {
        return new LlmChatMessage("assistant", content);
    }
}
