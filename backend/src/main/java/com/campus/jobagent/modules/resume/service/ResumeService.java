package com.campus.jobagent.modules.resume.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import com.campus.jobagent.modules.resume.entity.Resume;
import com.campus.jobagent.modules.resume.mapper.ResumeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 简历服务（基础 CRUD 骨架，业务逻辑由模块负责人扩展）。
 */
@Slf4j
@Service
public class ResumeService extends ServiceImpl<ResumeMapper, Resume> {

    /** 简历分页列表（标题关键词筛选）。 */
    public Page<Resume> pageResumes(long pageNum, long pageSize, String keyword) {
        return page(new Page<>(pageNum, pageSize), Wrappers.<Resume>lambdaQuery()
                .like(StringUtils.hasText(keyword), Resume::getTitle, keyword)
                .orderByDesc(Resume::getId));
    }

    /** 按 id 查询，不存在抛异常。 */
    public Resume detail(Long id) {
        Resume resume = getById(id);
        if (resume == null) {
            throw BizException.of(ResultCode.RESUME_NOT_FOUND);
        }
        return resume;
    }

    /** 新增简历。 */
    @Transactional(rollbackFor = Exception.class)
    public Resume create(Resume resume) {
        resume.setId(null);
        if (resume.getStatus() == null) {
            resume.setStatus(1);
        }
        resume.setDeleted(0);
        resume.setCreateTime(LocalDateTime.now());
        resume.setUpdateTime(LocalDateTime.now());
        save(resume);
        return resume;
    }

    /** 更新简历。 */
    @Transactional(rollbackFor = Exception.class)
    public Resume update(Long id, Resume resume) {
        Resume existing = detail(id);
        resume.setId(id);
        resume.setCreateTime(existing.getCreateTime());
        resume.setUpdateTime(LocalDateTime.now());
        updateById(resume);
        return getById(id);
    }

    /** 逻辑删除简历。 */
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        detail(id);
        removeById(id);
    }
}
