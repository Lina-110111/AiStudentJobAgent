package com.campus.jobagent.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import com.campus.jobagent.modules.user.entity.SysUser;
import com.campus.jobagent.modules.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 从数据库加载登录用户。
 */
@Service
@RequiredArgsConstructor
public class LoginUserService implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

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

    /** 按用户 ID 加载（JWT 过滤器使用）。 */
    public LoginUser loadUserById(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw BizException.of(ResultCode.USER_NOT_FOUND);
        }
        return toLoginUser(user);
    }

    public static LoginUser toLoginUser(SysUser user) {
        return new LoginUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRoleCode(),
                user.getRealName(),
                user.getStatus() != null && user.getStatus() == 1);
    }
}
