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

    private Long applyId;

    private Long studentId;

    /** 第几周 */
    private Integer weekNo;

    private LocalDate logDate;

    /** 日志正文 */
    private String content;

    /** 辅导员批阅留言 */
    private String teacherComment;

    /** 辅导员评分 */
    private Integer score;

    /** 智能体点评（JSON） */
    private String aiReviewJson;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
