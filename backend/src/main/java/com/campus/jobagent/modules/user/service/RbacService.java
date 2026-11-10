package com.campus.jobagent.modules.user.service;

import com.campus.jobagent.modules.user.dto.RolePermVO;
import com.campus.jobagent.modules.user.mapper.SysPermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RBAC 查询服务：把数据库里的角色、权限关联整理成前端可直接渲染的结构。
 */
@Service
@RequiredArgsConstructor
public class RbacService {

    private final SysPermissionMapper sysPermissionMapper;

    /**
     * 角色-权限矩阵：数据库返回的是扁平的 (角色, 权限) 行，
     * 这里按角色聚合，一个角色一条记录。
     */
    public List<RolePermVO> rolePermissionMatrix() {
        Map<String, RolePermVO> grouped = new LinkedHashMap<>();
        for (Map<String, Object> row : sysPermissionMapper.findRolePermRows()) {
            String roleCode = asText(row.get("roleCode"));
            String roleName = asText(row.get("roleName"));
            String permCode = asText(row.get("permCode"));
            if (roleCode == null || roleCode.isEmpty()) {
                continue;
            }
            RolePermVO vo = grouped.computeIfAbsent(
                    roleCode, code -> new RolePermVO(code, roleName, new ArrayList<>()));
            if (permCode != null && !permCode.isEmpty() && !vo.permissions().contains(permCode)) {
                vo.permissions().add(permCode);
            }
        }
        return new ArrayList<>(grouped.values());
    }

    private String asText(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
