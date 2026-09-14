package com.campus.jobagent.modules.job.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.jobagent.agent.AgentOrchestrator;
import com.campus.jobagent.agent.AgentScene;
import com.campus.jobagent.common.api.ResultCode;
import com.campus.jobagent.common.exception.BizException;
import com.campus.jobagent.common.util.JsonUtils;
import com.campus.jobagent.modules.job.dto.ApplicationStatusRequest;
import com.campus.jobagent.modules.job.dto.JobMatchVO;
import com.campus.jobagent.modules.job.dto.JobPostCreateRequest;
import com.campus.jobagent.modules.job.entity.JobApplication;
import com.campus.jobagent.modules.job.entity.JobPost;
import com.campus.jobagent.modules.job.mapper.JobApplicationMapper;
import com.campus.jobagent.modules.job.mapper.JobPostMapper;
import com.campus.jobagent.modules.user.entity.SysUser;
import com.campus.jobagent.modules.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 岗位发布与匹配、申请跟踪服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobService extends ServiceImpl<JobPostMapper, JobPost> {

    private final JobApplicationMapper jobApplicationMapper;
    private final JobMatchScorer jobMatchScorer;
    private final SysUserService sysUserService;
    private final AgentOrchestrator agentOrchestrator;

    /** 发布岗位。 */
    @Transactional(rollbackFor = Exception.class)
    public JobPost publish(Long publisherId, JobPostCreateRequest request) {
        JobPost job = new JobPost();
        job.setTitle(request.title());
        job.setCompanyName(request.companyName());
        job.setPublisherId(publisherId);
        job.setJobCategory(request.jobCategory());
        job.setCity(request.city());
        job.setSalaryMin(request.salaryMin());
        job.setSalaryMax(request.salaryMax());
        job.setHeadcount(request.headcount());
        job.setEducationReq(request.educationReq());
        job.setDescription(request.description());
        job.setRequirement(request.requirement());
        job.setDeadline(request.deadline());
        job.setStatus(1);
        job.setCreateTime(LocalDateTime.now());
        job.setUpdateTime(LocalDateTime.now());
        job.setDeleted(0);
        save(job);
        return job;
    }

    /** 岗位列表（学生/辅导员/HR 均可见，支持关键词与类别过滤）。 */
    public Page<JobPost> pageJobs(long pageNum, long pageSize, String keyword, String category, String city) {
        return page(new Page<>(pageNum, pageSize), Wrappers.<JobPost>lambdaQuery()
                .eq(JobPost::getStatus, 1)
                .eq(StringUtils.hasText(category), JobPost::getJobCategory, category)
                .eq(StringUtils.hasText(city), JobPost::getCity, city)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(JobPost::getTitle, keyword)
                        .or().like(JobPost::getCompanyName, keyword)
                        .or().like(JobPost::getRequirement, keyword))
                .orderByDesc(JobPost::getId));
    }

    public JobPost getExisting(Long jobId) {
        JobPost job = getById(jobId);
        if (job == null) {
            throw BizException.of(ResultCode.JOB_NOT_FOUND);
        }
        return job;
    }

    /** 下线岗位（仅发布人或管理员）。 */
    @Transactional(rollbackFor = Exception.class)
    public void offline(Long jobId, Long operatorId) {
        JobPost job = getExisting(jobId);
        if (!job.getPublisherId().equals(operatorId) && !isAdmin(operatorId)) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
        job.setStatus(0);
        job.setUpdateTime(LocalDateTime.now());
        updateById(job);
    }

    /**
     * 学生与岗位的匹配：规则打分（可解释）+ 智能体生成匹配说明。
     */
    public JobMatchVO match(Long jobId, Long studentId) {
        JobPost job = getExisting(jobId);
        SysUser student = sysUserService.getById(studentId);
        if (student == null) {
            throw BizException.of(ResultCode.USER_NOT_FOUND);
        }
        JobMatchScorer.MatchResult score = jobMatchScorer.score(student, job);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("学生档案", Map.of(
                "专业", safe(student.getMajor()),
                "学历", safe(student.getEducation()),
                "技能", safe(student.getSkills()),
                "求职意向", safe(student.getJobIntention())));
        payload.put("岗位信息", Map.of(
                "岗位名称", safe(job.getTitle()),
                "企业", safe(job.getCompanyName()),
                "城市", safe(job.getCity()),
                "任职要求", safe(job.getRequirement())));
        payload.put("规则打分", score.score());
        payload.put("命中技能", score.matchedSkills());
        payload.put("缺失技能", score.missingSkills());

        AgentOrchestrator.AgentResult agentResult =
                agentOrchestrator.run(AgentScene.JOB_MATCH, JsonUtils.toJson(payload));
        return new JobMatchVO(job.getId(), job.getTitle(), job.getCompanyName(),
                score.score(), score.level(), score.matchedSkills(), score.missingSkills(),
                score.reasons(), agentResult.data());
    }

    /** 一键投递。 */
    @Transactional(rollbackFor = Exception.class)
    public JobApplication apply(Long jobId, Long studentId, Long resumeId, Integer matchScore) {
        JobPost job = getExisting(jobId);
        if (job.getStatus() == null || job.getStatus() != 1) {
            throw BizException.of(ResultCode.JOB_OFFLINE);
        }
        Long exists = jobApplicationMapper.selectCount(Wrappers.<JobApplication>lambdaQuery()
                .eq(JobApplication::getJobId, jobId)
                .eq(JobApplication::getStudentId, studentId));
        if (exists != null && exists > 0) {
            throw BizException.of(ResultCode.ALREADY_APPLIED);
        }
        JobApplication application = new JobApplication();
        application.setJobId(jobId);
        application.setStudentId(studentId);
        application.setResumeId(resumeId);
        application.setMatchScore(matchScore);
        application.setStatus("SUBMITTED");
        application.setApplyTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        application.setDeleted(0);
        jobApplicationMapper.insert(application);
        return application;
    }

    /** 我的投递记录（含岗位信息）。 */
    public List<Map<String, Object>> myApplications(Long studentId) {
        List<JobApplication> applications = jobApplicationMapper.selectList(Wrappers.<JobApplication>lambdaQuery()
                .eq(JobApplication::getStudentId, studentId)
                .orderByDesc(JobApplication::getId));
        return applications.stream().map(application -> {
            JobPost job = getById(application.getJobId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("application", application);
            item.put("job", job);
            return item;
        }).toList();
    }

    /** 企业侧：查看某岗位收到的申请。 */
    public Page<JobApplication> pageApplications(Long jobId, long pageNum, long pageSize) {
        return jobApplicationMapper.selectPage(new Page<>(pageNum, pageSize),
                Wrappers.<JobApplication>lambdaQuery()
                        .eq(jobId != null, JobApplication::getJobId, jobId)
                        .orderByDesc(JobApplication::getId));
    }

    /** 企业侧：更新申请状态并留痕。 */
    @Transactional(rollbackFor = Exception.class)
    public JobApplication updateStatus(Long applicationId, ApplicationStatusRequest request) {
        JobApplication application = jobApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw BizException.of(ResultCode.NOT_FOUND, "申请记录不存在");
        }
        application.setStatus(request.status());
        application.setRemark(request.remark());
        application.setUpdateTime(LocalDateTime.now());
        jobApplicationMapper.updateById(application);
        return application;
    }

    private boolean isAdmin(Long operatorId) {
        SysUser operator = sysUserService.getById(operatorId);
        return operator != null && "COLLEGE_ADMIN".equals(operator.getRoleCode());
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
