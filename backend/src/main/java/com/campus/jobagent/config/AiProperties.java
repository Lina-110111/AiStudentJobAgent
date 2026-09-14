package com.campus.jobagent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 大模型服务配置（对应题目“支持本地大模型服务部署或调用云端大模型服务”）。
 *
 * <p>配置示例（application-dev.yml）：
 * <pre>
 * app:
 *   ai:
 *     enabled: true
 *     provider: deepseek
 *     base-url: https://api.deepseek.com
 *     api-key: ${AI_API_KEY:}
 *     model: deepseek-chat
 * </pre>
 *
 * <p>当 {@code enabled=false} 或未配置 api-key 时，系统自动降级为本地规则引擎
 * （{@code MockLlmClient}），保证无外网环境也能完整演示。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    /** 是否启用真实大模型；false 时使用内置规则引擎 */
    private boolean enabled = false;

    /** 服务商标识：deepseek / openai / qwen / ollama(本地) */
    private String provider = "deepseek";

    /** OpenAI 兼容的接口地址 */
    private String baseUrl = "https://api.deepseek.com";

    /** 对话补全的接口路径（OpenAI 兼容协议） */
    private String chatPath = "/v1/chat/completions";

    /** API Key；本地 ollama 可留空 */
    private String apiKey = "";

    /** 模型名称 */
    private String model = "deepseek-chat";

    /** 单次调用超时时间 */
    private Duration timeout = Duration.ofSeconds(60);

    /** 采样温度 */
    private double temperature = 0.3D;

    /** 最大生成 token 数 */
    private int maxTokens = 2048;
}
