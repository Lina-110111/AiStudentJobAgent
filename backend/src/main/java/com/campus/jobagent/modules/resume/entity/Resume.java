package com.campus.jobagent.modules.resume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 简历。
 */
@Data
@TableName("resume")
public class Resume implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属学生 */
    private Long userId;

    /** 简历标题，如“Java后端开发-张三” */
    private String title;

    /** 原始文件名 */
    private String fileName;

    /** 文件相对路径 */
    private String filePath;

    /** 文件类型：pdf / docx / txt */
    private String fileType;

    /** 简历正文（由解析器抽取，供 Agent 诊断使用） */
    private String contentText;

    /** 目标岗位 */
    private String targetJob;

    /** 最近一次诊断得分 */
    private Integer score;

    /** 最近一次诊断结果（JSON） */
    private String diagnosisJson;

    /** 状态：1 正常 / 0 停用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
