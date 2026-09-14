package com.campus.jobagent.modules.auth.dto;

import com.campus.jobagent.modules.user.dto.UserVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 认证模块的请求 / 响应对象。
 *
 * <p>脚手架精简考虑：登录、注册、登录响应三个小对象集中在一个文件里。
 */
public final class AuthDtos {

    private AuthDtos() {
    }

    @Schema(description = "登录请求")
    public record LoginRequest(
            @Schema(description = "登录账号", example = "student01")
            @NotBlank(message = "登录账号不能为空")
            String username,

            @Schema(description = "登录密码", example = "123456")
            @NotBlank(message = "密码不能为空")
            String password) {
    }

    @Schema(description = "注册请求")
    public record RegisterRequest(
            @NotBlank(message = "登录账号不能为空")
            @Size(min = 4, max = 32, message = "登录账号长度需在 4-32 位之间")
            String username,

            @NotBlank(message = "密码不能为空")
            @Size(min = 6, max = 32, message = "密码长度需在 6-32 位之间")
            String password,

            @NotBlank(message = "姓名不能为空")
            String realName,

            @Schema(description = "STUDENT / COUNSELOR / HR / COLLEGE_ADMIN", example = "STUDENT")
            @NotBlank(message = "角色不能为空")
            @Pattern(regexp = "STUDENT|COUNSELOR|HR|COLLEGE_ADMIN", message = "角色编码不合法")
            String roleCode,

            String phone,

            String email) {
    }

    @Schema(description = "登录响应")
    public record LoginResponse(
            @Schema(description = "JWT 令牌，请求时放入 Authorization: Bearer <token>")
            String token,

            @Schema(description = "令牌有效期（毫秒）")
            long expiresIn,

            UserVO user) {
    }
}
