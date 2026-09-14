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
 * 就业政策条目（知识库的最小单元）。
 */
@Data
@TableName("policy_item")
public class PolicyItem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    /** 分类：就业补贴 / 基层就业 / 创业扶持 / 落户档案 / 参军入伍 / 权益保障 */
    private String category;

    /** 适用地区 */
    private String region;

    /** 发布单位 */
    private String publishOrg;

    private LocalDate publishDate;

    /** 政策正文 */
    private String content;

    private String sourceUrl;

    /** 检索标签，逗号分隔 */
    private String tags;

    private Integer viewCount;

    /** 1 有效 / 0 失效 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
