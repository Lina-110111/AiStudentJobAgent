package com.campus.jobagent.modules.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.jobagent.modules.job.entity.JobApplication;
import org.apache.ibatis.annotations.Mapper;

/**
 * 岗位申请 Mapper。
 */
@Mapper
public interface JobApplicationMapper extends BaseMapper<JobApplication> {
}
