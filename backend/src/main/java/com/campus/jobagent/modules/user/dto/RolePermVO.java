package com.campus.jobagent.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 角色及其拥有的权限码（角色-权限矩阵的一行）。
 */
@Schema(description = "角色及其拥有的权限码")
public record RolePermVO(
        @Schema(description = "角色编码", example = "STUDENT")
        String roleCode,

        @Schema(description = "角色名称", example = "学生")
        String roleName,

        @Schema(description = "该角色拥有的权限码列表，如 [job:view, resume:view]")
        List<String> permissions
) {
}
