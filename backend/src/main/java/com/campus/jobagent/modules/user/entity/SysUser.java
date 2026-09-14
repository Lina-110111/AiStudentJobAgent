package com.campus.jobagent.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户（学生 / 辅导员 / 企业HR / 院系管理员）。
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录账号 */
    private String username;

    /** BCrypt 加密后的密码，禁止返回给前端 */
    @JsonIgnore
    private String password;

    private String realName;

    /** 角色编码：STUDENT / COUNSELOR / HR / COLLEGE_ADMIN */
    private String roleCode;

    private String phone;

    private String email;

    private String avatar;

    /** 所属学院 */
    private String college;

    /** 专业 */
    private String major;

    /** 年级，如 2024 */
    private String grade;

    /** 学历：本科 / 硕士 */
    private String education;

    /** 技能标签，逗号分隔 */
    private String skills;

    /** 求职意向 */
    private String jobIntention;

    /** 1 启用 / 0 停用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
