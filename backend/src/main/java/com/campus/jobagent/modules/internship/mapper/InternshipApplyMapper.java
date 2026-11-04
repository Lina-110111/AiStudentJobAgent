package com.campus.jobagent.modules.internship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.jobagent.modules.internship.entity.InternshipApply;
import org.apache.ibatis.annotations.Mapper;

/**
 * 实习申请 Mapper。
 */
@Mapper
public interface InternshipApplyMapper extends BaseMapper<InternshipApply> {
}
