package com.campus.jobagent.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 注册请求。
 */
@Schema(description = "注册请求")
public record RegisterRequest(
        @Schema(description = "登录账号", example = "2024010101")
        @NotBlank(message = "登录账号不能为空")
        @Size(min = 4, max = 32, message = "登录账号长度需在 4-32 位之间")
        String username,

        @Schema(description = "登录密码", example = "123456")
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "密码长度需在 6-32 位之间")
        String password,

        @Schema(description = "真实姓名", example = "张三")
        @NotBlank(message = "姓名不能为空")
        String realName,

        @Schema(description = "角色编码：STUDENT / COUNSELOR / HR / COLLEGE_ADMIN", example = "STUDENT")
        @NotBlank(message = "角色不能为空")
        @Pattern(regexp = "STUDENT|COUNSELOR|HR|COLLEGE_ADMIN", message = "角色编码不合法")
        String roleCode,

        @Schema(description = "手机号")
        String phone,

        @Schema(description = "邮箱")
        String email
) {
}
