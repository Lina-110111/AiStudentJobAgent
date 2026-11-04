package com.campus.jobagent.modules.user.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import com.campus.jobagent.modules.user.entity.SysUser;
import com.campus.jobagent.modules.user.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户基础 CRUD 服务（管理员端功能；新增账号请走注册/管理员建号流程，密码需 BCrypt 加密）。
 */
@Slf4j
@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    /** 用户分页列表（关键词 / 角色筛选）。 */
    public Page<SysUser> pageUsers(long pageNum, long pageSize, String keyword, String roleCode) {
        return page(new Page<>(pageNum, pageSize), Wrappers.<SysUser>lambdaQuery()
                .eq(StringUtils.hasText(roleCode), SysUser::getRoleCode, roleCode)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(SysUser::getUsername, keyword)
                        .or().like(SysUser::getRealName, keyword)
                        .or().like(SysUser::getCollege, keyword))
                .orderByDesc(SysUser::getId));
    }

    /** 按 id 查询，不存在抛异常。 */
    public SysUser detail(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw BizException.of(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    /** 更新用户基本信息（密码不在此接口维护）。 */
    @Transactional(rollbackFor = Exception.class)
    public SysUser update(Long id, SysUser user) {
        SysUser existing = detail(id);
        user.setId(id);
        user.setPassword(existing.getPassword());
        user.setCreateTime(existing.getCreateTime());
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
        return getById(id);
    }

    /** 逻辑删除用户。 */
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        detail(id);
        removeById(id);
    }
}
