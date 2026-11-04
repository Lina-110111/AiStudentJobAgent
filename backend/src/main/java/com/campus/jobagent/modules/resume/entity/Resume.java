package com.campus.jobagent.modules.resume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学生简历。
 */
@Data
@TableName("resume")
public class Resume implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属学生，指向 sys_user.id */
    private Long userId;

    /** 简历标题 */
    private String title;

    /** 原始文件名 */
    private String fileName;

    /** 相对存储路径 */
    private String filePath;

    /** pdf / docx / txt */
    private String fileType;

    /** 简历正文（供 AI 诊断） */
    private String contentText;

    /** 目标岗位 */
    private String targetJob;

    /** 最近一次诊断得分 */
    private Integer score;

    /** 最近一次诊断报告 JSON */
    private String diagnosisJson;

    /** 1 正常 / 0 停用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
