package com.campus.jobagent.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求。
 */
@Schema(description = "登录请求")
public record LoginRequest(
        @Schema(description = "登录账号", example = "student01")
        @NotBlank(message = "登录账号不能为空")
        String username,

        @Schema(description = "登录密码", example = "123456")
        @NotBlank(message = "密码不能为空")
        String password
) {
}
