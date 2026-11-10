package com.campus.jobagent.modules.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysPermissionMapper {

    /**
     * 根据用户ID，联查5张RBAC表，返回该用户拥有的所有权限编码。
     * 比如 ["job:view", "resume:view", "internship:apply"]
     */
    @Select("""
            SELECT DISTINCT p.perm_code
            FROM sys_permission p
            JOIN sys_role_permission rp ON p.id = rp.perm_id
            JOIN sys_user_role ur ON rp.role_id = ur.role_id
            WHERE ur.user_id = #{userId} AND p.status = 1
            """)
    List<String> findPermCodesByUserId(Long userId);

    /**
     * 查询全部角色及其权限码（角色-权限矩阵）。
     * 返回列：roleCode / roleName / permCode。
     */
    @Select("""
            SELECT r.role_code AS roleCode, r.role_name AS roleName, p.perm_code AS permCode
            FROM sys_role r
            LEFT JOIN sys_role_permission rp ON rp.role_id = r.id
            LEFT JOIN sys_permission p ON p.id = rp.perm_id AND p.status = 1
            WHERE r.status = 1
            ORDER BY r.id, p.id
            """)
    List<Map<String, Object>> findRolePermRows();
}
