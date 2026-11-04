package com.campus.jobagent.modules.policy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 就业政策条目。
 */
@Data
@TableName("policy_item")
public class PolicyItem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 政策标题 */
    private String title;

    /** 分类 */
    private String category;

    /** 适用地区 */
    private String region;

    /** 发布单位 */
    private String publishOrg;

    /** 发布日期 */
    private LocalDate publishDate;

    /** 政策正文 */
    private String content;

    /** 政策来源链接 */
    private String sourceUrl;

    /** 检索标签 */
    private String tags;

    /** 浏览次数 */
    private Integer viewCount;

    /** 1 有效 / 0 失效 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
