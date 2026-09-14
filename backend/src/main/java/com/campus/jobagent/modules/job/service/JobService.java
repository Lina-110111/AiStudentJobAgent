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
import com.campus.jobagent.modules.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 岗位发布、智能匹配与申请跟踪服务。
 *
 * <p>本模块是脚手架的"示例业务模块"：其它模块（简历、实习、面试、政策）按同样的分层照抄即可。
 *
 * <p>匹配打分采用<b>可解释的加权规则</b>而非直接交给大模型，保证分数稳定、可复现、可答辩；
 * 大模型只负责把匹配结果翻译成自然语言的解读与投递建议。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobService extends ServiceImpl<JobPostMapper, JobPost> {

    private static final List<String> EDU_ORDER = List.of("专科", "本科", "硕士", "博士");

    private final JobApplicationMapper jobApplicationMapper;
    private final SysUserMapper sysUserMapper;
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
        job.setDeleted(0);
        job.setCreateTime(LocalDateTime.now());
        job.setUpdateTime(LocalDateTime.now());
        save(job);
        return job;
    }

    /** 岗位列表（关键词 / 类别 / 城市筛选 + 分页）。 */
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

    /** 岗位下线（仅发布人或院系管理员）。 */
    @Transactional(rollbackFor = Exception.class)
    public void offline(Long jobId, Long operatorId) {
        JobPost job = getExisting(jobId);
        SysUser operator = sysUserMapper.selectById(operatorId);
        boolean isAdmin = operator != null && "COLLEGE_ADMIN".equals(operator.getRoleCode());
        if (!job.getPublisherId().equals(operatorId) && !isAdmin) {
            throw BizException.of(ResultCode.FORBIDDEN);
        }
        job.setStatus(0);
        job.setUpdateTime(LocalDateTime.now());
        updateById(job);
    }

    /** 学生与岗位的匹配：规则打分 + 智能体解读。 */
    public JobMatchVO match(Long jobId, Long studentId) {
        JobPost job = getExisting(jobId);
        SysUser student = sysUserMapper.selectById(studentId);
        if (student == null) {
            throw BizException.of(ResultCode.USER_NOT_FOUND);
        }
        MatchScore score = score(student, job);

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
        payload.put("命中技能", score.matched());
        payload.put("缺失技能", score.missing());

        AgentOrchestrator.AgentResult agentResult =
                agentOrchestrator.run(AgentScene.JOB_MATCH, JsonUtils.toJson(payload));
        return new JobMatchVO(job.getId(), job.getTitle(), job.getCompanyName(), score.score(),
                score.level(), score.matched(), score.missing(), score.reasons(), agentResult.data());
    }

    /** 一键投递（防重复投递）。 */
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
        application.setDeleted(0);
        application.setApplyTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        jobApplicationMapper.insert(application);
        return application;
    }

    /** 我的投递记录（含岗位信息）。 */
    public List<Map<String, Object>> myApplications(Long studentId) {
        List<JobApplication> applications = jobApplicationMapper.selectList(Wrappers.<JobApplication>lambdaQuery()
                .eq(JobApplication::getStudentId, studentId)
                .orderByDesc(JobApplication::getId));
        return applications.stream().map(application -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("application", application);
            item.put("job", getById(application.getJobId()));
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

    /** 企业侧：更新申请状态（已查看 / 面试中 / 录用 / 未通过）。 */
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

    // ================= 匹配打分：技能 40 + 学历 15 + 专业 15 + 地域意向 15 + 档案完整度 15 =================

    private MatchScore score(SysUser student, JobPost job) {
        List<String> reasons = new ArrayList<>();
        String jobText = safe(job.getRequirement()) + " " + safe(job.getDescription()) + " " + safe(job.getTitle());
        Set<String> studentSkills = splitTags(student.getSkills());
        Set<String> jobKeywords = splitTags(job.getRequirement());

        List<String> matched = studentSkills.stream().filter(jobText::contains).toList();
        int skillScore = studentSkills.isEmpty() ? 0
                : Math.min(40, (int) Math.round(40.0 * matched.size() / Math.min(studentSkills.size(), 6)));
        reasons.add("技能匹配得分 " + skillScore + "/40，命中技能：" + (matched.isEmpty() ? "无" : String.join("、", matched)));
        List<String> missing = jobKeywords.stream().filter(keyword -> !studentSkills.contains(keyword)).limit(8).toList();

        int eduScore = 15;
        if (StringUtils.hasText(job.getEducationReq())) {
            int need = EDU_ORDER.indexOf(job.getEducationReq().replace("及以上", "").trim());
            int own = EDU_ORDER.indexOf(safe(student.getEducation()).trim());
            if (need >= 0 && own >= 0) {
                eduScore = own >= need ? 15 : Math.max(0, 15 - (need - own) * 8);
            }
        }
        reasons.add("学历匹配得分 " + eduScore + "/15");

        int majorScore = 6;
        if (StringUtils.hasText(student.getMajor())) {
            boolean hit = jobText.contains(student.getMajor())
                    || (StringUtils.hasText(job.getJobCategory()) && student.getMajor().contains(job.getJobCategory()));
            majorScore = hit ? 15 : 8;
        }
        reasons.add("专业关联度得分 " + majorScore + "/15");

        int intentScore = 6;
        if (StringUtils.hasText(job.getTitle())) {
            String intention = safe(student.getJobIntention());
            boolean cityHit = StringUtils.hasText(job.getCity()) && intention.contains(job.getCity());
            boolean titleHit = intention.contains(job.getTitle().substring(0, Math.min(2, job.getTitle().length())));
            intentScore = cityHit || titleHit ? 15 : (intention.isBlank() ? 6 : 9);
        }
        reasons.add("地域/意向匹配得分 " + intentScore + "/15");

        List<String> profileFields = Arrays.asList(student.getRealName(), student.getEducation(),
                student.getMajor(), student.getSkills(), student.getJobIntention());
        long filled = profileFields.stream().filter(StringUtils::hasText).count();
        int profileScore = Math.round(15f * filled / profileFields.size());
        reasons.add("档案完整度得分 " + profileScore + "/15");

        int total = Math.max(30, Math.min(99, skillScore + eduScore + majorScore + intentScore + profileScore));
        return new MatchScore(total, level(total), matched, missing, reasons);
    }

    private String level(int score) {
        if (score >= 85) {
            return "高度匹配";
        }
        if (score >= 70) {
            return "较为匹配";
        }
        if (score >= 55) {
            return "一般匹配";
        }
        return "匹配度较低";
    }

    /** 逗号 / 顿号 / 空格分隔的标签拆成集合。 */
    private Set<String> splitTags(String text) {
        Set<String> tags = new LinkedHashSet<>();
        if (!StringUtils.hasText(text)) {
            return tags;
        }
        for (String part : text.split("[,，、;；/\\s]+")) {
            String tag = part.trim();
            if (tag.length() >= 2 && tag.length() <= 20) {
                tags.add(tag);
            }
        }
        return tags;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }

    /** 匹配打分中间结果。 */
    private record MatchScore(int score, String level, List<String> matched,
                              List<String> missing, List<String> reasons) {
    }
}
