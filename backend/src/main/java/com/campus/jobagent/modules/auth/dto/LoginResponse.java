package com.campus.jobagent.modules.auth.dto;

import com.campus.jobagent.modules.user.dto.UserVO;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登录响应。
 */
@Schema(description = "登录响应")
public record LoginResponse(
        @Schema(description = "JWT 令牌，后续请求放入 Authorization: Bearer <token>")
        String token,

        @Schema(description = "令牌有效期（毫秒）")
        long expiresIn,

        @Schema(description = "登录用户信息")
        UserVO user
) {
}
