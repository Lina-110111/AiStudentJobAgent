package com.campus.jobagent.agent;

import com.campus.jobagent.agent.LlmModels.ChatRequest;
import com.campus.jobagent.agent.LlmModels.ChatResponse;

/**
 * 大模型客户端抽象：业务代码只依赖本接口，便于切换 DeepSeek / 通义千问 / 智谱 / 本地 Ollama。
 */
public interface LlmClient {

    /** 发起一次对话补全。 */
    ChatResponse chat(ChatRequest request);

    /** 服务商标识，如 deepseek / ollama。 */
    String provider();
}
