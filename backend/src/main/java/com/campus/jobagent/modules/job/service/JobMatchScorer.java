package com.campus.jobagent.modules.job.service;

import com.campus.jobagent.modules.job.entity.JobPost;
import com.campus.jobagent.modules.user.entity.SysUser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 岗位匹配打分器。
 *
 * <p>采用“可解释的加权规则”而不是把分数完全交给大模型，原因：
 * 匹配分需要稳定、可复现、可向老师/企业解释；大模型只负责生成自然语言的匹配说明。
 *
 * <p>评分维度（总分 100）：
 * <ul>
 *   <li>技能匹配 40 分：学生技能标签 ∩ 岗位任职要求关键词</li>
 *   <li>学历匹配 15 分：岗位学历要求 vs 学生学历</li>
 *   <li>专业/岗位类别 15 分：学生专业与岗位类别/名称的关联度</li>
 *   <li>地域与意向 15 分：学生求职意向与岗位城市的匹配度</li>
 *   <li>档案完整度 15 分：档案填写完整度（影响推荐的可靠性）</li>
 * </ul>
 */
@Component
public class JobMatchScorer {

    private static final List<String> EDU_ORDER = List.of("专科", "本科", "硕士", "博士");

    public MatchResult score(SysUser student, JobPost job) {
        List<String> reasons = new ArrayList<>();
        int total = 0;

        // 1. 技能匹配
        Set<String> studentSkills = splitTags(student == null ? null : student.getSkills());
        String jobText = (safe(job.getRequirement()) + " " + safe(job.getDescription()) + " " + safe(job.getTitle()));
        Set<String> jobKeywords = splitTags(job.getRequirement());
        List<String> matched = new ArrayList<>();
        for (String skill : studentSkills) {
            if (jobText.contains(skill)) {
                matched.add(skill);
            }
        }
        int skillScore = studentSkills.isEmpty() ? 0
                : (int) Math.round(40.0 * matched.size() / Math.max(1, Math.min(studentSkills.size(), 6)));
        skillScore = Math.min(40, skillScore);
        total += skillScore;
        reasons.add("技能匹配得分 " + skillScore + "/40，命中技能：" + (matched.isEmpty() ? "无" : String.join("、", matched)));

        List<String> missing = new ArrayList<>();
        for (String keyword : jobKeywords) {
            if (!studentSkills.contains(keyword)) {
                missing.add(keyword);
            }
        }

        // 2. 学历匹配
        int eduScore = 15;
        if (StringUtils.hasText(job.getEducationReq()) && student != null) {
            int need = EDU_ORDER.indexOf(job.getEducationReq().replace("及以上", "").trim());
            int own = EDU_ORDER.indexOf(safe(student.getEducation()).trim());
            if (need >= 0 && own >= 0) {
                eduScore = own >= need ? 15 : Math.max(0, 15 - (need - own) * 8);
            }
        }
        total += eduScore;
        reasons.add("学历匹配得分 " + eduScore + "/15");

        // 3. 专业 / 岗位类别
        int majorScore = 6;
        if (student != null && StringUtils.hasText(student.getMajor())) {
            String major = student.getMajor();
            boolean hit = jobText.contains(major)
                    || (StringUtils.hasText(job.getJobCategory()) && major.contains(job.getJobCategory()))
                    || ("计算机".contains(major.substring(0, 1)) && jobText.contains("开发"));
            majorScore = hit ? 15 : 8;
        }
        total += majorScore;
        reasons.add("专业关联度得分 " + majorScore + "/15");

        // 4. 地域与求职意向
        int intentScore = 6;
        if (student != null && StringUtils.hasText(job.getTitle())) {
            String intention = safe(student.getJobIntention());
            boolean cityHit = StringUtils.hasText(job.getCity()) && intention.contains(job.getCity());
            String titlePrefix = job.getTitle().substring(0, Math.min(2, job.getTitle().length()));
            boolean titleHit = intention.contains(titlePrefix);
            intentScore = cityHit || titleHit ? 15 : (intention.isBlank() ? 6 : 9);
        }
        total += intentScore;
        reasons.add("地域/意向匹配得分 " + intentScore + "/15");

        // 5. 档案完整度
        int profileScore = profileCompleteness(student);
        total += profileScore;
        reasons.add("档案完整度得分 " + profileScore + "/15");

        int finalScore = Math.max(30, Math.min(99, total));
        return new MatchResult(finalScore, matched, missing.stream().limit(8).toList(), reasons);
    }

    private int profileCompleteness(SysUser student) {
        if (student == null) {
            return 0;
        }
        int filled = 0;
        List<String> fields = Arrays.asList(
                student.getRealName(), student.getEducation(), student.getMajor(),
                student.getSkills(), student.getJobIntention());
        for (String field : fields) {
            if (StringUtils.hasText(field)) {
                filled++;
            }
        }
        return Math.round(15f * filled / fields.size());
    }

    /** 把逗号/顿号/空格分隔的标签拆成集合。 */
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

    /** 打分结果。 */
    public record MatchResult(int score, List<String> matchedSkills, List<String> missingSkills, List<String> reasons) {

        public String level() {
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
    }
}
