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
 * 实习申请（含流程节点与三级评分）。
 */
@Data
@TableName("internship_apply")
public class InternshipApply implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 申请学生，指向 sys_user.id */
    private Long studentId;

    /** 实习单位 */
    private String companyName;

    /** 实习岗位 */
    private String position;

    /** 企业导师，指向 sys_user.id */
    private Long mentorId;

    /** 校内指导教师，指向 sys_user.id */
    private Long teacherId;

    /** RUNNING / PASSED / REJECTED */
    private String status;

    /** 当前流程节点：MENTOR_REVIEW/COUNSELOR_REVIEW/COLLEGE_REVIEW/PASSED/REJECTED */
    private String currentNode;

    /** 流程流转轨迹 */
    private String flowTraceJson;

    /** 实习开始日期 */
    private LocalDate startDate;

    /** 实习结束日期 */
    private LocalDate endDate;

    /** 学生自评 */
    private Integer selfScore;

    /** 企业导师评价 */
    private Integer mentorScore;

    /** 校方评定 */
    private Integer teacherScore;

    /** 加权汇总分 */
    private Integer finalScore;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
