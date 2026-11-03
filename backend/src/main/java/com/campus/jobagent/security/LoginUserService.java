package com.campus.jobagent.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import com.campus.jobagent.modules.user.entity.SysUser;
import com.campus.jobagent.modules.user.mapper.SysPermissionMapper;
import com.campus.jobagent.modules.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 从数据库加载登录用户，同时加载该用户的细粒度权限列表。
 */
@Service
@RequiredArgsConstructor
public class LoginUserService implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysPermissionMapper sysPermissionMapper;   // ← 新加：用来查权限

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, username)
                .last("limit 1"));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return toLoginUser(user);
    }

    /** 按用户 ID 加载（JWT 过滤器每次请求都会调）。 */
    public LoginUser loadUserById(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw BizException.of(ResultCode.USER_NOT_FOUND);
        }
        return toLoginUser(user);
    }

    /**
     * 把 SysUser 转成 Spring Security 用的 LoginUser。
     * 关键改动：多查一次权限表，把权限列表塞进去。
     */
    public LoginUser toLoginUser(SysUser user) {
        // 根据用户ID查他有哪些权限（job:view、job:add...）
        List<String> permissions;
        try {
            permissions = sysPermissionMapper.findPermCodesByUserId(user.getId());
        } catch (Exception e) {
            // 表还没建好时给个空列表，避免启动报错
            permissions = new ArrayList<>();
        }
        return new LoginUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRoleCode(),
                user.getRealName(),
                permissions,    // ← 第6个参数：权限列表
                user.getStatus() != null && user.getStatus() == 1
        );
    }
}
