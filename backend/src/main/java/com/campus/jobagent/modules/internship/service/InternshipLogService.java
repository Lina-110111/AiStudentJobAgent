package com.campus.jobagent.modules.internship.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import com.campus.jobagent.modules.internship.entity.InternshipLog;
import com.campus.jobagent.modules.internship.mapper.InternshipLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 实习日志服务（基础 CRUD 骨架；批阅与智能体点评业务由模块负责人扩展）。
 */
@Slf4j
@Service
public class InternshipLogService extends ServiceImpl<InternshipLogMapper, InternshipLog> {

    /** 按实习申请分页查询日志。 */
    public Page<InternshipLog> pageLogs(Long applyId, long pageNum, long pageSize) {
        return page(new Page<>(pageNum, pageSize), Wrappers.<InternshipLog>lambdaQuery()
                .eq(applyId != null, InternshipLog::getApplyId, applyId)
                .orderByDesc(InternshipLog::getLogDate)
                .orderByDesc(InternshipLog::getId));
    }

    /** 按 id 查询，不存在抛异常。 */
    public InternshipLog detail(Long id) {
        InternshipLog log = getById(id);
        if (log == null) {
            throw BizException.of(ResultCode.NOT_FOUND, "实习日志不存在");
        }
        return log;
    }

    /** 新增实习日志。 */
    @Transactional(rollbackFor = Exception.class)
    public InternshipLog create(InternshipLog log) {
        log.setId(null);
        log.setDeleted(0);
        log.setCreateTime(LocalDateTime.now());
        log.setUpdateTime(LocalDateTime.now());
        save(log);
        return log;
    }

    /** 更新实习日志。 */
    @Transactional(rollbackFor = Exception.class)
    public InternshipLog update(Long id, InternshipLog log) {
        InternshipLog existing = detail(id);
        log.setId(id);
        log.setCreateTime(existing.getCreateTime());
        log.setUpdateTime(LocalDateTime.now());
        updateById(log);
        return getById(id);
    }

    /** 逻辑删除实习日志。 */
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        detail(id);
        removeById(id);
    }
}
