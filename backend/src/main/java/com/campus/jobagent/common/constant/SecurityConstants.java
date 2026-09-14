package com.campus.jobagent.common.constant;

/**
 * 安全相关常量。
 */
public final class SecurityConstants {

    /** 认证请求头 */
    public static final String AUTH_HEADER = "Authorization";

    /** 令牌前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** JWT 中存放用户 ID 的自定义声明 */
    public static final String CLAIM_USER_ID = "uid";

    /** JWT 中存放角色编码的自定义声明 */
    public static final String CLAIM_ROLE = "role";

    /** 免登录白名单 */
    public static final String[] WHITE_LIST = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/captcha",
            "/api/policy/public/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health",
            "/ws/**"
    };

    private SecurityConstants() {
    }
}
