package com.campus.jobagent.modules.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.jobagent.modules.job.entity.JobPost;
import org.apache.ibatis.annotations.Mapper;

/**
 * 岗位 Mapper。
 */
@Mapper
public interface JobPostMapper extends BaseMapper<JobPost> {
}
