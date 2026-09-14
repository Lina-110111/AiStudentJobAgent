package com.campus.jobagent.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 登录用户主体：认证成功后放入 SecurityContext，供业务代码读取。
 *
 * <p>角色使用 Spring Security 约定前缀 {@code ROLE_}，例如 {@code ROLE_STUDENT}，
 * 便于在接口上使用 {@code @PreAuthorize("hasRole('STUDENT')")}。
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

    private final boolean enabled;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + roleCode));
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
