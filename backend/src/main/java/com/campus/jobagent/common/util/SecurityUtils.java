package com.campus.jobagent.common.util;

import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import com.campus.jobagent.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 从 Spring Security 上下文中读取当前登录用户，业务代码用它替代“从 token 里手动解析”。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw BizException.of(ResultCode.UNAUTHORIZED);
        }
        return loginUser;
    }

    public static Long getUserId() {
        return getLoginUser().getUserId();
    }

    public static String getRoleCode() {
        return getLoginUser().getRoleCode();
    }

    public static boolean hasRole(String roleCode) {
        LoginUser loginUser = getLoginUser();
        return roleCode != null && roleCode.equals(loginUser.getRoleCode());
    }
}
