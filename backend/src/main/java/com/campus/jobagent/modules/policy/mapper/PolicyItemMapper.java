package com.campus.jobagent.modules.policy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.jobagent.modules.policy.entity.PolicyItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 政策条目 Mapper。
 */
@Mapper
public interface PolicyItemMapper extends BaseMapper<PolicyItem> {
}
