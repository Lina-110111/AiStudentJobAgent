package com.campus.jobagent.modules.internship.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import com.campus.jobagent.modules.internship.entity.InternshipApply;
import com.campus.jobagent.modules.internship.mapper.InternshipApplyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 实习申请服务（基础 CRUD 骨架；审批流转业务由模块负责人扩展）。
 */
@Slf4j
@Service
public class InternshipApplyService extends ServiceImpl<InternshipApplyMapper, InternshipApply> {

    /** 实习申请分页列表（单位/岗位关键词、状态筛选）。 */
    public Page<InternshipApply> pageApplies(long pageNum, long pageSize, String keyword, String status) {
        return page(new Page<>(pageNum, pageSize), Wrappers.<InternshipApply>lambdaQuery()
                .eq(StringUtils.hasText(status), InternshipApply::getStatus, status)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(InternshipApply::getCompanyName, keyword)
                        .or().like(InternshipApply::getPosition, keyword))
                .orderByDesc(InternshipApply::getId));
    }

    /** 按 id 查询，不存在抛异常。 */
    public InternshipApply detail(Long id) {
        InternshipApply apply = getById(id);
        if (apply == null) {
            throw BizException.of(ResultCode.NOT_FOUND, "实习申请不存在");
        }
        return apply;
    }

    /** 新增实习申请。 */
    @Transactional(rollbackFor = Exception.class)
    public InternshipApply create(InternshipApply apply) {
        apply.setId(null);
        if (apply.getStatus() == null) {
            apply.setStatus("RUNNING");
        }
        if (apply.getCurrentNode() == null) {
            apply.setCurrentNode("MENTOR_REVIEW");
        }
        apply.setDeleted(0);
        apply.setCreateTime(LocalDateTime.now());
        apply.setUpdateTime(LocalDateTime.now());
        save(apply);
        return apply;
    }

    /** 更新实习申请。 */
    @Transactional(rollbackFor = Exception.class)
    public InternshipApply update(Long id, InternshipApply apply) {
        InternshipApply existing = detail(id);
        apply.setId(id);
        apply.setCreateTime(existing.getCreateTime());
        apply.setUpdateTime(LocalDateTime.now());
        updateById(apply);
        return getById(id);
    }

    /** 逻辑删除实习申请。 */
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        detail(id);
        removeById(id);
    }
}
