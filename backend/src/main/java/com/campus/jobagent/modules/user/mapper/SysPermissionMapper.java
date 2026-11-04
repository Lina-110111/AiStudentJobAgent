package com.campus.jobagent.modules.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
}
