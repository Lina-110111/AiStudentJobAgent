package com.campus.jobagent.agent;

import com.campus.jobagent.agent.model.LlmChatRequest;
import com.campus.jobagent.agent.model.LlmChatResponse;

/**
 * 大模型客户端抽象。
 *
 * <p>实现可对接任意“OpenAI 兼容”服务：DeepSeek、通义千问、智谱、以及本地
 * Ollama / vLLM 部署的模型。业务代码只依赖本接口，便于答辩时说明可替换性。
 */
public interface LlmClient {

    /** 发起一次对话补全。 */
    LlmChatResponse chat(LlmChatRequest request);

    /** 服务商标识，如 deepseek / ollama。 */
    String provider();
}
