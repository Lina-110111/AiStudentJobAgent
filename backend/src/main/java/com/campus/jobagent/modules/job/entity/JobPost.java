package com.campus.jobagent.modules.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 校招岗位。
 */
@Data
@TableName("job_post")
public class JobPost implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 岗位名称 */
    private String title;

    /** 企业名称 */
    private String companyName;

    /** 发布人（企业HR / 辅导员） */
    private Long publisherId;

    /** 岗位类别：互联网/金融/制造/教育/公务员/其他 */
    private String jobCategory;

    /** 工作城市 */
    private String city;

    /** 薪资下限（元/月） */
    private Integer salaryMin;

    /** 薪资上限（元/月） */
    private Integer salaryMax;

    /** 招聘人数 */
    private Integer headcount;

    /** 学历要求：专科 / 本科 / 硕士 */
    private String educationReq;

    /** 岗位职责 */
    private String description;

    /** 任职要求（技能关键词集中在这里） */
    private String requirement;

    /** 截止日期 */
    private LocalDate deadline;

    /** 1 招聘中 / 0 已下线 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
