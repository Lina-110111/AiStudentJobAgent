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
 * 实习申请（携带流程节点，对应题目“基于流程模型实现的多级审批”）。
 */
@Data
@TableName("internship_apply")
public class InternshipApply implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private String companyName;

    private String position;

    /** 企业导师 */
    private Long mentorId;

    /** 校内指导教师（辅导员） */
    private Long teacherId;

    /** 汇总状态：RUNNING 审批中 / PASSED 已通过 / REJECTED 已驳回 */
    private String status;

    /** 当前流程节点：MENTOR_REVIEW / COUNSELOR_REVIEW / COLLEGE_REVIEW */
    private String currentNode;

    /** 流程流转轨迹（JSON 数组，记录每个节点的操作人、意见、时间） */
    private String flowTraceJson;

    private LocalDate startDate;

    private LocalDate endDate;

    /** 学生自评 */
    private Integer selfScore;

    /** 企业导师评价 */
    private Integer mentorScore;

    /** 校方评定 */
    private Integer teacherScore;

    /** 三方加权汇总分 */
    private Integer finalScore;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
