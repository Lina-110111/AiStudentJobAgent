package com.campus.jobagent.modules.interview.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import com.campus.jobagent.modules.interview.entity.InterviewSession;
import com.campus.jobagent.modules.interview.mapper.InterviewSessionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 面试训练服务（基础 CRUD 骨架；出题、评分业务由模块负责人扩展）。
 */
@Slf4j
@Service
public class InterviewSessionService extends ServiceImpl<InterviewSessionMapper, InterviewSession> {

    /** 面试训练分页列表（场景关键词、难度筛选）。 */
    public Page<InterviewSession> pageSessions(long pageNum, long pageSize, String keyword, String difficulty) {
        return page(new Page<>(pageNum, pageSize), Wrappers.<InterviewSession>lambdaQuery()
                .eq(StringUtils.hasText(difficulty), InterviewSession::getDifficulty, difficulty)
                .like(StringUtils.hasText(keyword), InterviewSession::getCategory, keyword)
                .orderByDesc(InterviewSession::getId));
    }

    /** 按 id 查询，不存在抛异常。 */
    public InterviewSession detail(Long id) {
        InterviewSession session = getById(id);
        if (session == null) {
            throw BizException.of(ResultCode.INTERVIEW_NOT_FOUND);
        }
        return session;
    }

    /** 新增面试训练记录。 */
    @Transactional(rollbackFor = Exception.class)
    public InterviewSession create(InterviewSession session) {
        session.setId(null);
        if (session.getStatus() == null) {
            session.setStatus("RUNNING");
        }
        session.setDeleted(0);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        save(session);
        return session;
    }

    /** 更新面试训练记录。 */
    @Transactional(rollbackFor = Exception.class)
    public InterviewSession update(Long id, InterviewSession session) {
        InterviewSession existing = detail(id);
        session.setId(id);
        session.setCreateTime(existing.getCreateTime());
        session.setUpdateTime(LocalDateTime.now());
        updateById(session);
        return getById(id);
    }

    /** 逻辑删除面试训练记录。 */
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        detail(id);
        removeById(id);
    }
}
