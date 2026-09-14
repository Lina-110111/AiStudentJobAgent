package com.campus.jobagent.modules.user.controller;

import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import com.campus.jobagent.common.util.SecurityUtils;
import com.campus.jobagent.modules.user.dto.UserVO;
import com.campus.jobagent.modules.user.entity.SysUser;
import com.campus.jobagent.modules.user.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户档案接口。
 */
@Tag(name = "用户与档案", description = "个人档案查询与维护")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final SysUserMapper sysUserMapper;

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public Result<UserVO> me() {
        return Result.ok(UserVO.from(getCurrentUser()));
    }

    @Operation(summary = "更新个人档案", description = "技能与求职意向会直接影响岗位匹配得分")
    @PutMapping("/me")
    public Result<UserVO> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        SysUser user = getCurrentUser();
        user.setRealName(request.realName());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setAvatar(request.avatar());
        user.setCollege(request.college());
        user.setMajor(request.major());
        user.setGrade(request.grade());
        user.setEducation(request.education());
        user.setSkills(request.skills());
        user.setJobIntention(request.jobIntention());
        sysUserMapper.updateById(user);
        return Result.ok("档案已更新", UserVO.from(sysUserMapper.selectById(user.getId())));
    }

    private SysUser getCurrentUser() {
        SysUser user = sysUserMapper.selectById(SecurityUtils.getUserId());
        if (user == null) {
            throw BizException.of(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    /** 个人档案更新请求（仅本接口使用，直接内联在本文件中）。 */
    @Schema(description = "个人档案更新请求")
    public record ProfileUpdateRequest(
            @Size(max = 32, message = "姓名长度不能超过 32 个字符")
            String realName,

            String phone,

            @Email(message = "邮箱格式不正确")
            String email,

            String avatar,

            String college,

            String major,

            String grade,

            String education,

            @Size(max = 255, message = "技能标签过长")
            String skills,

            @Size(max = 255, message = "求职意向过长")
            String jobIntention) {
    }
}
