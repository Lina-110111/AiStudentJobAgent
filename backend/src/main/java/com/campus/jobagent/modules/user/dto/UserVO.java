package com.campus.jobagent.modules.user.dto;

import com.campus.jobagent.common.constant.RoleCode;
import com.campus.jobagent.modules.user.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 用户信息展示对象（不含密码）。
 */
@Schema(description = "用户信息")
public record UserVO(
        Long id,
        String username,
        String realName,
        String roleCode,
        String roleLabel,
        String phone,
        String email,
        String avatar,
        String college,
        String major,
        String grade,
        String education,
        String skills,
        String jobIntention,
        LocalDateTime createTime
) {

    public static UserVO from(SysUser user) {
        return new UserVO(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getRoleCode(),
                roleLabel(user.getRoleCode()),
                user.getPhone(),
                user.getEmail(),
                user.getAvatar(),
                user.getCollege(),
                user.getMajor(),
                user.getGrade(),
                user.getEducation(),
                user.getSkills(),
                user.getJobIntention(),
                user.getCreateTime());
    }

    private static String roleLabel(String roleCode) {
        if (roleCode == null) {
            return "";
        }
        try {
            return RoleCode.valueOf(roleCode).getLabel();
        } catch (IllegalArgumentException e) {
            return roleCode;
        }
    }
}
