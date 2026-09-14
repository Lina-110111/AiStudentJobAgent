package com.campus.jobagent.modules.job.service;

import com.campus.jobagent.modules.job.entity.JobPost;
import com.campus.jobagent.modules.user.entity.SysUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 岗位匹配打分器单元测试。
 */
class JobMatchScorerTest {

    private final JobMatchScorer scorer = new JobMatchScorer();

    private SysUser student(String skills, String intention) {
        SysUser user = new SysUser();
        user.setRealName("张三");
        user.setEducation("本科");
        user.setMajor("软件工程");
        user.setSkills(skills);
        user.setJobIntention(intention);
        return user;
    }

    private JobPost job(String requirement, String city) {
        JobPost post = new JobPost();
        post.setTitle("Java后端开发工程师");
        post.setCompanyName("某科技有限公司");
        post.setJobCategory("计算机软件");
        post.setCity(city);
        post.setEducationReq("本科");
        post.setRequirement(requirement);
        post.setDescription("负责就业服务平台的接口开发与性能优化");
        return post;
    }

    @Test
    @DisplayName("技能命中越多，匹配分越高")
    void higherSkillOverlapShouldScoreHigher() {
        JobPost post = job("Java,Spring Boot,MySQL,Redis,Kafka", "成都");

        JobMatchScorer.MatchResult high = scorer.score(student("Java,Spring Boot,MySQL,Redis,Kafka", "成都 Java后端"), post);
        JobMatchScorer.MatchResult low = scorer.score(student("Photoshop,UI设计", "成都 视觉设计"), post);

        assertTrue(high.score() > low.score(), "命中技能多的学生得分应更高");
        assertTrue(high.matchedSkills().size() >= 3, "命中技能应不少于 3 个");
    }

    @Test
    @DisplayName("匹配分数与等级映射正确")
    void levelMappingShouldBeReasonable() {
        JobMatchScorer.MatchResult result = scorer.score(student("Java,MySQL", "成都"), job("Java,MySQL,Redis", "成都"));
        assertTrue(result.score() >= 30 && result.score() <= 99);
        assertTrue(result.level().endsWith("匹配"));
    }

    @Test
    @DisplayName("学历不满足要求时应扣分")
    void educationGapShouldReduceScore() {
        JobPost post = job("Java,MySQL,Redis", "成都");
        post.setEducationReq("硕士");
        JobMatchScorer.MatchResult bachelor = scorer.score(student("Java,MySQL,Redis", "成都"), post);
        post.setEducationReq("本科");
        JobMatchScorer.MatchResult matched = scorer.score(student("Java,MySQL,Redis", "成都"), post);

        assertTrue(bachelor.score() < matched.score(), "学历不满足应扣分");
    }
}
