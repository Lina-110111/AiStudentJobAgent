package com.campus.jobagent.modules.internship.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 实习日志。
 */
@Data
@TableName("internship_log")
public class InternshipLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属实习申请，指向 internship_apply.id */
    private Long applyId;

    /** 写日志的学生，指向 sys_user.id */
    private Long studentId;

    /** 第几周 */
    private Integer weekNo;

    /** 日志日期 */
    private LocalDate logDate;

    /** 日志正文 */
    private String content;

    /** 辅导员批阅留言 */
    private String teacherComment;

    /** 辅导员评分 */
    private Integer score;

    /** 智能体点评 */
    private String aiReviewJson;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
