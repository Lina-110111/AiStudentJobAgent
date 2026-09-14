package com.campus.jobagent.modules.user.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.jobagent.common.api.PageResult;
import com.campus.jobagent.common.api.Result;
import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import com.campus.jobagent.common.util.SecurityUtils;
import com.campus.jobagent.modules.user.dto.ProfileUpdateRequest;
import com.campus.jobagent.modules.user.dto.UserVO;
import com.campus.jobagent.modules.user.entity.SysUser;
import com.campus.jobagent.modules.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户与档案接口。
 */
@Tag(name = "用户与档案", description = "个人档案维护、用户查询（管理员/辅导员）")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService sysUserService;

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public Result<UserVO> me() {
        SysUser user = sysUserService.getById(SecurityUtils.getUserId());
        if (user == null) {
            throw BizException.of(ResultCode.USER_NOT_FOUND);
        }
        return Result.ok(UserVO.from(user));
    }

    @Operation(summary = "更新当前用户个人档案")
    @PutMapping("/me")
    public Result<UserVO> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        SysUser user = sysUserService.getById(SecurityUtils.getUserId());
        if (user == null) {
            throw BizException.of(ResultCode.USER_NOT_FOUND);
        }
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
        sysUserService.updateById(user);
        return Result.ok("档案已更新", UserVO.from(sysUserService.getById(user.getId())));
    }

    @Operation(summary = "分页查询用户（辅导员/院系管理员）")
    @PreAuthorize("hasAnyRole('COUNSELOR','COLLEGE_ADMIN')")
    @GetMapping
    public Result<PageResult<UserVO>> page(@RequestParam(defaultValue = "1") long pageNum,
                                           @RequestParam(defaultValue = "10") long pageSize,
                                           @RequestParam(required = false) String roleCode,
                                           @RequestParam(required = false) String keyword) {
        Page<SysUser> page = sysUserService.page(new Page<>(pageNum, pageSize),
                Wrappers.<SysUser>lambdaQuery()
                        .eq(roleCode != null && !roleCode.isBlank(), SysUser::getRoleCode, roleCode)
                        .and(keyword != null && !keyword.isBlank(), w -> w
                                .like(SysUser::getUsername, keyword)
                                .or()
                                .like(SysUser::getRealName, keyword))
                        .orderByDesc(SysUser::getId));
        return Result.ok(PageResult.of(page, UserVO::from));
    }

    @Operation(summary = "查询指定用户详情（辅导员/院系管理员）")
    @PreAuthorize("hasAnyRole('COUNSELOR','COLLEGE_ADMIN')")
    @GetMapping("/{id}")
    public Result<UserVO> detail(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            throw BizException.of(ResultCode.USER_NOT_FOUND);
        }
        return Result.ok(UserVO.from(user));
    }
}
