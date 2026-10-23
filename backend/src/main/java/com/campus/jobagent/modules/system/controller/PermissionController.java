package com.campus.jobagent.modules.system.controller;

import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.common.constant.RoleCode;
import com.campus.jobagent.common.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 权限接口：查看当前登录用户的角色与权限范围。
 *
 * <p>对齐《需求分析文档 v2.2》FR-1「查看自身权限」。角色来源为 sys_user.role_code（{@link RoleCode}）。
 * 接口级鉴权由 Spring Security 的 {@code @PreAuthorize("hasRole('XXX')")} 承担（见 SecurityConfig），
 * 数据范围隔离在 Service/Mapper 层按角色拼接查询条件完成。</p>
 */
@Tag(name = "权限管理", description = "当前用户角色与权限")
@RestController
@RequestMapping("/api/system")
public class PermissionController {

    /** 角色编码 -> 数据范围（文档 5.2 角色职责与权限表） */
    private static final Map<String, String> DATA_SCOPE = new HashMap<>();

    static {
        DATA_SCOPE.put(RoleCode.STUDENT.getCode(), "仅本人数据");
        DATA_SCOPE.put(RoleCode.COUNSELOR.getCode(), "所辖班级数据");
        DATA_SCOPE.put(RoleCode.HR.getCode(), "本企业岗位与相关申请");
        DATA_SCOPE.put(RoleCode.COLLEGE_ADMIN.getCode(), "全院数据");
    }

    @Operation(summary = "查看当前登录用户的角色与权限范围")
    @GetMapping("/me/permissions")
    public Result<Map<String, String>> currentPermissions() {
        String roleCode = SecurityUtils.getRoleCode();
        Map<String, String> data = new HashMap<>();
        data.put("roleCode", roleCode);
        data.put("roleName", resolveRoleName(roleCode));
        data.put("dataScope", DATA_SCOPE.getOrDefault(roleCode, "未知"));
        return Result.ok(data);
    }

    private String resolveRoleName(String roleCode) {
        for (RoleCode rc : RoleCode.values()) {
            if (rc.getCode().equals(roleCode)) {
                return rc.getLabel();
            }
        }
        return roleCode;
    }
}
