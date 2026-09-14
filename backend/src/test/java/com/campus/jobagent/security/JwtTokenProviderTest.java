package com.campus.jobagent.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JWT 工具单元测试（不依赖 Spring 容器与数据库）。
 */
class JwtTokenProviderTest {

    private static final String SECRET = "unit-test-secret-key-must-be-at-least-32-bytes-long";

    private final JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60, "job-agent");

    @Test
    @DisplayName("签发的令牌可以正确解析出用户ID与角色")
    void shouldParseClaimsFromGeneratedToken() {
        LoginUser user = new LoginUser(1001L, "student01", "pwd", "STUDENT", "张三", true);
        String token = provider.generateToken(user);

        assertTrue(provider.validate(token));
        assertEquals(1001L, provider.getUserId(token));
        assertEquals("STUDENT", provider.getRoleCode(token));
    }

    @Test
    @DisplayName("被篡改的令牌应校验失败")
    void shouldRejectTamperedToken() {
        LoginUser user = new LoginUser(1002L, "hr01", "pwd", "HR", "李四", true);
        String token = provider.generateToken(user);

        assertFalse(provider.validate(token + "tampered"));
        assertFalse(provider.validate("not-a-jwt"));
    }
}
