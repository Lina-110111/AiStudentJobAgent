package com.campus.jobagent.modules.user.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.jobagent.modules.user.entity.SysUser;
import com.campus.jobagent.modules.user.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

/**
 * 用户服务：基础 CRUD 由 MyBatis-Plus 提供，复杂业务在子类或本类中扩展。
 */
@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {
}
