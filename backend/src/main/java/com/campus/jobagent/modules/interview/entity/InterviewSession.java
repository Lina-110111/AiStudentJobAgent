package com.campus.jobagent.modules.interview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 面试训练记录。
 */
@Data
@TableName("interview_session")
public class InterviewSession implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 训练学生，指向 sys_user.id */
    private Long studentId;

    /** 面试场景：技术岗 / 管理岗 / 综合素质 */
    private String category;

    /** 难度：EASY / NORMAL / HARD */
    private String difficulty;

    /** 题目列表 JSON */
    private String questionsJson;

    /** 作答与逐题评估 JSON */
    private String answersJson;

    /** 训练报告 JSON */
    private String reportJson;

    /** 总分 */
    private Integer totalScore;

    /** RUNNING / FINISHED */
    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
