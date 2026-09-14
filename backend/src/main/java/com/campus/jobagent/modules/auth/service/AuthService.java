package com.campus.jobagent.modules.auth.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.constant.RoleCode;
import com.campus.jobagent.common.exception.BizException;
import com.campus.jobagent.modules.auth.dto.LoginRequest;
import com.campus.jobagent.modules.auth.dto.LoginResponse;
import com.campus.jobagent.modules.auth.dto.RegisterRequest;
import com.campus.jobagent.modules.user.dto.UserVO;
import com.campus.jobagent.modules.user.entity.SysUser;
import com.campus.jobagent.modules.user.service.SysUserService;
import com.campus.jobagent.security.JwtTokenProvider;
import com.campus.jobagent.security.LoginUser;
import com.campus.jobagent.security.LoginUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务：注册、登录、当前用户。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginUserService loginUserService;

    /** 注册：账号唯一性校验 + 密码加密存储。 */
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterRequest request) {
        Long count = sysUserService.getBaseMapper().selectCount(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, request.username()));
        if (count != null && count > 0) {
            throw BizException.of(ResultCode.USERNAME_EXISTS);
        }
        if (!RoleCode.isValid(request.roleCode())) {
            throw BizException.of(ResultCode.PARAM_INVALID, "角色编码不合法");
        }
        SysUser user = new SysUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRealName(request.realName());
        user.setRoleCode(request.roleCode());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDeleted(0);
        sysUserService.save(user);
        log.info("新用户注册成功：username={}, role={}", user.getUsername(), user.getRoleCode());
        return UserVO.from(user);
    }

    /** 登录：校验账号密码并签发 JWT。 */
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserService.getOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, request.username())
                .last("limit 1"));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw BizException.of(ResultCode.PASSWORD_ERROR);
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw BizException.of(ResultCode.USER_DISABLED);
        }
        LoginUser loginUser = LoginUserService.toLoginUser(user);
        String token = jwtTokenProvider.generateToken(loginUser);
        return new LoginResponse(token, jwtTokenProvider.getExpireMillis(), UserVO.from(user));
    }

    /** 查询当前登录用户。 */
    public UserVO currentUser(Long userId) {
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw BizException.of(ResultCode.USER_NOT_FOUND);
        }
        return UserVO.from(user);
    }
}
