package com.campus.jobagent.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 登录用户主体：认证成功后放入 SecurityContext，供业务代码读取。
 *
 * <p>同时携带粗粒度角色（ROLE_xxx）和细粒度权限（job:view、job:add...），
 * 配合 @PreAuthorize("hasAuthority('job:add')") 使用。
 */
@Getter
@AllArgsConstructor
public class LoginUser implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    /** 角色编码：STUDENT / COUNSELOR / HR / COLLEGE_ADMIN */
    private final String roleCode;
    private final String realName;
    /** 细粒度权限列表：job:view、job:add、internship:approve 等 */
    private final List<String> permissions;
    private final boolean enabled;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        // 1. 粗粒度角色（保留原有 hasRole() 能力）
        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleCode));
        // 2. 细粒度权限（仿禅道：模块:操作）
        if (permissions != null) {
            permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
