package com.campus.jobagent.modules.user.controller;

import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.modules.user.dto.RolePermVO;
import com.campus.jobagent.modules.user.service.RbacService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * RBAC 角色-权限矩阵接口。
 *
 * <p>只有拥有 {@code user:manage} 权限的角色（院系管理员）能调用，
 * 其他角色请求会被方法级安全直接拦成 403。
 */
@Tag(name = "权限矩阵", description = "角色与权限的对应关系")
@RestController
@RequestMapping("/api/rbac")
@RequiredArgsConstructor
public class RbacController {

    private final RbacService rbacService;

    @Operation(summary = "角色-权限矩阵")
    @GetMapping("/matrix")
    @PreAuthorize("hasAuthority('user:manage')")
    public Result<List<RolePermVO>> matrix() {
        return Result.ok(rbacService.rolePermissionMatrix());
    }
}
