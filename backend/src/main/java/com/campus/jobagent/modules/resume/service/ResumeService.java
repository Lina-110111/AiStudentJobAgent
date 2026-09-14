package com.campus.jobagent.modules.resume.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.jobagent.agent.AgentOrchestrator;
import com.campus.jobagent.agent.AgentScene;
import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import com.campus.jobagent.common.util.JsonUtils;
import com.campus.jobagent.modules.resume.dto.ResumeCreateRequest;
import com.campus.jobagent.modules.resume.entity.Resume;
import com.campus.jobagent.modules.resume.mapper.ResumeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 简历服务：登记、查询、AI 诊断。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService extends ServiceImpl<ResumeMapper, Resume> {

    private final AgentOrchestrator agentOrchestrator;

    /** 登记一份简历。 */
    @Transactional(rollbackFor = Exception.class)
    public Resume create(Long userId, ResumeCreateRequest request) {
        if (!StringUtils.hasText(request.filePath()) && !StringUtils.hasText(request.contentText())) {
            throw BizException.of(ResultCode.PARAM_INVALID, "请上传简历文件或粘贴简历正文");
        }
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setTitle(request.title());
        resume.setTargetJob(request.targetJob());
        resume.setFilePath(request.filePath());
        resume.setFileName(request.fileName());
        resume.setFileType(request.fileType());
        resume.setContentText(request.contentText());
        resume.setStatus(1);
        resume.setCreateTime(LocalDateTime.now());
        resume.setUpdateTime(LocalDateTime.now());
        resume.setDeleted(0);
        save(resume);
        return resume;
    }

    /** 我的简历列表。 */
    public Page<Resume> pageByUser(Long userId, long pageNum, long pageSize) {
        return page(new Page<>(pageNum, pageSize),
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<Resume>lambdaQuery()
                        .eq(Resume::getUserId, userId)
                        .orderByDesc(Resume::getId));
    }

    /** 查询简历并校验归属，防止越权访问他人简历。 */
    public Resume getOwned(Long resumeId, Long userId, boolean allowAnyOwner) {
        Resume resume = getById(resumeId);
        if (resume == null) {
            throw BizException.of(ResultCode.RESUME_NOT_FOUND);
        }
        if (!allowAnyOwner && !resume.getUserId().equals(userId)) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
        return resume;
    }

    /**
     * 调用智能体生成简历诊断报告，并把结果回写简历记录（便于历史对比）。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> diagnose(Long resumeId, Long userId, String targetJob) {
        Resume resume = getOwned(resumeId, userId, false);
        String job = StringUtils.hasText(targetJob) ? targetJob
                : StringUtils.hasText(resume.getTargetJob()) ? resume.getTargetJob() : "软件开发工程师";

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("简历标题", resume.getTitle());
        payload.put("目标岗位", job);
        payload.put("简历正文", StringUtils.hasText(resume.getContentText()) ? resume.getContentText() : "（未提供正文，仅有附件）");

        AgentOrchestrator.AgentResult result =
                agentOrchestrator.run(AgentScene.RESUME_DIAGNOSIS, JsonUtils.toJson(payload));

        Object score = result.data().get("overallScore");
        if (score instanceof Number number) {
            resume.setScore(number.intValue());
        }
        resume.setDiagnosisJson(JsonUtils.toJson(result.data()));
        resume.setUpdateTime(LocalDateTime.now());
        updateById(resume);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("resumeId", resume.getId());
        response.put("targetJob", job);
        response.put("aiGenerated", result.aiGenerated());
        response.put("engine", result.engine());
        response.put("report", result.data());
        return response;
    }
}
