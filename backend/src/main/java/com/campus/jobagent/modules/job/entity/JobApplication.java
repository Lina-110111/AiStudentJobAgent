package com.campus.jobagent.modules.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位申请记录。
 */
@Data
@TableName("job_application")
public class JobApplication implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long jobId;

    private Long studentId;

    /** 投递使用的简历 */
    private Long resumeId;

    /** 申请时的匹配分（留痕，便于复盘） */
    private Integer matchScore;

    /** SUBMITTED 已投递 / VIEWED 已查看 / INTERVIEW 面试中 / OFFER 已录用 / REJECTED 未通过 */
    private String status;

    private String remark;

    private LocalDateTime applyTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
