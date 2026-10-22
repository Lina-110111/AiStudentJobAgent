package com.campus.jobagent.modules.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import com.campus.jobagent.common.util.SecurityUtils;
import com.campus.jobagent.modules.user.dto.UserVO;
import com.campus.jobagent.modules.user.entity.SysUser;
import com.campus.jobagent.modules.user.mapper.SysUserMapper;
import com.campus.jobagent.modules.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final SysUserService sysUserService;

    @Operation(summary = "用户分页列表（关键词 / 角色筛选）")
    @GetMapping
    public Result<Page<SysUser>> page(@RequestParam(defaultValue = "1") long pageNum,
                                      @RequestParam(defaultValue = "10") long pageSize,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String roleCode) {
        return Result.ok(sysUserService.pageUsers(pageNum, pageSize, keyword, roleCode));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    public Result<SysUser> detail(@PathVariable Long id) {
        return Result.ok(sysUserService.detail(id));
    }

    @Operation(summary = "更新用户（密码不在此接口维护）")
    @PutMapping("/{id}")
    public Result<SysUser> update(@PathVariable Long id, @RequestBody SysUser user) {
        return Result.ok("用户已更新", sysUserService.update(id, user));
    }

    @Operation(summary = "删除用户（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        sysUserService.remove(id);
        return Result.ok("用户已删除", null);
    }

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
